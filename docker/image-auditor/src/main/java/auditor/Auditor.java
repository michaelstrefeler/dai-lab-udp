package auditor;

import com.google.gson.*;

import java.io.IOException;
import java.net.*;
import java.util.UUID;
import java.net.MulticastSocket;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.DatagramPacket;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.nio.charset.StandardCharsets.*;


public class Auditor {
    final static String IPADDRESS = "239.255.22.5";
    final static int PORT = 9904;

    /**
     * Record for the message JSON object
     * @param uuid
     * @param sound
     */
    public record Message(UUID uuid, String sound){}

    public static void main(String[] args) {
        System.out.println("Hello");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                // Accept incoming connections
                // Start a service thread
                Thread.ofVirtual().start(() -> {
                    try (MulticastSocket socket = new MulticastSocket(PORT)) {
                        InetSocketAddress group_address =  new InetSocketAddress(IPADDRESS, PORT);
                        NetworkInterface netif = NetworkInterface.getByName("eth0");
                        socket.joinGroup(group_address, netif);

                        byte[] buffer = new byte[1024];
                        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                        socket.receive(packet);
                        String message = new String(packet.getData(), 0, packet.getLength(), UTF_8);

                        System.out.println("Received message: " + message + " from " + packet.getAddress() + ", port " + packet.getPort());
                        socket.leaveGroup(group_address, netif);
                    } catch (IOException ex) {
                        System.out.println(ex.getMessage());
                    }
                });
            }
        } catch (IOException e) {
            System.out.println("Exception caught when trying to listen on port "
                    + PORT + " or listening for a connection");
            System.out.println(e.getMessage());
        }
    }
}