import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import java.nio.charset.StandardCharsets;

public class Ping {

    
    //private int ID;
    //private boolean hasStarted;
    private final static String QUEUE_PING = "ping";
    private final static String QUEUE_PONG = "pong";

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        // Declare queues, PING and PONG
        channel.queueDeclare(QUEUE_PONG, false, false, false, null);
        channel.queueDeclare(QUEUE_PING, false, false, false, null);
        System.out.println(" [*] Ready to receive pongs. To exit press CTRL+C");

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            System.out.println(" [x] Received '" + message + "'");
            doWait();
            // Send ping in response
            message = "PING";
            channel.basicPublish("", QUEUE_PING, null, message.getBytes(StandardCharsets.UTF_8));
            System.out.println(" [x] Sent '" + message + "'");
        };

        channel.basicConsume(QUEUE_PONG, true, deliverCallback, consumerTag -> { });

        
        // Send a start PING
        // Using Console to input data from user
        String name = System.console().readLine();
 
        System.out.println("You entered string " + name);
        
        String message = "PING";
        channel.basicPublish("", QUEUE_PING, null, message.getBytes(StandardCharsets.UTF_8));
        System.out.println(" [x] Sent '" + message + "'");
    }

    private static void doWait() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException _ignored) {
            Thread.currentThread().interrupt();
        }
    }

}