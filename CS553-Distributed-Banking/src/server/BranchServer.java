package server;

import java.io.IOException;
import java.net.ServerSocket;

public class BranchServer {

	static int branchId;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		ServerSocket serverSocket = null;
		boolean listening = true;
		
		
			try {
				if ( args.length == 1) {
					
					serverSocket = new ServerSocket(Integer.parseInt(args[0]));
					branchId = Integer.parseInt(args[0]);
					
					while (listening) 
						new BranchServerService(serverSocket.accept()).start();
					
					serverSocket.close();
					
				}
				else {
					System.out.println("Usage: java BranchServer <PORT>");
					System.exit(-1);
				}
				
			} catch (NumberFormatException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		

	}
	
	// gets the server branch id
	public static int getBranchId () {
		return branchId;
	}

}
