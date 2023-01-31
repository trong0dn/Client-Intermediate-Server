import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

/**
 * 
 */

/**
 * 
 * @author Trong Nguyen
 * @version 1.0
 * @date 11-02-2023
 */
public class Server {

	public static final int SERVER_RECEIVE_PORT_NUM = 69;
	
	public static void main(String[] args) {
		new Server();
	}
	
	private DatagramPacket sendPacket, receivePacket;
	private DatagramSocket sendSocket, receiveSocket;
	
	byte[] data;
	
	public Server() {
		init();
	}

	private void init() {
		createSockets();
		receivePacket();
		sendPacket();
		closeSocket();
	}
	
	/**
	 * 
	 */
	public void createSockets() {
		try {
			sendSocket = new DatagramSocket();
			receiveSocket = new DatagramSocket(SERVER_RECEIVE_PORT_NUM);
		} catch (SocketException se) {
			se.printStackTrace();
			System.exit(1);
		}
	}
	
	public void receivePacket() {
		// Construct a DatagramPacket for receiving packets up 
	    // to 100 bytes long (the length of the byte array).
		byte[] data = new byte[100];
		receivePacket = new DatagramPacket(data, data.length);
		// Block until a datagram packet is received from receiveSocket.
		try {
			System.out.println(this.getClass().getName() + ": Waiting...\n");
			receiveSocket.receive(receivePacket);
			printPacketContent(receivePacket, "received");
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}	
	
	/**
	 * Verify the encoded the message specified from the Client.
	 * @param receivePacket, DatagramPacket
	 * @return byte[], the message
	 */
	private byte[] verifyMessage(DatagramPacket receivePacket) {
		data = receivePacket.getData();
		return data;
	}
	
	/**
	 * Create a new datagram packet containing the string received from the client.
	 * Construct a datagram packet that is to be sent to a specified port 
	 * on a specified host.
	 */
	public void sendPacket() {
		data = verifyMessage(receivePacket);
		sendPacket = new DatagramPacket(data, 
				receivePacket.getLength(), 
				receivePacket.getAddress(), 
				receivePacket.getPort());
		try {
			// Send the datagram packet to the client via the send socket.
			sendSocket.send(sendPacket);
			printPacketContent(receivePacket, "sending");
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	/**
	 * Close the socket.
	 */
	public void closeSocket() {
		sendSocket.close();
		receiveSocket.close();
	}
	
	/**
	 * Prints out the information it has put in the packet 
	 * both as a String and as bytes. 
	 * @param receivePacket, DatagramPacket
	 * @param direction, received or sending
	 */
	public void printPacketContent(DatagramPacket receivePacket, String direction)  {
	    System.out.println(this.getClass().getName() + ": Packet " + direction);
	    System.out.println("Address: " + receivePacket.getAddress());
	    System.out.println("Host port: " + receivePacket.getPort());
	    int len = receivePacket.getLength();
	    System.out.println("Length: " + len);
	    System.out.print("Containing: ");
	    String packetStr = new String(receivePacket.getData(), 0, len);   
	    System.out.println(packetStr + "\n");
	      
	    // Slow things down (wait 1 seconds)
	    try {
	        Thread.sleep(1000);
	    } catch (InterruptedException e ) {
	        e.printStackTrace();
	        System.exit(1);
	    }
	}
}
