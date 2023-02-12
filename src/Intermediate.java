import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

/**
 * This class represents an Intermediate Host.
 * @author Trong Nguyen
 * @version 2.0
 * @date 04/03/2023
 */
public class Intermediate {
	
	private static final int DATA_CLIENT_PORT_NUM = 23;
	private static final int DATA_SERVER_PORT_NUM = 69;

	DatagramSocket dataClientSocket, dataServerSocket, ackServerSocket, ackClientSocket;
	
	private byte[] data;
	private int counter = 0;
	
	/**
	 * Main method for Intermediate host.
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
		try {
			
			while (true) {
				DatagramPacket reply = replyClient();
				replyServer(reply);
				ackServer();
				ackClient();
				counter++;
				System.out.println("-------------------");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.err.println(this.getClass().getName() + ": Program terminated.");
	}
	
	public DatagramPacket replyClient() {
		data = new byte[100];
		DatagramPacket receiveClientDataPacket = null;
		
		try {
			dataClientSocket = new DatagramSocket(DATA_CLIENT_PORT_NUM);
			receiveClientDataPacket = new DatagramPacket(data, data.length);
			System.out.println(this.getClass().getName() + ": Waiting...\n");
			dataClientSocket.receive(receiveClientDataPacket);
			printPacketContent(receiveClientDataPacket, "receive data from client", counter);
			
			DatagramPacket replyClientPacket = new DatagramPacket(
					receiveClientDataPacket.getData(), 
					receiveClientDataPacket.getLength(), 
					receiveClientDataPacket.getAddress(), 
					receiveClientDataPacket.getPort());
			dataClientSocket.send(replyClientPacket);
			printPacketContent(replyClientPacket, "send reply to client", counter);
			dataClientSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		return receiveClientDataPacket;
	}
	

	public void replyServer(DatagramPacket sendServerDataPacket) {
		data = new byte[100];
		try {
			dataServerSocket = new DatagramSocket(DATA_SERVER_PORT_NUM);
			DatagramPacket receiveServerPacket = new DatagramPacket(data, data.length);
			System.out.println(this.getClass().getName() + ": Waiting...\n");
			dataServerSocket.receive(receiveServerPacket);
			printPacketContent(receiveServerPacket, "receive reply from server", counter);
			
			DatagramPacket replyServerDataPacket = new DatagramPacket(
					sendServerDataPacket.getData(),
					sendServerDataPacket.getLength(), 
					receiveServerPacket.getAddress(), 
					receiveServerPacket.getPort());
			dataServerSocket.send(replyServerDataPacket);
			printPacketContent(replyServerDataPacket, "send data to server", counter);
			dataServerSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	public void ackServer() {
		data = new byte[100];
		try {
			ackServerSocket = new DatagramSocket(DATA_SERVER_PORT_NUM);
			DatagramPacket receiveServerAckPacket = new DatagramPacket(data, data.length);
			System.out.println(this.getClass().getName() + ": Waiting...\n");
			ackServerSocket.receive(receiveServerAckPacket);
			printPacketContent(receiveServerAckPacket, "received ack from server", counter);
			
			DatagramPacket replyServerAckPacket = new DatagramPacket(
					receiveServerAckPacket.getData(),
					receiveServerAckPacket.getLength(), 
					receiveServerAckPacket.getAddress(), 
					receiveServerAckPacket.getPort());
			ackServerSocket.send(replyServerAckPacket);
			printPacketContent(replyServerAckPacket, "reply ack to server", counter);
			ackServerSocket.close();
		} catch (IOException e) {
			System.exit(1);
		}
	}
	
	public void ackClient() {
		data = new byte[100];
		try {
			ackClientSocket = new DatagramSocket(DATA_CLIENT_PORT_NUM);
			DatagramPacket receiveClientAckPacket = new DatagramPacket(data, data.length);
			System.out.println(this.getClass().getName() + ": Waiting...\n");
			ackClientSocket.receive(receiveClientAckPacket);
			printPacketContent(receiveClientAckPacket, "receive ack from client", counter);
			
			DatagramPacket replyClientAckPacket = new DatagramPacket(
					receiveClientAckPacket.getData(), 
					receiveClientAckPacket.getLength(), 
					receiveClientAckPacket.getAddress(), 
					receiveClientAckPacket.getPort());
			ackClientSocket.send(replyClientAckPacket);
			printPacketContent(replyClientAckPacket, "reply ack to client", counter);
			ackClientSocket.close();
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
}
