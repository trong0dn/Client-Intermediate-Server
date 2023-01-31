import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

/**
 * 
 */

/**
 * This class represents a Client.
 * @author Trong Nguyen
 * @version 1.0
 * @date 11-02-2023
 */
public class Client {
	
	private static final int CLIENT_SEND_PORT_NUM = 23;
	
	private static final int REPEAT_NUM = 10;
	private static final String FILENAME = "filename.txt";
	
	private DatagramSocket sendReceiveSocket;
	private DatagramPacket sendPacket;
	private DatagramPacket receivePacket;
	
	byte[] data;
	
	/**
	 * Main method for Client.
	 * @param args
	 */
	public static void main(String[] args) {
		new Client();
	}
	
	/**
	 * Constructor for Client.
	 */
	public Client() {
		init();
	}
	
	/**
	 * Initialize Client methods.
	 */
	private void init() {
		sendReceiveSocket();
		for (int i = 0; i < REPEAT_NUM; i++) {
			createSendPacket();
			sendPacket();
		}
		receivePacket();
		closeSocket();
	}
	
	/**
	 * Construct a datagram socket and bind it to any available
	 * port on the local host machine. This socket will be used to
	 * send and receive UDP Datagram packets.
	 */
	private void sendReceiveSocket() {
		try {
			sendReceiveSocket = new DatagramSocket();
		} catch (SocketException se) {
			se.printStackTrace();
			System.exit(1);
		}
	}
	
	/**
	 * Encodes the message specified for Client.
	 * @return byte[], the message
	 * @throws IOException
	 */
	private byte[] encodeMessage() throws IOException {
		byte zero = (byte) 0;
		byte one = (byte) 1;
		byte two = (byte) 2;
		byte[] filename = FILENAME.getBytes();
		byte[] netascii = "netascii".getBytes();
		byte[] octet = "octet".getBytes();
	
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		for (int i = 0; i < REPEAT_NUM; i++) {
			if (i == 10) {
				System.out.println("invalid");
			} else if (i % 2 == 0) {
				// Read request format
				os.write(zero); 
				os.write(one); 
			} else {
				// Write request format
				os.write(zero);
				os.write(two);
			}
		}
		os.write(filename);
		os.write(zero);
		if (randomSelection(0, 1) == 0) {
			os.write(octet);
		} else {
			os.write(netascii);
		}
		os.write(zero);

		return os.toByteArray();
	}
	
	/**
	 * Randomly chooses a number between a range.
	 * @return random integer within minimum and maximum range
	 */
	public int randomSelection(int min, int max) {
		int range = (max - min) + 1;
		return (int)(Math.random() * range) + min;
	}
	
	/**
	 * Prepare a DatagramPacket and send it via sendReceiveSocket
	 * to port on the destination host.
	 * @throws IOException 
	 */
	public void createSendPacket() {
		try {
			byte[] msg = encodeMessage();
			sendPacket = new DatagramPacket(msg, msg.length, InetAddress.getLocalHost(), CLIENT_SEND_PORT_NUM);
			printPacketContent(sendPacket, "sending");
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	/**
	 * Send the datagram packet to the host via the send/receive socket.
	 */
	public void sendPacket() {
		try {
			sendReceiveSocket.send(sendPacket);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	/**
	 * Construct a DatagramPacket for receiving packets up 
	 * to 100 bytes long (the length of the byte array).
	 */
	public void receivePacket() {
		data = new byte[100];
		receivePacket = new DatagramPacket(data, data.length);
		try {
			// Block until a datagram is received via sendReceiveSocket. 
			sendReceiveSocket.receive(receivePacket);
			printPacketContent(sendPacket, "received");			
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
	}
	
	/**
	 * Prints out the information it has put in the packet 
	 * both as a String and as bytes. 
	 * @param receivePacket, DatagramPacket
	 * @param direction, received or sending
	 */
	private void printPacketContent(DatagramPacket sendPacket, String direction) {
		System.out.println(this.getClass().getName() + ": Packet " + direction);
	    System.out.println("To: " + sendPacket.getAddress());
	    System.out.println("Destination port: " + sendPacket.getPort());
	    int len = sendPacket.getLength();
	    System.out.println("Length: " + sendPacket.getLength());
	    System.out.print("Containing: ");
	    String packetStr = new String(sendPacket.getData(), 0, len);
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
