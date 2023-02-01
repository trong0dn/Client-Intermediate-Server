import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
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
	
	private static final int CLIENT_HOST_PORT_NUM = 23;
	private static final int HOST_SERVER_PORT_NUM = 69;

	private boolean valid = true;
	private int counter = 0;
	private byte[] data;
	
	private DatagramSocket clientHostSocket, hostServerSocket;
	private DatagramPacket clientHostPacket, hostServerPacket, serverHostPacket, hostClientPacket;
	
	public static void main(String[] args) {
		new Host();
	}
	
	public Host() {
		run();
	}

	private void run() {
		createSockets();
		while (valid) {
			receiveClientPacket();
			sendServerPacket();
			receiveServerPacket();
			sendClientPacket();
			counter++;
		}
		clientHostSocket.close();
		hostServerSocket.close();
		System.out.println(this.getClass().getName() + ": Program completed.");
	}

	
	/**
	 * Constructs datagram sockets on the local host machine. 
	 * These sockets will be used to send UDP Datagram packets.
	 */
	public void createSockets() {
		try {
			clientHostSocket = new DatagramSocket(CLIENT_HOST_PORT_NUM);
			hostServerSocket = new DatagramSocket();
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
			clientHostSocket.receive(clientHostPacket);
			printPacketContent(clientHostPacket, "received", counter);
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
	public void sendServerPacket() {
		hostServerPacket = new DatagramPacket(
				clientHostPacket.getData(),
				clientHostPacket.getLength(), 
				clientHostPacket.getAddress(), 
				HOST_SERVER_PORT_NUM);
		try {
			hostServerSocket.send(hostServerPacket);
			printPacketContent(hostServerPacket, "sending", counter);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	public void receiveServerPacket() {
		data = new byte[100];
		try {
			serverHostPacket = new DatagramPacket(data, data.length);
			System.out.println(this.getClass().getName() + ": Waiting...\n");
			hostServerSocket.receive(serverHostPacket);
			printPacketContent(serverHostPacket, "received", counter);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	public void sendClientPacket() {
		valid = verifyMessage(serverHostPacket);
		data = serverHostPacket.getData();
		hostClientPacket = new DatagramPacket(
				data,
				serverHostPacket.getLength(), 
				serverHostPacket.getAddress(), 
				clientHostPacket.getPort());
		try {
			clientHostSocket.send(hostClientPacket);
			printPacketContent(hostClientPacket, "sending", counter);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
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
