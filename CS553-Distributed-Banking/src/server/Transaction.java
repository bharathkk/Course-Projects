package server;

 enum Action {DEPOSIT,WITHDRAW,QUERY,TRANSFER};

public class Transaction {
	int srcAccount,destAccount;
	int destBranch,srcBranch;
	float amount;
	Action transactionType;
	static int transactionCounter = 0;
	int transactionId;
	String serialNumber;
	boolean intraBankTransfer;
	
	public Transaction() {
		transactionCounter++;
		transactionId = transactionCounter;
		destBranch = destAccount = srcBranch = srcAccount = 0;
		amount = 0;
		transactionType = null;
		intraBankTransfer = false;
	}
		
	public Action getTransactionType() {
		return transactionType;
	}
	
	public boolean isIntraBankTransfer() {
		return intraBankTransfer;
	}

	public void setIntraBankTransfer(boolean intraBankTransfer) {
		this.intraBankTransfer = intraBankTransfer;
	}

	public void setTransactionType(Action transactionType) {
		this.transactionType = transactionType;
	}
	
	public int getSrcAccount() {
		return srcAccount;
	}
	
	public void setSrcAccount(int srcAccount) {
		this.srcAccount = srcAccount;
	}
	
	public int getDestAccount() {
		return destAccount;
	}
	
	public void setDestAccount(int destAccount) {
		this.destAccount = destAccount;
	}
	
	public int getDestBranch() {
		return destBranch;
	}
	
	public void setDestBranch(int destBranch) {
		this.destBranch = destBranch;
	}
	
	public int getSrcBranch() {
		return srcBranch;
	}
	
	public void setSrcBranch(int srcBranch) {
		this.srcBranch = srcBranch;
	}
	
	public float getAmount() {
		return amount;
	}
	
	public int getTransactionId() {
		return transactionId;
	}

	public void setAmount(float amount) {
		this.amount = amount;
	}
	
	public String getSerialNumber() {
		return serialNumber;
	}
	
	public void setSerialNumber(String serialNumber) {
		this.serialNumber = serialNumber;
	}	
	
}
