import me.marvin.proxy.networking.*
import me.marvin.proxy.networking.packet.*
import me.marvin.proxy.utils.*
import me.marvin.proxy.utils.ByteBufUtils.*

import io.netty.buffer.*
import io.netty.channel.*

import java.util.*

object Constants {
    val SINKED_CHANNELS = setOf("minecraft:register", "minecraft:registerfabric")
}

object Sink : PacketListener {
    override fun priority(): Byte = 0

    override fun handle(type: PacketType, buf: ByteBuf, send: Channel, recv: ChannelHandlerContext, version: Version): Tristate {
        if (type == PacketTypes.Play.Client.PLUGIN_MESSAGE) {
            IndexRollback.readerManual(buf).use {
                val channel = readString(buf)
                
                if (Constants.SINKED_CHANNELS.contains(channel)) {
                    return Tristate.TRUE
                } else {
                    return Tristate.NOT_SET
                }
            }
        }
        return Tristate.NOT_SET
    }
}

@Entrypoint
fun entry() {
    logger.info("[Sink] Started!")
    logger.info("[Sink] Redirecting channels to sink: ${Constants.SINKED_CHANNELS.joinToString(separator = ",") { "'$it'"}}")
    proxy.registerListeners(Sink)
}

@Destructor
fun destruct() {
    proxy.unregisterListener(Sink)
    logger.info("[Sink] Stopped!")
}
