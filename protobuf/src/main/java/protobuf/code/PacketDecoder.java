package protobuf.code;

import com.google.protobuf.Message;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protobuf.analysis.ParseMap;

import java.util.List;

/**
 * Created by Administrator on 2016/1/29.
 */
public class PacketDecoder extends ByteToMessageDecoder {
    private static final Logger logger = LoggerFactory.getLogger(PacketDecoder.class);

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in,
                          List<Object> out) throws Exception {

        int length = checkLength(ctx, in);
        if (length == -1 || length == 0)
            return;

        ByteBuf byteBuf = Unpooled.buffer(length);
        in.readBytes(byteBuf);

        try {
            /* 解密消息体
            ThreeDES des = ctx.channel().attr(ClientAttr.ENCRYPT).get();
            byte[] bareByte = des.decrypt(inByte);*/

            int ptoNum = in.readInt();
            byte[] body= byteBuf.array();

            Message msg = ParseMap.getMessage(ptoNum, body);
            out.add(msg);
            logger.info("GateServer Received Message: content length {}, ptoNum: {}", length, ptoNum);

        } catch (Exception e) {
            logger.error(ctx.channel().remoteAddress() + ",decode failed.", e);
        }
    }

    int checkLength(ChannelHandlerContext ctx, ByteBuf in){
        in.markReaderIndex();

        if (in.readableBytes() < 2) {
            logger.error("readableBytes length less than 2 bytes");
            return 0;
        }

        int length = in.readInt();

        if (length < 0) {
            ctx.close();
            logger.error("message length less than 0, channel closed");
        }

        if (length > in.readableBytes()) {
            in.resetReaderIndex();
            logger.error("message received is incomplete");
            return -1;
        }
        return length;
    }
}