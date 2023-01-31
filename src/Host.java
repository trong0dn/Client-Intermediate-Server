import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

/**
 * 
 */

/**
 * @author Trong Nguyen
 * @version 1.0
 * @date 11-02-2023
 */
public class Host {
	
	private static final int HOST_RECEIVE_PORT_NUM = 23;
	private static final int HOST_SEND_RECEIVE_PORT_NUM = 69;

	public static void main(String[] args) {
		new Host();
	}
	
	private DatagramPacket sendReceivePacket, receivePacket;
	private DatagramSocket sendReceiveSocket, receiveSocket;
	
	byte[] data;
	
	public Host() {
		init();
	}

	private void init() {
		createSockets();
		receivePacket();
		sendPacket();
		closeSocket();
	}

	
	/**
	 * Constructs datagram sockets on the local host machine. 
	 * These sockets will be used to send UDP Datagram packets.
	 */
	public void createSockets() {
		try {
			sendReceiveSocket = new DatagramSocket();
			receiveSocket = new DatagramSocket(HOST_RECEIVE_PORT_NUM);
		} catch (SocketException se) {
			se.printStackTrace();
			System.exit(1);
		}
	}
	
	/**
	 * Construct a DatagramPacket for receiving packets up 
	 * to 100 bytes long (the length of the byte array).
	 */
	public void receivePacket() {
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
	 * Create a new datagram packet containing the string received from the client.
	 * Construct a datagram packet that is to be sent to a specified port 
	 * on a specified host.
	 */
	public void sendPacket() {
		data = receivePacket.getData();
		sendReceivePacket = new DatagramPacket(data, 
				receivePacket.getLength(), 
				receivePacket.getAddress(), 
				HOST_SEND_RECEIVE_PORT_NUM);
		try {
			// Send the datagram packet to the client via the send socket.
			sendReceiveSocket.send(sendReceivePacket);
			printPacketContent(sendReceivePacket, "sending");
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	/**
	 * Close the socket.
	 */
	public void closeSocket() {
		sendReceiveSocket.close();
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
