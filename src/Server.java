import java.io.ByteArrayOutputStream;
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

	public static final int HOST_SERVER_PORT_NUM = 69;
	public boolean valid = true;
	private int counter = 0;
	private byte[] data;
	
	public static void main(String[] args) {
		new Server();
	}
	
	private DatagramPacket hostServerPacket, serverHostPacket;
	private DatagramSocket hostServerSocket;
	
	public Server() {
		run();
	}

	private void run() {
		createSockets();
		while (valid) {
			receivePacket();
			sendPacket();
			counter++;
		}
		hostServerSocket.close();
		System.out.println(this.getClass().getName() + ": Program completed.");
	}
	
	/**
	 * 
	 */
	public void createSockets() {
		try {
			hostServerSocket = new DatagramSocket(HOST_SERVER_PORT_NUM);
		} catch (SocketException se) {
			se.printStackTrace();
			System.exit(1);
		}
	}
	
	public void receivePacket() {
		byte[] data = new byte[100];
		hostServerPacket = new DatagramPacket(data, data.length);
		try {
			System.out.println(this.getClass().getName() + ": Waiting...\n");
			hostServerSocket.receive(hostServerPacket);
			printPacketContent(hostServerPacket, "received", counter);
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
		byte[] data = null;
		if (verifyMessage(hostServerPacket)) {
			data = createResponse(hostServerPacket);
		} else {
			data = "INVALID_REQUEST".getBytes();
		}
		serverHostPacket = new DatagramPacket(
				data, 
				data.length, 
				hostServerPacket.getAddress(), 
				hostServerPacket.getPort());
		try {
			hostServerSocket.send(serverHostPacket);
			printPacketContent(serverHostPacket, "sending", counter);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
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
	
	/**
	 * Verify the encoded the message specified from the Client.
	 * @param receivePacket, DatagramPacket
	 * @return byte[], the message
	 */
	private boolean verifyMessage(DatagramPacket receivePacket) {
		data = receivePacket.getData();
		int index = 0;
		while (index < receivePacket.getLength()) {
			if (index == 0) {
				if (data[index] == 0x00) {
					index++;
				} else {
					break;
				}
			}
			if (index == 1) {
				if (data[index] == 0x01) {
					// Read request
					index++;
				} else if (data[index] == 0x02) {
					// Write request
					index++;
				} else {
					break;
				}
			}
			if (index > 1) {
				if (data[index] > 0x20 && data[index] < 0x7F) {
					index++;
				} else if (data[index] == 0x00) {
					index++;
					if (data[index] > 0x20 && data[index] < 0x7F) {
						index++;
					} else if (data[index] == 0x00) {
						if (index == receivePacket.getLength()) {
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
		String invalid = new String(receivePacket.getData(), 0, receivePacket.getLength());
		System.err.println(invalid);
		valid = false;
		return false;
	}
	
	private byte[] createResponse(DatagramPacket receivePacket) {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		data = receivePacket.getData();
		if (data[1] == 0x01) {
			// Read request
			os.write(0x00);
			os.write(0x03);
			os.write(0x00);
			os.write(0x01);
		} else if (data[1] == 0x02) {
			// Write request
			os.write(0x00);
			os.write(0x04);
			os.write(0x00);
			os.write(0x00);
		}
		return os.toByteArray();
	}
}
