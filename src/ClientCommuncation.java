
import java.text.SimpleDateFormat;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

/*
 This call is Socket Server helper Class. It encapsulates the client functionality on the Server Side.

 */
class ClientCommunication extends Thread {

    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");

    private int maxClientsCount;
    private String currentClientName = null;
    private BufferedReader is = null;
    private PrintStream os = null;
    private Socket clientSocket = null;
    private final ClientCommunication[] clientThreads;


    public ClientCommunication(Socket clientSocket, ClientCommunication[] threads) {
        maxClientsCount = threads.length;
        this.clientSocket = clientSocket;
        this.clientThreads = threads;
    }

    public void run() {
        int maxClientsCount = this.maxClientsCount;
        ClientCommunication[] threads = this.clientThreads;

        try {
            //Create input  stream for this client.
            is = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            //  Create output streams for this client.
            os = new PrintStream(clientSocket.getOutputStream());
            String clientName;
                os.println("Welcome to Chat Server. Please enter Enter your name.");
                clientName = is.readLine().trim();

            os.println("Welcome " + clientName
                    + " to our chat room.\nTo leave enter /quit in a new line.");
            synchronized (this) {
                for (int i = 0; i < maxClientsCount; i++) {
                    if (threads[i] != null && threads[i] == this) {
                        currentClientName = "@" + clientName;
                        break;
                    }
                }
                for (int i = 0; i < maxClientsCount; i++) {
                    if (threads[i] != null && threads[i] != this) {
                        threads[i].os.println("*** A new user " + clientName + " entered the chat room !!! ***");
                    }
                }
            }
            /* Start the chatting. */
            while (true) {
                String line = is.readLine();
                if (line.startsWith("/quit")) {
                    break;
                }
                 else {
                    /* Message sent to all client */
                    synchronized (this) {
                        for (int i = 0; i < maxClientsCount; i++) {
                            if (threads[i] != null && threads[i].currentClientName != null) {
                                String timeStamp = sdf.format(System.currentTimeMillis());
                                threads[i].os.println("[" + clientName + "] " + "[" + timeStamp + "] " + line);
                            }
                        }
                    }
                }
            }
            synchronized (this) {
                for (int i = 0; i < maxClientsCount; i++) {
                    if (threads[i] != null && threads[i] != this
                            && threads[i].currentClientName != null) {
                        threads[i].os.println("user [" + clientName + "] is exiting the chat");
                    }
                }
            }
            os.println("logout" + clientName + " has Exited");

            /*
             * Clean up. Set the current thread variable to null so that a new client
             * could be accepted by the server.
             */
            synchronized (this) {
                for (int i = 0; i < maxClientsCount; i++) {
                    if (threads[i] == this) {
                        threads[i] = null;
                    }
                }
            }
            /*
             * Close the output stream, close the input stream, close the socket.
             */
            is.close();
            os.close();
            clientSocket.close();
        } catch (IOException e) {
        }
    }
}