import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

/**
 * This class represents a Server.
 * @author Trong Nguyen
 * @version 1.0
 * @date 11-02-2023
 */
public class Server {

	public static final int HOST_SERVER_PORT_NUM = 69;
	
	private DatagramSocket hostServerSocket;
	private DatagramPacket hostServerPacket, serverHostPacket;
	
	private byte[] data;
	private int counter = 0;

	/**
	 * Main method for Server.
	 * @param args, default parameters
	 */
	public static void main(String[] args) {
		new Server();
	}
	
	/**
	 * Constructor for Server.
	 */
	public Server() {
		run();
	}

	/**
	 * Run Server methods.
	 */
	private void run() {
		createSocket();
		// repeat the following "forever"
		while (true) {
			receivePacket();
			sendPacket();
			counter++;
		}
	}
	
	/**
	 * Construct a datagram socket and bind it to any available
	 * port on the local host machine. This socket will be used to
	 * send and receive UDP Datagram packets.
	 */
	public void createSocket() {
		try {
			// creates a DatagramSocket to use to receive (port 69)
			hostServerSocket = new DatagramSocket(HOST_SERVER_PORT_NUM);
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
		hostServerPacket = new DatagramPacket(data, data.length);
		try {
			// waits to receive a request
			System.out.println(this.getClass().getName() + ": Waiting...\n");
			hostServerSocket.receive(hostServerPacket);
			//  prints out the information it has received 
			// (print the request both as a String and as bytes)
			printPacketContent(hostServerPacket, "received", counter);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}	
	
	/**
	 * Create a new datagram packet containing the string received from the Host.
	 * Construct a datagram packet that is to be sent to a specified port 
	 * on a specified host.
	 */
	public void sendPacket() {
		byte[] data = null;
		// the packet should be either a "read request" or a "write request"
		if (verifyMessage(hostServerPacket)) {
			data = createResponse(hostServerPacket);
		} else {
			System.err.println(this.getClass().getName() + ": Program terminated.");
			System.exit(1);
		}
		serverHostPacket = new DatagramPacket(
				data, 
				data.length, 
				hostServerPacket.getAddress(), 
				hostServerPacket.getPort());
		try {
			// creates a DatagramSocket to use just for this response
			DatagramSocket serverHostSocket = new DatagramSocket();
			// sends the packet via the new socket to the port it received the request from 
			serverHostSocket.send(serverHostPacket);
			// prints out the response packet information
			printPacketContent(serverHostPacket, "sending", counter);
			// closes the socket it just created
			serverHostSocket.close();
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
	private boolean verifyMessage(DatagramPacket receivePacket) {
		// the server should parse the packet to confirm that the format is valid
		data = receivePacket.getData();
		int index = 0;
		while (index < receivePacket.getLength()) {
			if (index == 0) {
				if (data[index] == 0x00) {
					// starts with byte 0
					index++;
				} else {
					break;
				}
			}
			if (index == 1) {
				if (data[index] == 0x01) {
					// read request, second byte 1
					index++;
				} else if (data[index] == 0x02) {
					// write request, second byte 2
					index++;
				} else {
					break;
				}
			}
			if (index > 1) {
				if (data[index] > 0x20 && data[index] < 0x7F) {
					// some text
					index++;
				} else if (data[index] == 0x00) {
					// followed by byte 0
					index++;
					if (data[index] > 0x20 && data[index] < 0x7F) {
						// some text
						index++;
					} else if (data[index] == 0x00) {
						if (index == receivePacket.getLength()) {
							// ends with byte 0
							return true;
						}
					} else {
						break;
					}
				} else {
					break;
				}
			} else {
				break;
			}
		}
		return false;
	}
	
	/**
	 * Create response to Host when receive packet is valid.
	 * @param receivePacket, DatagramPacket
	 * @return byte[], message
	 */
	private byte[] createResponse(DatagramPacket receivePacket) {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		data = receivePacket.getData();
		if (data[1] == 0x01) {
			// read request, sends back 0 3 0 1 (exactly four bytes)
			os.write(0x00);
			os.write(0x03);
			os.write(0x00);
			os.write(0x01);
		} else if (data[1] == 0x02) {
			// write request, sends back 0 4 0 0 (exactly four bytes)
			os.write(0x00);
			os.write(0x04);
			os.write(0x00);
			os.write(0x00);
		} else {
			// if the packet is invalid, the server throws an exception and quits
			String invalid = new String(receivePacket.getData(), 0, receivePacket.getLength());
			System.err.println(invalid);
		}
		return os.toByteArray();
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
