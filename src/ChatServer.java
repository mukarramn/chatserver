import java.io.PrintStream;
import java.io.IOException;
import java.net.Socket;
import java.net.ServerSocket;

/*
 * A Chat Server with following Features
 * Connect a client to the server
 * Send a message to the server
 * The server relays messages to all connected clients,
 * including a timestamp and
 * name of the client sending the message
 */
public class ChatServer {

    private static ServerSocket serverSocket = null; //server socket.
    private static Socket clientSocket = null; // Client Socket

    private static final int maxClientsCount = 10; // maximum client connections
    private static final ClientCommunication[] threads = new ClientCommunication[maxClientsCount];

    public static void main(String args[]) {

        // The Server binds to the localhost and port number
        int portNumber = 2229;  // default port number
        if (args.length < 1) {
            System.out.println("Run Command: java ChatServer [portNumber] " + "... using port number=" + portNumber);
        } else {
            portNumber = Integer.valueOf(args[0]).intValue();
        }


        try {
            serverSocket = new ServerSocket(portNumber);
        } catch (IOException e) {
            System.out.println(e);
        }

        /*
         * Create a client socket for each connection and pass it to a new client
         * thread.
         */
        while (true) {
            try {
                clientSocket = serverSocket.accept();
                int i = 0;
                for (i = 0; i < maxClientsCount; i++) {
                    if (threads[i] == null) {
                        (threads[i] = new ClientCommunication(clientSocket, threads)).start();
                        break;
                    }
                }
                if (i == maxClientsCount) {
                    PrintStream os = new PrintStream(clientSocket.getOutputStream());
                    os.println("Reached Maximum Number of Clients. Try Later");
                    os.close();
                    clientSocket.close();
                }
            } catch (IOException e) {
                System.out.println(e);
            }
        }
    }
}
