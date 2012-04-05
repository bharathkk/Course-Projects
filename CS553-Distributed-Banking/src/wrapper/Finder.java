package wrapper;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import server.BranchServer;

public class Finder {
	
	boolean linkPresent;
	int destPort;
	String destMachine;
	static List<Integer> neighbours;
	
	Finder() throws FileNotFoundException { 
		
		linkPresent = false;
		destPort = 0;
		destMachine = null; 
		
		if ( neighbours == null ) {
			
			neighbours = new ArrayList<Integer>();
			acquireNeighbourLinks();
		}
	}
	
	public boolean isLinkPresent( int destBranchId ) {
		
		linkPresent = false;
		
		Iterator<Integer> itr = neighbours.iterator();
		
		while ( itr.hasNext()) {
			
			if ( itr.next() == destBranchId ) {
				linkPresent = true;
				break;
			}				
		}
		
		return linkPresent;
	}
	
	public List<Integer> whoAreNeighbours() {
		return neighbours;
	}
	
	public int getDestPort() {
		return destPort;
	}
	
	public String getDestMachine() {
		return destMachine;
	}
		
	void setDestPort(int destPort) {
		this.destPort = destPort;
	}

	void setDestMachine(String destMachine) {
		this.destMachine = destMachine;
	}

	void acquireNeighbourLinks() {
		
		try {
			BufferedReader bReader = new BufferedReader(new FileReader("topology.txt"));
			String inputLine;
			int srcBranchId,destBranchId;
			
			while ( (inputLine = bReader.readLine()) != null ) {
				
				StringTokenizer st = new StringTokenizer(inputLine);
				
				srcBranchId = Integer.parseInt(st.nextToken(" "));
				st.nextToken(" ");
				destBranchId = Integer.parseInt(st.nextToken(" "));
				
				if ( srcBranchId == BranchServer.getBranchId()) {
					neighbours.add(destBranchId);
				}
			}
			
			bReader.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	void getBranchInformation( int branchId ) { 
		
		try {
			
			BufferedReader bReader = new BufferedReader(new FileReader("BranchServers.txt"));
			String inputLine;
			int destBranchId;
			
			while ( (inputLine = bReader.readLine()) != null ) {
				
				StringTokenizer st = new StringTokenizer(inputLine);
					
				destBranchId = Integer.parseInt(st.nextToken(" "));
				if ( destBranchId == branchId ) {
					
					int destPort;
					String destMachine;
					
					destMachine = st.nextToken(" ");
					destPort = Integer.parseInt(st.nextToken(" "));
					setDestPort(destPort);
					setDestMachine(destMachine);
					break;
				}
			}
			bReader.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
