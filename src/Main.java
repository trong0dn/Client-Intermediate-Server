/**
 * @author Trong Nguyen
 * @version 2.0
 * @date 04/03/2023
 */
public class Main {
	
	/**
	 * Main method.
	 * @param args, default parameters
	 */
	public static void main(String[] args) {
		Thread client = new Thread() {
			public void run() {
				new Client();
			}
		};
		
		Thread intermediate = new Thread() {
			public void run() {
				new Intermediate();
			}
		};
		
		Thread server = new Thread() {
			public void run() {
				new Server();
			}
		};
		
		try {
			intermediate.start();
			Thread.sleep(1000);
			client.start();
			Thread.sleep(1000);
			server.start();
		} catch (InterruptedException e ) {
	        e.printStackTrace();
	        System.exit(1);
	    }
	}
}
