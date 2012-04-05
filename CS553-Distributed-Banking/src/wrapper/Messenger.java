package wrapper;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

import server.Transaction;

enum Action {DEPOSIT,WITHDRAW,QUERY,TRANSFER};

public class Messenger {
	
	Finder finderService;
	Transaction transaction;
	
	public Messenger () throws FileNotFoundException {
		finderService = new Finder();
	}
	
	public boolean sendMessage( int destBranchId, Transaction todoTransaction ) {
		
		boolean result = false;
		String transactionDetails = null;
		
		if (finderService.isLinkPresent(destBranchId)) {
			
			finderService.getBranchInformation(destBranchId);
						
			try {
				
				Socket clientSocket = new Socket(finderService.getDestMachine(),finderService.getDestPort());
				PrintWriter socketWriter = new PrintWriter(clientSocket.getOutputStream(),true);
				
				transactionDetails = constructTransaction(todoTransaction);
				socketWriter.print(transactionDetails.toString());
				
				socketWriter.close();
				clientSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return result;
	}
	
	public List<Integer> whoAreNeighbours() {
		return finderService.whoAreNeighbours();
	}
	
	String constructTransaction( Transaction todoTransaction ) {
		
		StringBuilder transactionDetails = new StringBuilder();
		
		transactionDetails.append(todoTransaction.getTransactionType()+" ");
		transactionDetails.append(todoTransaction.getSrcBranch()+"."+todoTransaction.getSrcAccount()+" ");
		transactionDetails.append(todoTransaction.getDestBranch()+"."+todoTransaction.getDestAccount()+" ");
		transactionDetails.append(todoTransaction.getAmount()+" ");
		transactionDetails.append("serialnumber "+todoTransaction.getSerialNumber());
		if ( todoTransaction.getSrcBranch() == todoTransaction.getDestBranch())
			transactionDetails.append(" intraBranch");
		return transactionDetails.toString();
	}
}
