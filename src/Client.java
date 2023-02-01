import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

/**
 * This class represents a Client.
 * @author Trong Nguyen
 * @version 1.0
 * @date 11-02-2023
 */
public class Client {
	
	private static final int REPEAT_NUM = 11;
	private static final int CLIENT_HOST_PORT_NUM = 23;
	private static final String FILENAME = "test.txt";
	private static final String MODE = "octet";
	
	private DatagramSocket clientHostSocket;
	private DatagramPacket clientHostPacket, hostClientPacket;
	
	private byte[] data;
	private int sendCounter = 0;
	private int receiveCounter = 0;
	
	/**
	 * Main method for Client.
	 * @param args, default parameters
	 */
	public static void main(String[] args) {
		new Client();
	}
	
	/**
	 * Constructor for Client.
	 */
	public Client() {
		run();
	}
	
	/**
	 * Run Client methods.
	 */
	private void run() {
		createSocket();
		// repeat the following 11 times
		while (sendCounter < REPEAT_NUM) {
			sendHostPacket();
			sendCounter++;
		}
		while (receiveCounter < REPEAT_NUM) {
			receiveHostPacket();
			receiveCounter++;
		}
		clientHostSocket.close();
		System.out.println(this.getClass().getName() + ": Program completed.");
	}
	
	/**
	 * Construct a datagram socket and bind it to any available
	 * port on the local host machine. This socket will be used to
	 * send and receive UDP Datagram packets.
	 */
	private void createSocket() {
		try {
			// creates a DatagramSocket to use to both send and receive
			clientHostSocket = new DatagramSocket();
		} catch (SocketException se) {
			se.printStackTrace();
			System.exit(1);
		}
	}
	
	/**
	 * Send the datagram packet to the host via the DatagramSocket to Host.
	 */
	private void sendHostPacket() {
		try {
			data = encodeMessage();
			// creates a DatagramPacket
			clientHostPacket = new DatagramPacket(data, data.length, InetAddress.getLocalHost(), CLIENT_HOST_PORT_NUM);
			// sends the packet to a well­ known port: 23 on the intermediate host the client waits 
			// on its DatagramSocket
			clientHostSocket.send(clientHostPacket);
			// prints out the information it has put in the packet 
			// (print the request both as a String and as bytes)
			printPacketContent(clientHostPacket, "sending", sendCounter);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	/**
	 * Construct a DatagramPacket for receiving packets up 
	 * to 100 bytes long (the length of the byte array).
	 */
	private void receiveHostPacket() {
		try {
			data = new byte[100];
			hostClientPacket = new DatagramPacket(data, data.length);
			// receives a DatagramPacket from the intermediate host
			clientHostSocket.receive(hostClientPacket);
			// prints out the information received, including the byte array
			printPacketContent(hostClientPacket, "received", receiveCounter);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	/**
	 * Encodes the message specified for Client.
	 * @return byte[], message
	 * @throws IOException
	 */
	private byte[] encodeMessage() throws IOException {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		// the packet is either a "read request" or a "write request" 
		// (alternate between read and write requests, five each) 
		if (sendCounter % 2 == 0) {
			// read request format
			// first two bytes are 0 and 1 (these are binary, not text)
			os.write(0x00); 
			os.write(0x01); 
		} else {
			// write request format
			// just like a read request, except it starts with 0 2 instead of 0 1
			os.write(0x00);
			os.write(0x02);
		}
		os.write(Integer.toString(sendCounter).getBytes());
		// then a filename converted from a string to bytes
		os.write(FILENAME.getBytes());
		// then a 0 byte
		os.write(0x00);
		// then a mode (netascii or octet, any mix of cases, e.g. ocTEt) 
		// converted from a string to bytes
		os.write(MODE.getBytes());
		// finally another 0 byte (and nothing else after that)
		os.write(0x00);
		byte[] message = os.toByteArray();
		os.flush();
		if (sendCounter == REPEAT_NUM - 1) {
			message = "INVALID_REQUEST".getBytes();
			System.err.println(String.format("INVALID_REQUEST"));
		}
		return message;
	}
	
	/**
	 * Prints out the information it has put in the packet 
	 * both as a String and as bytes. 
	 * @param packet, DatagramPacket
	 * @param direction, String (i.e. received or sending)
	 * @param counter, int to keep count of the packets
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
