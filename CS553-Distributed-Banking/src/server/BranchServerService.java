package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.StringTokenizer;

public class BranchServerService extends Thread {
	
	private Socket socket = null;
	static int counter = 0;
	
	public BranchServerService ( Socket socket ) {
		super("BranchServerService");
		this.socket = socket;
		counter++;
	}
	
	// Reconstructs the message from the clients and creates an instance of branch server
	// and performs the associated transaction and returns the result.
	public void run() {
		try {
			PrintWriter out = new PrintWriter(socket.getOutputStream(),true);
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			
			String input,result;
			BranchManager branchManager = new BranchManager();
			Transaction newTransaction = null;
			
			while ( (input = in.readLine()) != null ) {
				
				newTransaction = parseMessage(input);
				getSerialNumber(newTransaction,socket.getPort(),input);
				checkIntraBranchTransfer(newTransaction,input);
				result = branchManager.performTransaction(newTransaction);	
				
				// include a code that checks for clientGUI connection.. if so send the result
				if ( newTransaction.getTransactionType() != Action.TRANSFER)
				out.println(result);				
			}
			
			in.close();
			out.close();
			socket.close();
			
		} catch ( IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	// Parse the message from the client/other server and construct into a valid transaction object.
	public Transaction parseMessage ( String message ) {
		
		StringTokenizer st = new StringTokenizer(message," ");
		Transaction newTransaction = new Transaction();
		Action transactionType = null;
		String temp;
		temp = st.nextToken();
		temp = temp.toUpperCase();
		transactionType = assignTransactionType(temp);
		
		if ( transactionType == null )
			return null;
		
		 temp = st.nextToken();
		 String[] temp1 = temp.split("[.]");
		 newTransaction.setSrcBranch(Integer.parseInt(temp1[0]));
		 newTransaction.setSrcAccount(Integer.parseInt(temp1[1]));		 
		
		switch (transactionType) {
		
			case DEPOSIT  :  temp = st.nextToken();
							 newTransaction.setAmount(Float.valueOf(temp));
							 newTransaction.setTransactionType(server.Action.DEPOSIT);
							 break;
			
			case WITHDRAW :	 temp = st.nextToken();
							 newTransaction.setAmount(Float.valueOf(temp));
			 				 newTransaction.setTransactionType(server.Action.WITHDRAW);
			 			 	 break;
							
			case QUERY    :	 newTransaction.setTransactionType(server.Action.QUERY);
							 break;
			
			case TRANSFER :  temp = st.nextToken();
			 				 String[] temp2 = temp.split(".");
			 				 newTransaction.setDestBranch(Integer.parseInt(temp2[0]));
			 				 newTransaction.setDestAccount(Integer.parseInt(temp2[1]));			 				 
			 				 
			 				 temp = st.nextToken();
			 				 newTransaction.setAmount(Float.valueOf(temp));
							 newTransaction.setTransactionType(server.Action.TRANSFER);
							 break;
		}
		
		return newTransaction;
		
	}
	
	// assigns the type of transaction to the transaction object created based on parsing the incoming message.
	Action assignTransactionType ( String temp ) {
		
		Action transaction;
		if ( temp.equalsIgnoreCase("deposit"))
			transaction = server.Action.DEPOSIT;
		else 
			if ( temp.equalsIgnoreCase("withdraw"))
				transaction = server.Action.WITHDRAW;
			else
				if ( temp.equalsIgnoreCase("query"))
					transaction = server.Action.QUERY;
				else 
					if (temp.equalsIgnoreCase("transfer"))
						transaction = server.Action.TRANSFER;
					else
						transaction = null;
		
		return transaction;
	}
	
	void getSerialNumber ( Transaction transaction, int clientPort, String message ) {
		
		StringBuilder serialNum = new StringBuilder();
		String temp;
		
		if ( message.contains("serialNumber")) {
			
			StringTokenizer st = new StringTokenizer(message);
			
			while ( st.hasMoreTokens()) {
				
				temp = st.nextToken(" ");
				if ( temp.equalsIgnoreCase("serialnumber")) {
					temp = st.nextToken(" ");
					transaction.setSerialNumber(temp);
					break;
				}
			}
		}
		else {
			serialNum.append(transaction.getSrcAccount());
			serialNum.append(transaction.getSrcBranch());
			serialNum.append(clientPort);
			transaction.setSerialNumber(serialNum.toString());
		}		
	}
	
	void checkIntraBranchTransfer( Transaction todoTransaction, String message ) {
		
		if ( message.contains("intraBranch"))
			if (todoTransaction.getDestBranch() == todoTransaction.getSrcBranch())
				todoTransaction.setIntraBankTransfer(true);
	}
	
}
