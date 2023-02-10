import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

/**
 * This class represents an Intermediate Host.
 * @author Trong Nguyen
 * @version 1.0
 * @date 11-02-2023
 */
public class Intermediate {
	
	private static final int CLIENT_HOST_PORT_NUM = 23;
	private static final int HOST_SERVER_PORT_NUM = 69;
	private static final int TIMEOUT = 10000;

	private DatagramSocket clientHostSocket, hostServerSocket;
	private DatagramPacket clientHostPacket, hostServerPacket, serverHostPacket, hostClientPacket;
	
	private byte[] data;
	private int counter = 0;
	
	/**
	 * Main method for Host.
	 * @param args, default parameters
	 */
	public static void main(String[] args) {
		new Intermediate();
	}
	
	/**
	 * Constructor for Host.
	 */
	public Intermediate() {
		run();
	}

	/**
	 * Run Host methods.
	 */
	private void run() {
		createSocket();
		try {
			// repeat the following "forever"
			while (true) {
				receiveClientPacket();
				sendServerPacket();
				receiveServerPacket();
				sendClientPacket();
				counter++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		clientHostSocket.close();
		hostServerSocket.close();
	}

	/**
	 * Constructs datagram sockets on the local host machine. 
	 * These sockets will be used to send and receive UDP Datagram packets.
	 */
	public void createSocket() {
		try {
			// creates a DatagramSocket to use to receive (port 23) 
			clientHostSocket = new DatagramSocket(CLIENT_HOST_PORT_NUM);
			clientHostSocket.setSoTimeout(TIMEOUT);
			// creates a DatagramSocket to use to send and receive
			hostServerSocket = new DatagramSocket();
			hostServerSocket.setSoTimeout(TIMEOUT);
		} catch (SocketException se) {
			se.printStackTrace();
			System.exit(1);
		}
	}
	
	/**
	 * Construct a DatagramPacket for receiving packets up 
	 * to 100 bytes long (the length of the byte array).
	 */
	public void receiveClientPacket() {
		data = new byte[100];
		try {
			clientHostPacket = new DatagramPacket(data, data.length);
			System.out.println(this.getClass().getName() + ": Waiting...\n");
			// waits to receive a request
			clientHostSocket.receive(clientHostPacket);
			// prints out the information it has received 
			// (print the request both as a String and as bytes)
			printPacketContent(clientHostPacket, "received", counter);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	/**
	 * Create a new datagram packet containing the string received from the Client.
	 * Construct a datagram packet that is to be sent to a specified port 
	 * on a specified host.
	 */
	public void sendServerPacket() {
		// forms a packet to send containing exactly what it received
		hostServerPacket = new DatagramPacket(
				clientHostPacket.getData(),
				clientHostPacket.getLength(), 
				clientHostPacket.getAddress(), 
				HOST_SERVER_PORT_NUM);
		try {
			// sends this packet on its send/receive socket to port 69 it waits to receive a response
			hostServerSocket.send(hostServerPacket);
			// prints out this information
			printPacketContent(hostServerPacket, "sending", counter);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	/**
	 * Construct a DatagramPacket for receiving packets up 
	 * to 100 bytes long (the length of the byte array).
	 */
	public void receiveServerPacket() {
		data = new byte[100];
		try {
			serverHostPacket = new DatagramPacket(data, data.length);
			System.out.println(this.getClass().getName() + ": Waiting...\n");
			hostServerSocket.receive(serverHostPacket);
			// prints out the information received
			printPacketContent(serverHostPacket, "received", counter);
		} catch (IOException e) {
			System.err.println("java.net.SocketTimeoutException: Receive timed out");
			System.err.println(this.getClass().getName() + ": Program terminated.");
			System.exit(1);
		}
	}
	
	/**
	 * Send the datagram packet to the host via the DatagramSocket to Server.
	 */
	public void sendClientPacket() {
		if (verifyMessage(serverHostPacket)) {
			// forms a packet to send back to the Client sending the request 
			hostClientPacket = new DatagramPacket(
					serverHostPacket.getData(),
					serverHostPacket.getLength(), 
					serverHostPacket.getAddress(), 
					clientHostPacket.getPort());
		}
		try {
			DatagramSocket sendToClientSocket = new DatagramSocket();
			// sends the request
			sendToClientSocket.send(hostClientPacket);
			// prints out the information being sent
			printPacketContent(hostClientPacket, "sending", counter);
			sendToClientSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	/**
	 * Verify message receive packet as specified for Server.
	 * @param receivePacket, DatagramPacket
	 * @return boolean, true if message is valid, otherwise false
	 */
	private boolean verifyMessage(DatagramPacket receivePacket) {
		data = receivePacket.getData();
		if (	(data[0] == 0x00) && (data[1] == 0x03) && (data[2] == 0x00) && (data[3] == 0x01)) {
			// Read request
			return true;
		} else if ((data[0] == 0x00) && (data[1] == 0x04) && (data[2] == 0x00) && (data[3] == 0x00)) {
			// Write request
			return true;
		} else {
			String invalid = new String(receivePacket.getData(), 0, receivePacket.getLength());
			System.err.println(invalid);
			return false;
		}
	}
	
	/**
	 * Prints out the information it has put in the packet 
	 * both as a String and as bytes. 
	 * @param packet, DatagramPacket
	 * @param direction, received or sending
	 */
	private void printPacketContent(DatagramPacket packet, String direction, int counter) {
		System.out.println(this.getClass().getName() + ": Packet " + counter + " " + direction);
		System.out.println("Address: " + packet.getAddress());
	    System.out.println("Port: " + packet.getPort());
	    int len = packet.getLength();
	    System.out.println("Length: " + packet.getLength());
	    System.out.print("Containing: ");
	    String packetStr = new String(packet.getData(), 0, len);
	    System.out.println(packetStr + "\n");
	    try {
	        Thread.sleep(1000);
	    } catch (InterruptedException e ) {
	        e.printStackTrace();
	        System.exit(1);
	    }
	}
}
