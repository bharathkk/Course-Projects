package server;

import java.util.ArrayList;
import java.util.List;

public class Account {
	
	int accountNumber,branchId;
	float balance;
	List<Transaction> ongoingTransactions = null,transactionsHistory = null;
	
	public void setAccountNumber(int accountNumber) {
		this.accountNumber = accountNumber;
	}

	public void setBranchId(int branchId) {
		this.branchId = branchId;
	}

	public void setBalance(float balance) {
		this.balance = balance;
	}

	public int getAccountNumber() {
		return accountNumber;
	}

	public int getBranchId() {
		return branchId;
	}

	public float getBalance() {
		return balance;
	}

	synchronized public void updateBalance( float amount ) {
		this.balance = amount;
	}
	
	synchronized public Transaction getAwaitingTransaction () {
		
		int first = 0;
		if ( ongoingTransactions == null )
			return null;
		
		Transaction tobeProcessed = null;
		tobeProcessed = ongoingTransactions.remove(first);
				
		return tobeProcessed;
	}
	
	public void updateOngoingQueue ( Transaction todoTransaction ) {
		
		if ( ongoingTransactions == null ) {
			ongoingTransactions = new ArrayList<Transaction>();
		}
		
		ongoingTransactions.add(todoTransaction);
	}
	
	public void updateTransactionsHistory ( Transaction completedTransaction ) {
		
		if ( transactionsHistory == null ) {
			transactionsHistory = new ArrayList<Transaction>();
		}
		
		transactionsHistory.add(completedTransaction);
	}
	
	boolean hasOccured ( Transaction transaction ) {
		
		int index = 0;
		Transaction temp = null;
		
		do {
			temp = ongoingTransactions.get(index++);
			if ( temp.getTransactionId() != transaction.getTransactionId()) {
				if ( temp.getSerialNumber() == transaction.getSerialNumber())
					return true;
			}
			else
				break;				
		} while(index < ongoingTransactions.size());		
		
		return true;
	}

}
