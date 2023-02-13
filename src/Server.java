import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * This class represents a Server.
 * @author Trong Nguyen
 * @version 2.0
 * @date 04/03/2023
 */
public class Server implements Runnable {

	public static final int SERVER_PORT_NUM = 69;
	private static final int TIMEOUT = 5000;

	private DatagramSocket dataSocket, ackSocket;
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
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		try {
			dataSocket = new DatagramSocket();
			ackSocket = new DatagramSocket();
			dataSocket.setSoTimeout(TIMEOUT);
			ackSocket.setSoTimeout(TIMEOUT);
			while (true) {
				counter++;
				send();
				DatagramPacket reply = receiveData();
				sendAck(reply);
				receive();
				System.out.println("--------------------------------------");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			dataSocket.close();
			ackSocket.close();
			System.err.println(this.getClass().getName() + ": Program terminated.");
		}
	}
	
	/**
	 * Send request for data to Intermediate host.
	 */
	private void send() {
		data = (this.getClass().getName() + " - Requesting data").getBytes();
		try {
			DatagramPacket sendHostPacket = new DatagramPacket(
					data, 
					data.length, 
					InetAddress.getLocalHost(), 
					SERVER_PORT_NUM);
			dataSocket.send(sendHostPacket);
			printPacketContent(sendHostPacket, "request data from host", counter);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	/**
	 * Receive data from Intermediate host.
	 * @return DatagramPacket, message requested from Intermediate host
	 */
	public DatagramPacket receiveData() {
		byte[] data = new byte[100];
		DatagramPacket receiveHostPacket = new DatagramPacket(data, data.length);
		try {
			System.out.println(this.getClass().getName() + ": Waiting...\n");
			dataSocket.receive(receiveHostPacket);
			printPacketContent(receiveHostPacket, "receive data from host", counter);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		return receiveHostPacket;
	}	
	
	/**
	 * Send acknowledge to Intermediate host.
	 * @param receiveHostPacket DatagramPacket, data packet received from request
	 */
	public void sendAck(DatagramPacket receiveHostPacket) {
		if (verifyMessage(receiveHostPacket)) {
			data = createResponse(receiveHostPacket);
		} else {
			String invalid = new String(receiveHostPacket.getData(), 0, receiveHostPacket.getLength());
			System.err.println(invalid);
			System.err.println(this.getClass().getName() + ": Program terminated.");
			System.exit(1);
		}
		DatagramPacket sendAckHostPacket = new DatagramPacket(
				data, 
				data.length, 
				receiveHostPacket.getAddress(), 
				receiveHostPacket.getPort());
		try {
			ackSocket.send(sendAckHostPacket);
			printPacketContent(sendAckHostPacket, "send ack to host", counter);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	/**
	 * Receive reply to acknowledge from Intermediate host.
	 */
	public void receive() {
		byte[] data = new byte[100];
		DatagramPacket receiveAckPacket = new DatagramPacket(data, data.length);
		try {
			System.out.println(this.getClass().getName() + ": Waiting...\n");
			ackSocket.receive(receiveAckPacket);
			printPacketContent(receiveAckPacket, "receive ack from host", counter);
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
