import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.nio.charset.StandardCharsets;

public class Pong {

    private final static String QUEUE_PING = "ping";
    private final static String QUEUE_PONG = "pong";

    public static void main(String[] argv) throws Exception {
    
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.queueDeclare(QUEUE_PING, false, false, false, null);
        channel.queueDeclare(QUEUE_PONG, false, false, false, null);
        System.out.println(" [*] Ready to receive pings. To exit press CTRL+C");

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            System.out.println(" [x] Received '" + message + "'");
            doWait();
            // Send pong in response
            message = "PONG";
            channel.basicPublish("", QUEUE_PONG, null, message.getBytes(StandardCharsets.UTF_8));
            System.out.println(" [x] Sent '" + message + "'");
        };

        channel.basicConsume(QUEUE_PING, true, deliverCallback, consumerTag -> { });

    }

    private static void doWait() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException _ignored) {
            Thread.currentThread().interrupt();
        }
    }
}