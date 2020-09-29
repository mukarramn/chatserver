import java.io.PrintStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class ChatClient  implements Runnable {

    private static PrintStream os = null; // Output stream to write to socket
    private static BufferedReader is = null; // Input Stream to read from Socket
    private static BufferedReader inputLine = null; // User Input from Standard Input
    private static boolean isShutDown = false; // for shutting down communication with Socket Server
    private static Socket clientSocket = null; // Client Socket

    /*
     * Thread to read from the server.
     */
    @Override
    public void run() {
        /*
         * reading from the socket till we receive "logout"
         * from the server.
         */
        String responseLine;
        try {
            while ((responseLine = is.readLine()) != null) {
                System.out.println(responseLine);
                if (responseLine.indexOf("over logout") != -1)
                    break;
            }
            isShutDown = true;
        } catch (IOException e) {
            System.err.println("Input/Output Exception:  " + e);
        }
    }

    public static void main(String[] args) {


        String host = "localhost";// The default host.
        int portNumber = 2229;  // default port number.

        if (args.length < 2) {
            System.out.println("Run Command: java ChatClient [host] [portNumber] " + "... Currently using host=" + host + ", portNumber=" + portNumber);
        } else {
            host = args[0];
            portNumber = Integer.valueOf(args[1]).intValue();
        }

        /*
         * Open a socket on a given host and port. Open input and output streams.
         */
        try {
            // getting host ip
            InetAddress ip = InetAddress.getByName(host);

            clientSocket = new Socket(ip, portNumber);
            inputLine = new BufferedReader(new InputStreamReader(System.in));
            os = new PrintStream(clientSocket.getOutputStream());

            is = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        } catch (UnknownHostException e) {
            System.err.println("Unknown Host " + host);
        } catch (IOException e) {
            System.err.println("No I/O connection to host " + host);
        }

        /*
         * If everything has been initialized then we want to write some data to the
         * socket we have opened a connection to on the port portNumber.
         */
        if (clientSocket != null && os != null && is != null) {
            try {

                /* Create a thread to read from the server. */
                new Thread(new ChatClient()).start();

                while (!isShutDown) {
                    os.println(inputLine.readLine().trim());
                }


                os.close(); // Close  output stream
                is.close(); //close  input stream
                clientSocket.close(); // close  socket.
            } catch (IOException e) {
                System.err.println(" IOException::  " + e);
            }
        }
    }
}