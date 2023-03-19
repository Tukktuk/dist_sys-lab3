import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import java.nio.charset.StandardCharsets;

public class PingPong {

    private static final String EXCHANGE_NAME = "pingpong_ex";
    
    
    public static void main(String[] argv) throws Exception {
        String ID = argv[0];
        boolean hasStarted = false;
        
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.exchangeDeclare(EXCHANGE_NAME, "direct");
        String queueName = channel.queueDeclare().getQueue();
        
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            System.out.println(" [x] Received '" + message + "'");
            doWait();
            String[] messParts = message.split(" ");
            
            String messType = messParts[0];
            System.out.println(messType);
            String fromID = messParts[2];
            System.out.println(fromID);
            if (messType.equals("PING")) {
                message = "PONG from "+ID;
                System.out.println(message);
                channel.basicPublish(EXCHANGE_NAME, "pong"+fromID, null, message.getBytes("UTF-8"));
            } else if (messType.equals("PONG")) {
                message = "PING from "+ID;
                channel.basicPublish(EXCHANGE_NAME, "ping"+fromID, null, message.getBytes("UTF-8"));
            }
        };

        channel.queueBind(queueName, EXCHANGE_NAME, "ping"+ID);
        System.out.println(" [*] Waiting for ping with routingKey: "+"ping"+ID);
        channel.basicConsume(queueName, true, deliverCallback, consumerTag -> { });
        
        
        // Send a PING
        System.out.println("If you want to start, enter the ID of node you want to ping");
        // received PING instruction
        String destID = System.console().readLine();
        String message = "PING from "+ID;
        // make self unpingable but pongable
        channel.queueUnbind(queueName, EXCHANGE_NAME, "ping"+ID);
        channel.queueBind(queueName, EXCHANGE_NAME, "pong"+ID);

        channel.basicPublish(EXCHANGE_NAME, "ping"+destID, null, message.getBytes("UTF-8"));
        System.out.println(" [x] Sent '"+message+"' to '" + destID + "'");
        
    }

    private static void doWait() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException _ignored) {
            Thread.currentThread().interrupt();
        }
    }

}