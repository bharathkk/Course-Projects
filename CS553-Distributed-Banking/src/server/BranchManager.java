package server;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Semaphore;

import wrapper.Messenger;

public class BranchManager extends Thread{

	static HashMap<Integer,String> transactionResults = null;
	static HashMap<Integer,Semaphore> semaphores = null;
	static List<Account> accounts = null;
	int accountNumber;
	
	public String performTransaction ( Transaction transaction ) throws InterruptedException, FileNotFoundException {
		String result = null;
		
		// Return error code if the transaction doesn't belong to this branch
		if ( !belongsToBranch(transaction) )
			return "Code:400 Unable to process the transaction";
		
		
		// Checks whether one of the accounts source/destination is present among the list of accounts.
		int accountIndex = isAccountPresent(transaction.getSrcAccount());
		if ( accountIndex == -1 )
			accountIndex = isAccountPresent(transaction.getDestAccount());
		
		if ( transaction.isIntraBankTransfer())
			accountIndex = isAccountPresent(transaction.getDestAccount());
		
		// Checks which one of them source/destination belongs to the branch
		// Creates an account with that account number starting with a zero balance.
		if ( accountIndex == -1) {
			
			if ( transaction.getSrcBranch() == BranchServer.getBranchId()) {
				addAccount(transaction.getSrcAccount());
				accounts.get(accounts.size() -1 ).updateOngoingQueue(transaction);
				accountIndex = accounts.size() - 1;
			}
			else {
				addAccount(transaction.getDestAccount());
				accounts.get(accounts.size() - 1 ).updateOngoingQueue(transaction);
				accountIndex = accounts.size() - 1;
			}
		}
		else 		
			accounts.get(accountIndex).updateOngoingQueue(transaction);	
		
		accountNumber = accounts.get(accountIndex).getAccountNumber();
		this.start();
		
		// Wait for the transaction to proceed until the result table is updated with the transaction result
		while ( transactionResults == null || transactionResults.containsKey(transaction.getTransactionId()))
			Thread.sleep(2000);
		
		result = transactionResults.get(transaction.getTransactionId());
		return result;
	}
	
	// Adds account the existing list of accounts maintained starting with a zero balance
	void addAccount ( int accountNumber ) {
		
		Account newAccount = new Account();
		newAccount.setAccountNumber(accountNumber);
		newAccount.setBalance(0);
		newAccount.setBranchId(BranchServer.branchId);
		if ( accounts == null )
			accounts = new ArrayList<Account>();
		accounts.add(newAccount);
		
		if ( semaphores == null )
			semaphores = new HashMap<Integer,Semaphore>();
		Semaphore semaphore = new Semaphore(1);
		semaphores.put(newAccount.getAccountNumber(), semaphore);
	}
	
	// Returns if a transaction belongs to this branch or not
	boolean belongsToBranch ( Transaction transaction ) {
		if ( transaction.getSrcBranch() == BranchServer.getBranchId() )
			return true;
		else
			if ( transaction.getDestBranch() == BranchServer.getBranchId())
				return true;
		
		return false;
	}
	
	// Checks the presence of the account among the maintained list of accounts
	int isAccountPresent ( int accountNumber ) {
		int result = -1;
		
		if ( accounts != null ) {
			int index = 0;
			
			while ( index < accounts.size() ) {
				
				if ( accounts.get(index).getAccountNumber() == accountNumber ) {
					result = index;
					break;
				}
				index++;
			}
		}
		return result;
	}	
	
	public void run() {
		
		try {
				Semaphore sem = null;
				int index = 0;
				Transaction todoTransaction = null;
			
				index = isAccountPresent(accountNumber);
				if ( index != -1 ) {
						
					System.out.println("Entered to process transaction....");
					sem = semaphores.get(accountNumber);
					sem.acquire();
					System.out.println("semaphore acquired...");
					todoTransaction = accounts.get(index).getAwaitingTransaction();
					serviceRequest(todoTransaction);
					sem.release();
					System.out.println("semaphore released...");
				}
											
			} catch (InterruptedException e) {
					e.printStackTrace();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}		
	}
	
