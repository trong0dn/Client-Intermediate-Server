import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * This class represents a Client.
 * @author Trong Nguyen
 * @version 2.0
 * @date 04/03/2023
 */
public class Client implements Runnable {
	
	private static final int REPEAT_NUM = 11;
	private static final int CLIENT_PORT_NUM = 23;
	private static final String FILENAME = "test.txt";
	private static final String MODE = "octet";
	private static final int TIMEOUT = 5000;
	
	private DatagramSocket dataSocket, ackSocket;
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
				sendCounter++;
				byte[] data = encodeMessage();
				sendData(data);
				DatagramPacket reply = receive();
				receiveCounter++;
				send(reply);
				receiveAck();
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
	 * Send data to Intermediate host.
	 * @param data byte[], client encoded message
	 */
	private void sendData(byte[] data) {
		try {
			DatagramPacket sendHostDataPacket = new DatagramPacket(
					data, 
					data.length, 
					InetAddress.getLocalHost(), 
					CLIENT_PORT_NUM);
			dataSocket.send(sendHostDataPacket);
			printPacketContent(sendHostDataPacket, "send data to host", sendCounter);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	/**
	 * Receive reply from Intermediate host.
	 * @return DatagramPacket, message received from Intermediate host
	 */
	private DatagramPacket receive() {
		byte[] data = new byte[100];
		DatagramPacket receiveHostReplyPacket = null;
		try {
			receiveHostReplyPacket = new DatagramPacket(data, data.length);
			System.out.println(this.getClass().getName() + ": Waiting...\n");
			dataSocket.receive(receiveHostReplyPacket);
			printPacketContent(receiveHostReplyPacket, "receive reply from host", sendCounter);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		return receiveHostReplyPacket;
	}
	
	/**
	 * Request acknowledge from Intermediate host.
	 * @param replyPacket DatagramPacket, message containing ack request
	 */
	private void send(DatagramPacket replyPacket) {
		byte[] data = (this.getClass().getName() + " - Request ack").getBytes();
		try {
			DatagramPacket sendHostPacket = new DatagramPacket(
					data, 
					data.length, 
					replyPacket.getAddress(), 
					replyPacket.getPort());
			ackSocket.send(sendHostPacket);
			printPacketContent(sendHostPacket, "request ack from host", receiveCounter);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	/**
	 * Receive acknowledge request from Intermediate host.
	 */
	private void receiveAck() {
		byte[] data = new byte[100];
		DatagramPacket receiveHostAckPacket = new DatagramPacket(data, data.length);
		try {
			System.out.println(this.getClass().getName() + ": Waiting...\n");
			ackSocket.receive(receiveHostAckPacket);
			printPacketContent(receiveHostAckPacket, "receive ack from host", receiveCounter);
		} catch (IOException e) {
			System.err.println(this.getClass().getName() + ": Program terminated.");
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
		if (sendCounter == REPEAT_NUM) {
			message = "INVALID_REQUEST".getBytes();
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
