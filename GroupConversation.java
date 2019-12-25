package groupConversion;

import java.net.*;
import java.io.*;
import java.util.*;
 
/**
 * This class make use of java networking to communicate over network.
 * @author Rahul kumar
 * @version 1.0
 */

public class GroupConversation {
        private static final String TERMINATE = "Exit";
        static String name;
        static volatile boolean finished = false;
        
        /**
         * This method demonstrate all methods. It creates a InetAddress, socket and make use of java.net package.
         * @param args Unused
         */
        public static void main(String[] args) {
               if (args.length != 2)
                       System.out.println("Two arguments required: <multicast-host> <port-number>");
               else {
                       try {
                             InetAddress group = InetAddress.getByName(args[0]);
                             int port = Integer.parseInt(args[1]);
                             Scanner sc = new Scanner(System.in);
                             System.out.print("Enter your name: ");
                             name = sc.nextLine();
                             MulticastSocket socket = new MulticastSocket(port);
                             socket.setTimeToLive(0);
                             socket.joinGroup(group);
                             Thread t = new Thread(new ReadThread(socket, group, port));
                             t.start();
                             System.out.println("Start typing messages...n");
                             while (true) {
                                    String message;
                                    message = sc.nextLine();
                                    if (message.equalsIgnoreCase(GroupConversation.TERMINATE)) {
                                          finished = true;
                                          socket.leaveGroup(group);
                                          socket.close();
                                          break;
                                    }
                                    message = name + ": " + message;
                                    byte[] buffer = message.getBytes();
                                    DatagramPacket datagram = new DatagramPacket(buffer, buffer.length, group, port);
                                    socket.send(datagram);
                             }
                       } 
                       catch (SocketException se) {
                             System.out.println("Error creating socket");
                             se.printStackTrace();
                       } 
                       catch (IOException ie) {
                             System.out.println("Error reading/writing from/to socket");
                             ie.printStackTrace();
                       }
               }
        }
}

/**
 * This class implements Runnable and use multithreading for group conversion
 */
class ReadThread implements Runnable {
         private MulticastSocket socket;
         private InetAddress group;
         private int port;
         private static final int MAX_LEN = 1000;
         
         /**
          * This constructor initialize private variables/instances of this class
          * @param socket is a MulticastSocket
          * @param group is a InetAddress
          * @param port is a integer
          */
         ReadThread(MulticastSocket socket, InetAddress group, int port) {
                this.socket = socket;
                this.group = group;
                this.port = port;
         }
         
         @Override
          public void run() {
                while (!GroupConversation.finished) {
                        byte[] buffer = new byte[ReadThread.MAX_LEN]; 
                        DatagramPacket datagram = new DatagramPacket(buffer, buffer.length, group, port);
                        String message;
                try {
                        socket.receive(datagram);
                        message = new String(buffer, 0, datagram.getLength(), "UTF-8");
                        if (!message.startsWith(GroupConversation.name))
                                System.out.println(message);
                        } 
                        catch (IOException e) {
                                System.out.println("Socket closed!");
                        }
                 }
          }
}
