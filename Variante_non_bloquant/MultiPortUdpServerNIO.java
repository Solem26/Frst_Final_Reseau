package Variante_non_bloquant;


import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;

public class MultiPortUdpServerNIO {

    public static void main(String[] args) {

        try {

            Selector selector = Selector.open();

            DatagramChannel channel1 = DatagramChannel.open();
            channel1.bind(new InetSocketAddress(8000));
            channel1.configureBlocking(false);
            channel1.register(selector, SelectionKey.OP_READ);

            DatagramChannel channel2 = DatagramChannel.open();
            channel2.bind(new InetSocketAddress(8001));
            channel2.configureBlocking(false);
            channel2.register(selector, SelectionKey.OP_READ);

            System.out.println("Serveur NIO en attente...");

            ByteBuffer buffer = ByteBuffer.allocate(1024);

            while (true) {

                selector.select();

                Iterator<SelectionKey> keys =
                        selector.selectedKeys().iterator();

                while (keys.hasNext()) {

                    SelectionKey key = keys.next();
                    keys.remove();

                    if (key.isReadable()) {

                        DatagramChannel channel =
                                (DatagramChannel) key.channel();

                        buffer.clear();

                        InetSocketAddress client =
                                (InetSocketAddress) channel.receive(buffer);

                        buffer.flip();

                        byte[] data = new byte[buffer.remaining()];
                        buffer.get(data);

                        String msg = new String(data);

                        System.out.println(
                                "Port "
                                        + channel.socket().getLocalPort()
                                        + " | "
                                        + client
                                        + " : "
                                        + msg
                        );
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}