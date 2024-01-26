package musician;

import com.google.gson.*;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.util.UUID;

import static java.nio.charset.StandardCharsets.*;

class Musician {
    final static String IPADDRESS = "239.255.22.5";
    final static int PORT = 9904;

    /**
     * Record for the message JSON object
     * @param uuid
     * @param sound
     */
    public record Message(UUID uuid, String sound){}

    public static void main(String[] args) {
        while (true){
            try (DatagramSocket socket = new DatagramSocket()) {
                UUID uuid = UUID.randomUUID();

                String sound = null;
                if (args.length > 0) {
                    sound = switch (args[0]) {
                        case "piano" -> "ti-ta-ti";
                        case "flute" -> "trulu";
                        case "drum" -> "boum-boum";
                        case "trumpet" -> "pouet";
                        case "violin" -> "gzi-gzi";
                        default -> "*cricket noises*";
                    };
                }

                try {
                    // Create Message object
                   Message message = new Message(uuid, sound);

                    // Convert message object to JSON object
                    String json = new Gson().toJson(message);

                    // Put the JSON string into the payload
                    byte[] payload = json.getBytes(UTF_8);
                    InetSocketAddress dest_address = new InetSocketAddress(IPADDRESS, PORT);
                    var packet = new DatagramPacket(payload, payload.length, dest_address);
                    socket.send(packet);
                } catch (Exception ex) {
                    System.out.println("Error could not create a GSON object!");
                }
            } catch (IOException ex) {
                System.out.println(ex.getMessage());
            }

            try {
                Thread.sleep(1000);
            }catch (InterruptedException e){
                // Restore the interrupted status
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}