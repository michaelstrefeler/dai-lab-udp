package auditor;

import com.google.gson.*;

import java.io.*;
import java.net.*;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import static java.nio.charset.StandardCharsets.*;


public class Auditor {
    final static String IPADDRESS = "239.255.22.5";
    final static int MULTICAST_PORT = 9904;
    final static int TCP_PORT = 2205;
    private static final Logger LOG = Logger.getLogger(Auditor.class.getName());
    private static ConcurrentHashMap<Message, Long> orchestra = new ConcurrentHashMap<>();

    /**
     * Record for the message JSON object
     * @param uuid
     * @param sound
     */
    public record Message(UUID uuid, String sound){}

    public static void main(String[] args) throws InterruptedException {
        // UDP thread
        Thread UDP = Thread.ofVirtual().start(() -> {
            LOG.info("UDP thread started");
            try (MulticastSocket socket = new MulticastSocket(MULTICAST_PORT)) {
                while(true){
                    InetSocketAddress group_address =  new InetSocketAddress(IPADDRESS, MULTICAST_PORT);

                    NetworkInterface netif = NetworkInterface.getByInetAddress(InetAddress.getLocalHost());
                    socket.joinGroup(group_address, netif);

                    byte[] buffer = new byte[1024];
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

                    // Receive the packet
                    socket.receive(packet);
                    String msg = new String(packet.getData(), 0, packet.getLength(), UTF_8);
                    LOG.info(msg);
                    // Create a message using Gson
                    Message message = new Gson().fromJson(msg, Message.class);

                    System.out.println("Received message: " + msg + " from " + packet.getAddress() + ", port " + packet.getPort());

                    socket.leaveGroup(group_address, netif);

                    Long currentTime = System.currentTimeMillis();
                    // Add message to hashmap
                    orchestra.put(message, currentTime);
                }
            } catch (IOException ex) {
                System.out.println(ex.getMessage());
            }
        });

        // TCP thread
        Thread TCP = Thread.ofVirtual().start(() ->{
            LOG.info("TCP thread started");
            try (ServerSocket serverSocket = new ServerSocket(TCP_PORT)) {
                while (true) {
                    try (Socket socket = serverSocket.accept();
                         var out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), UTF_8))) {

                        StringBuilder sb = new StringBuilder("[");
                        Long currentTime = System.currentTimeMillis();
                        orchestra.forEach((key, value) -> {
                            if (currentTime - value > 5000){
                                orchestra.remove(key);
                            }else{
                                JsonObject jsonObject = new JsonObject();
                                jsonObject.addProperty("uuid", String.valueOf(key.uuid()));
                                jsonObject.addProperty("instrument", key.sound());
                                jsonObject.addProperty("lastActivity", value);
                                sb.append(jsonObject);
                                sb.append(",\n");
                            }
                        });
                        sb.append("]");
                        out.write(sb + "\n");
                        out.flush();
                    } catch (IOException e) {
                        System.out.println("Server: socket ex.: " + e);
                    }
                }
            } catch (IOException e) {
                System.out.println("Server: server socket ex.: " + e);
            }
        });

        TCP.join();
        UDP.join();
    }
}