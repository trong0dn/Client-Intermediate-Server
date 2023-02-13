import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

/**
 * This class represents an Intermediate Host.
 * @author Trong Nguyen
 * @version 2.0
 * @date 04/03/2023
 */
public class Intermediate implements Runnable {
	
	private static final int CLIENT_PORT_NUM = 23;
	private static final int SERVER_PORT_NUM = 69;
	private static final int TIMEOUT = 10000;

	DatagramSocket clientSocket, serverSocket;
	
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
	 * Run Intermediate host methods.
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		try {
			clientSocket = new DatagramSocket(CLIENT_PORT_NUM);
			serverSocket = new DatagramSocket(SERVER_PORT_NUM);
			clientSocket.setSoTimeout(TIMEOUT);
			serverSocket.setSoTimeout(TIMEOUT);
			while (true) {
				counter++;
				DatagramPacket reply = replyClient();
				replyServer(reply);
				ackServer();
				ackClient();
				System.out.println("--------------------------------------");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			serverSocket.close();
			clientSocket.close();
			System.err.println(this.getClass().getName() + ": Program terminated.");
		}
	}
	
	/**
	 * Reply to Client sending data.
	 * @return DatagramPacket, containing the same message received
	 */
	private DatagramPacket replyClient() {
		data = new byte[100];
		DatagramPacket receiveClientDataPacket = null;
		try {
			receiveClientDataPacket = new DatagramPacket(data, data.length);
			System.out.println(this.getClass().getName() + ": Waiting...\n");
			clientSocket.receive(receiveClientDataPacket);
			printPacketContent(receiveClientDataPacket, "receive data from client", counter);
			
			DatagramPacket replyClientPacket = new DatagramPacket(
					receiveClientDataPacket.getData(), 
					receiveClientDataPacket.getLength(), 
					receiveClientDataPacket.getAddress(), 
					receiveClientDataPacket.getPort());
			clientSocket.send(replyClientPacket);
			printPacketContent(replyClientPacket, "send reply to client", counter);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		return receiveClientDataPacket;
	}
	
	/**
	 * Reply to Server request for data.
	 * @param sendServerDataPacket DatagramPacket, contain the message from Client
	 */
	private void replyServer(DatagramPacket sendDataPacket) {
		data = new byte[100];
		try {
			DatagramPacket receiveServerPacket = new DatagramPacket(data, data.length);
			System.out.println(this.getClass().getName() + ": Waiting...\n");
			serverSocket.receive(receiveServerPacket);
			printPacketContent(receiveServerPacket, "receive reply from server", counter);
			
			DatagramPacket replyServerDataPacket = new DatagramPacket(
					sendDataPacket.getData(),
					sendDataPacket.getLength(), 
					receiveServerPacket.getAddress(), 
					receiveServerPacket.getPort());
			serverSocket.send(replyServerDataPacket);
			printPacketContent(replyServerDataPacket, "send data to server", counter);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	/**
	 * Acknowledge Server message.
	 */
	private void ackServer() {
		data = new byte[100];
		try {
			DatagramPacket receiveServerAckPacket = new DatagramPacket(data, data.length);
			System.out.println(this.getClass().getName() + ": Waiting...\n");
			serverSocket.receive(receiveServerAckPacket);
			printPacketContent(receiveServerAckPacket, "received ack from server", counter);
			
			DatagramSocket ackServerSocket = new DatagramSocket();
			DatagramPacket replyServerAckPacket = new DatagramPacket(
					receiveServerAckPacket.getData(),
					receiveServerAckPacket.getLength(), 
					receiveServerAckPacket.getAddress(), 
					receiveServerAckPacket.getPort());
			ackServerSocket.send(replyServerAckPacket);
			printPacketContent(replyServerAckPacket, "reply ack to server", counter);
			ackServerSocket.close();
		} catch (IOException e) {
			System.err.println(this.getClass().getName() + ": Program terminated.");
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	/**
	 * Acknowledge Client message.
	 */
	private void ackClient() {
		data = new byte[100];
		try {
			DatagramPacket receiveClientAckPacket = new DatagramPacket(data, data.length);
			System.out.println(this.getClass().getName() + ": Waiting...\n");
			clientSocket.receive(receiveClientAckPacket);
			printPacketContent(receiveClientAckPacket, "receive ack from client", counter);
			
			DatagramSocket ackClientSocket = new DatagramSocket();
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
