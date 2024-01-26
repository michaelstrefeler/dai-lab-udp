package musician;

import com.google.gson.*;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import static java.nio.charset.StandardCharsets.UTF_8;

class Musician {
    private static final Logger LOG = Logger.getLogger(Musician.class.getName());
    final static String IPADDRESS = "239.255.22.5";
    final static int PORT = 9904;

    /**
     * Record for the message JSON object
     * @param uuid
     * @param sound
     */
    public record Message(UUID uuid, String sound){}

    public static void main(String[] args) {
        UUID uuid = UUID.randomUUID();
        String sound = null;
        HashMap<String, String> sounds = new HashMap<>();
        sounds.put("piano", "ti-ta-ti");
        sounds.put("flute", "trulu");
        sounds.put("drum", "boum-boum");
        sounds.put("trumpet", "pouet");
        sounds.put("violin", "gzi-gzi");
        if (args.length > 0) {
            sound = sounds.get(args[0]);
        }

        while (true){
            try (DatagramSocket socket = new DatagramSocket()) {
                try {
                    // Create Message object
                    Message message = new Message(uuid, sound);

                    // Convert message object to JSON object
                    String json = new Gson().toJson(message);
                    LOG.info(json);
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