	void serviceRequest ( Transaction todoTransaction ) throws FileNotFoundException {
		
		switch (todoTransaction.getTransactionType()) {
		
			case DEPOSIT   :	deposit(todoTransaction); 
								break;
							
			case WITHDRAW  :	withdraw(todoTransaction);
								break;
								
			case QUERY	   :	query(todoTransaction);
								break;
								
			case TRANSFER  :	transfer(todoTransaction);
								break;
								
		}
	}
	
	void deposit( Transaction todoTransaction ) {
		
		StringBuilder transactionResult = new StringBuilder();
		Account account = accounts.get(isAccountPresent(todoTransaction.getSrcAccount()));
		float accountBalance = 0;
		
		accountBalance = account.getBalance() - todoTransaction.getAmount();
		if ( !account.hasOccured(todoTransaction))
			account.updateBalance(account.getBalance() + todoTransaction.getAmount());
		
		transactionResult.append("Code:200 ");
		transactionResult.append(accountBalance);
		
		if ( transactionResults == null )
			transactionResults = new HashMap<Integer,String>();
		
		transactionResults.put(todoTransaction.getTransactionId(), transactionResult.toString());		
	}
	
	void withdraw ( Transaction todoTransaction ) {
		
		StringBuilder transactionResult = new StringBuilder();
		Account account = accounts.get(isAccountPresent(todoTransaction.getSrcAccount()));
		float accountBalance = 0;
		
		accountBalance = account.getBalance() - todoTransaction.getAmount();
		if ( !account.hasOccured(todoTransaction))
			account.updateBalance(account.getBalance() - todoTransaction.getAmount());
		
		transactionResult.append("Code:200 ");
		transactionResult.append(accountBalance);
		
		if ( transactionResults == null )
			transactionResults = new HashMap<Integer,String>();
		
		transactionResults.put(todoTransaction.getTransactionId(), transactionResult.toString());
	}
	
	void query ( Transaction todoTransaction ) {
		
		StringBuilder transactionResult = new StringBuilder();
		Account account = accounts.get(isAccountPresent(todoTransaction.getSrcAccount()));
		
		transactionResult.append("Code:200 ");
		transactionResult.append(account.getBalance());
		
		if ( transactionResults == null )
			transactionResults = new HashMap<Integer,String>();
		
		transactionResults.put(todoTransaction.getTransactionId(), transactionResult.toString());
	}
	
	void transfer ( Transaction todoTransaction ) throws FileNotFoundException {
		
		StringBuilder transactionResult = new StringBuilder();
		float transactionBalance = -1.1111f;
		boolean isComplete = false;
		Account account = null;
		Messenger interBranchService = new Messenger();
		
		if ( todoTransaction.isIntraBankTransfer()) {
			
			account = accounts.get(isAccountPresent(todoTransaction.getDestAccount()));
			if ( !account.hasOccured(todoTransaction)) {
				account.updateBalance(account.getBalance() + todoTransaction.getAmount());
				transactionBalance = account.getBalance();
			}
			else
				transactionBalance = account.getBalance() - todoTransaction.getAmount();
		}
		else {
			
			if ( todoTransaction.getSrcBranch() == BranchServer.getBranchId()) {
				
				isComplete = interBranchService.sendMessage(todoTransaction.getDestBranch(), todoTransaction);
				account = accounts.get(isAccountPresent(todoTransaction.getSrcAccount()));
				if ( !account.hasOccured(todoTransaction) && isComplete) {
					account.updateBalance(account.getBalance() - todoTransaction.getAmount());
					transactionBalance = account.getBalance();
				}
				else
					transactionBalance = account.getBalance() - todoTransaction.getAmount();
					
			}
				
			else{
				
				account = accounts.get(isAccountPresent(todoTransaction.getDestAccount()));
				if ( !account.hasOccured(todoTransaction)) {
					account.updateBalance(account.getBalance() + todoTransaction.getAmount());
					transactionBalance = account.getBalance();
				}
				else
					transactionBalance = account.getBalance() - todoTransaction.getAmount();			
			}
		}
		
			
		if ( transactionBalance != -1.1111f ) {
			
			transactionResult.append("Code:200 ");
			transactionResult.append(transactionBalance);
		}
		else 
			transactionResult.append("Code:400 Accounts inaccessible to the branch/ Accounts not present");
		
		
		if ( transactionResults == null )
			transactionResults = new HashMap<Integer,String>();
		
		transactionResults.put(todoTransaction.getTransactionId(), transactionResult.toString());
	}
}
