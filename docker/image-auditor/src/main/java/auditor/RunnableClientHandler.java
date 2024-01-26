package auditor;

import java.io.*;
import java.net.*;
import static java.nio.charset.StandardCharsets.*;

public class RunnableClientHandler implements Runnable {
    final Socket socket;
    final Worker worker;

    public RunnableClientHandler(Socket socket, Worker worker){
        this.socket = socket;
        this.worker = worker;
    }

    public void run(){
        try(socket; // Not strictly necessary, but this is allowed.
            var in = new BufferedReader(new InputStreamReader(socket.getInputStream(), UTF_8))) {

            System.out.println("Client connected");

            StringBuilder request = new StringBuilder();
            String line;
            while((line = in.readLine()) != null && line.length() != 0) {
                request.append(line);
            }
            String response = worker.work(request.toString());

        } catch (IOException e) {
            System.err.println("Error in ClientHandler: " + e.getMessage());
        }
    }
}
