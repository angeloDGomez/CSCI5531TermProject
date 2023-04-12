import java.net.*;
import java.io.*;
import java.util.Scanner;
import java.util.Random;
import java.util.ArrayList;

public class ServiceProvider{
	
	public static void main(String args[]){
		int brokerPort = 49155;
		int providerPort = validPNum(brokerPort);
		BrokerCom bc = new BrokerCom(providerPort);
		new Thread(bc).start();
		/*
		Set up service provider server to accept
		communication from service requester.
		*/
		ServerSocket providerSocket = null;
		try{
			providerSocket = new ServerSocket(providerPort);
			while(true){
				Socket clientSocket = providerSocket.accept();
				System.out.println("Client connected at " + clientSocket.getInetAddress() + " "+ clientSocket.getPort());
				ClientServer newClient = new ClientServer(clientSocket);
				new Thread(newClient).start();
			}
		}
		// error handling
		catch(IOException e){System.out.println("Listen :"+e.getMessage());}
		finally {
			if (providerSocket != null) {
                try {providerSocket.close();}
                catch (IOException e) {e.printStackTrace();}
            }
		}		
	}
	
	
	// Generates a port number that is currently not in use 
	public static int validPNum(int providerPort){
		ArrayList<Integer> unavailPNums = new ArrayList<Integer>();
		unavailPNums.add(providerPort);
		Socket s = null;
		try{
			s = new Socket("localhost", providerPort);
			DataInputStream in = new DataInputStream( s.getInputStream());
			DataOutputStream out =new DataOutputStream( s.getOutputStream());
			out.writeUTF("portAvail");
			int i = in.readInt();
			if (i > 0){
				for(int j = 0; j < i; j ++){
					String ipPort = in.readUTF();
					int dashIndex = getDash(ipPort);
					int portToAdd = Integer.parseInt(ipPort.substring(dashIndex + 1, ipPort.length()));
					unavailPNums.add(portToAdd);
				}
			}
			s.close();
		// error handling
		} catch (UnknownHostException e){System.out.println("\nSock:"+e.getMessage()); 
		} catch (EOFException e){System.out.println("\nEOF:"+e.getMessage());
		} catch (IOException e){System.out.println("\nIO:"+e.getMessage());
		} finally {if(s!=null) try {s.close();}catch (IOException e){/*close failed*/}}
		
		while(!unavailPNums.contains(providerPort)){
			providerPort = getRandNum(49152, 65535);
		}
		return providerPort;
	}
	
	// Gets index of dash to find port number.
	public static int getDash(String ipPort){
		int i = 0;
		while(ipPort.charAt(i) != '-'){i++;}
		return i;	
	}
	
	/*
	Generates a random number within a range.
	Used to generate Provider's port number and 
	to calculate a random number for the client.
	*/
	public static int getRandNum(int low, int high){
		Random rand = new Random();
		int randomNum = rand.nextInt((high - low) + 1) + low;
		return randomNum;
	}
	
	public static void getHash(){}
}

/*
Class used to communicate with the broker.
Takes user input on whether to add or remove a service.
 */
class BrokerCom implements Runnable{
	public static int brokerPort = 49155;
	final int portNum;
	Scanner inputScanner;
	public BrokerCom(int portNum){
		this.portNum = portNum;
		this.inputScanner = new Scanner(System.in);
	}
	
	public void run(){
		String userInput;
		while(true){
			System.out.println("\nWould you like to:\n1. Add a service\n2. Remove a service");
			userInput = inputScanner.nextLine();
			userInput = getOneOrTwo(userInput);
			if (userInput.equals("1")){
				System.out.println("\nWhat service would you like to add?\n"+
				"1. Random Number Generator\n2. Hash Generator");
				userInput = inputScanner.nextLine();
				userInput = getOneOrTwo(userInput);
				System.out.println("\nContacting Service Broker to fulfull request.");
				if (userInput.equals("1")){addService("randNum");
				}else{addService("hash");}
			}else{
				System.out.println("\nWhat service would you like to remove?\n"+
				"1. Random Number Generator\n2. Hash Generator");
				userInput = inputScanner.nextLine();
				userInput = getOneOrTwo(userInput);
				System.out.println("\nContacting Service Broker to fulfull request.");
				if (userInput.equals("1")){removeService("randNum");
				}else{removeService("hash");}
			}
		}
	}
	
	public String getOneOrTwo(String userInput){
		while(!(userInput.equals("1") || userInput.equals("2"))){
			System.out.println("\nPlease enter the number 1, or 2");
			userInput = inputScanner.nextLine();
		}
		return userInput;
	}
	
	public void addService(String serviceName){
		Socket s = null;
		try{
			s = new Socket("localhost", brokerPort);
			DataInputStream in = new DataInputStream( s.getInputStream());
			DataOutputStream out =new DataOutputStream( s.getOutputStream());
			out.writeUTF("addService");
			out.writeUTF(Integer.toString(portNum));
			out.writeUTF(serviceName);
			if (in.readBoolean()){
				System.out.println("\nYour service has been successfully added to the broker.");
			}else{System.out.println("\nThis service has already been registered with this port number.");}
			s.close();
		// error handling
		} catch (UnknownHostException e){System.out.println("\nSock:"+e.getMessage()); 
		} catch (EOFException e){System.out.println("\nEOF:"+e.getMessage());
		} catch (IOException e){System.out.println("\nIO:"+e.getMessage());
		} finally {if(s!=null) try {s.close();}catch (IOException e){/*close failed*/}}
	}
	
	public void removeService(String serviceName){
		Socket s = null;
		try{
			s = new Socket("localhost", brokerPort);
			DataInputStream in = new DataInputStream( s.getInputStream());
			DataOutputStream out =new DataOutputStream( s.getOutputStream());
			out.writeUTF("removeService");
			out.writeUTF(Integer.toString(portNum));
			out.writeUTF(serviceName);
			if (in.readBoolean()){
				System.out.println("\nYour service has been successfully removed from the broker.");
			}else{System.out.println("\nThere is no service associated with your port number in the broker.");}
			s.close();
		// error handling
		} catch (UnknownHostException e){System.out.println("\nSock:"+e.getMessage()); 
		} catch (EOFException e){System.out.println("\nEOF:"+e.getMessage());
		} catch (IOException e){System.out.println("\nIO:"+e.getMessage());
		} finally {if(s!=null) try {s.close();}catch (IOException e){/*close failed*/}}
	}
}

/*
This class is used to communicate with the 
service requester. It returns the appropriate 
value based on the request.
*/
class ClientServer implements Runnable{
	private DataInputStream in;
	private DataOutputStream out;
	private Socket clientSocket;
	
	public ClientServer(Socket client){
		this.clientSocket = client;
	}
	
	public void run(){
		try{
			in = new DataInputStream(clientSocket.getInputStream());
			out = new DataOutputStream(clientSocket.getOutputStream());
			String request = in.readUTF();
			if (request.equals("getRandNum")){
				int low = in.readInt();
				int high = in.readInt();
				out.writeInt(ServiceProvider.getRandNum(low, high));
			}
			else{
				String hashType = in.readUTF();
				String strToHash = in.readUTF();
			}
			
		// error handling
		} catch(EOFException e) {System.out.println("EOF:"+e.getMessage());
		} catch(IOException e) {System.out.println("IO:"+e.getMessage());
		} finally { try {clientSocket.close();}catch (IOException e){/*close failed*/}}
	}
		
}