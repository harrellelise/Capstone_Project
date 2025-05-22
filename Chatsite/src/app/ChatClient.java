package app;

import java.io.*;     
import java.net.*;    



public class ChatClient {
	
    private Socket socket = null;
    private BufferedReader inputConsole = null;  // reads user input from the console
    private PrintWriter out = null;              // sends data to the server
    private BufferedReader in = null;            // receives data from the server
    private String username;  					// saves the user's name

    
    public ChatClient(String address, int port) {
        try {
            // connect to the server using the provided address and port
            socket = new Socket(address, port);
            System.out.println("Connection Established!"); // message output to the user 
            
            
            // THIS IS THE BASIS OF BEING ABLE TO CREATE AND SEND MESSAGES
            // create/set the console input reader
            inputConsole = new BufferedReader(new InputStreamReader(System.in));
            // create/set the output stream to send data to the server
            out = new PrintWriter(socket.getOutputStream(), true);
            // create/set the input stream to receive data from the server
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            
            // read the prompt from the server and display it
            System.out.print(in.readLine()); 
            username = inputConsole.readLine();
            out.println(username);

            // start a thread to continuously read messages from the server
            new Thread(() -> {
                try {
                    String serverMsg;
                    // continuously read messages from the server
                    while ((serverMsg = in.readLine()) != null) {
                        if (serverMsg.equalsIgnoreCase("Server is shutting down. You will be disconnected.")) {
                            System.out.print("\nServer has shut down. Exiting...");
                            System.exit(0); // forcefully close the client
                        }
                        if(serverMsg.equalsIgnoreCase("Your account has been deleted. Bye!")) {
                        	System.out.print(serverMsg);
                            System.exit(0);
                        }
                        // move the cursor to the beginning of the line and clear it
                        System.out.print("\r" + " ".repeat(50) + "\r");
                        // print the incoming message on its own line
                        System.out.println(serverMsg);
                        // reprint the prompt so the user knows it's their turn to type
                        System.out.print(" - ");
                       //got rid of username here since we couldn't update it, was originally (username + " ")
                    }
                } catch (IOException e) {
                    System.out.print("Disconnected from server. Exiting...");
                    System.exit(0); // Close client on disconnect
                }
            }).start();


            // Main thread: read user input from user and send to server, to send to all other users
            String userInput;
            while (true) {
            	System.out.print(" - ");
            	//got rid of username here since we couldn't update it
                userInput = inputConsole.readLine();

                // checks if the connection to the server is still open
                if (socket.isClosed() || !socket.isConnected()) {
                    System.out.println("Disconnected from server. Exiting...");
                    break; // breaks loop/disconnects if there's no connection
                }// this is to prevent users from being able to send messages once the server closes
                
                // checks and skips empty messages
                if (userInput == null || userInput.trim().isEmpty()) {
                    continue;
                }

                if (userInput.equalsIgnoreCase("/exit")) {
                    out.println("/exit");
                    break;
                }
                // send only the message (server will add the username)
                out.println(userInput);
            }

            // close the socket and all streams
            socket.close();
            inputConsole.close();
            out.close();
            in.close();
        } catch (UnknownHostException u) {
            // handle exception when the host is unknown
            System.out.println("Host unknown: " + u.getMessage());
        } catch (IOException i) {
            // handle general I/O exceptions
            System.out.println("Unexpected exception: " + i.getMessage());
        }
    }

    public static void main(String[] args) {
    	String serverAddress = "localhost"; //Can change to the ip_address of the computer running the server
        int serverPort = 8000;
        new ChatClient(serverAddress, serverPort); //localhost/server. 
    }
    
}
