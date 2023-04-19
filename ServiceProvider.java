import java.net.*;
import java.io.*;
import java.util.Scanner;
import java.util.Random;
import java.util.ArrayList;
import java.util.Arrays;

public class ServiceProvider{
	final static int brokerPort = 49155;
	public static Scanner inputScanner = new Scanner(System.in);
	
	public static void main(String[] args){
		int myPort = registerSelf();
		if (myPort == -1){
			System.out.println("ERROR: Something wrong happened in system set up. Shutting Down.");
			System.exit(0);
		}
		BrokerCom bc = new BrokerCom();
		new Thread(bc).start();
		/*
		Set up service provider server to accept
		communication from service requester.
		*/
		ServerSocket providerSocket = null;
		try{
			providerSocket = new ServerSocket(myPort);
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

	public static int registerSelf(){
		Socket s = null;
		try{
			s = new Socket("localhost", brokerPort);
			DataInputStream in = new DataInputStream( s.getInputStream());
			DataOutputStream out =new DataOutputStream( s.getOutputStream());
			out.writeUTF("registerProvider");
			System.out.println("Please enter your desired username.");
			String userName = inputScanner.nextLine();
			boolean invalidUser = true;
			while(invalidUser){
				out.writeUTF(userName);
				invalidUser = in.readBoolean();
				// Add userName and password checking
				if (invalidUser){
					System.out.println("\nPlease enter your desired username.");
					userName = inputScanner.nextLine();					
				}
			}
			System.out.println("\nPlease enter your desired password.");
			String password = inputScanner.nextLine();
			/*
			while(userName.length > 10 || userName.length < 1){
				System.out.println("Please enter your desired password. (No more than 10 characters long)\n");
				password = inputScanner.nextLine();
			}*/
			out.writeUTF(password);			
			int myPort = in.readInt();
			return myPort;
		// error handling
		} catch (UnknownHostException e){System.out.println("\nSock:"+e.getMessage()); 
		} catch (EOFException e){System.out.println("\nEOF:"+e.getMessage());
		} catch (IOException e){
			System.out.println("\nIO:"+e.getMessage());
			System.out.println("Broker is currently unavailable.\nShutting down.");
			System.exit(0);
		} finally {if(s!=null) try {s.close();}catch (IOException e){/*close failed*/}}			
		return -1;
	}
}

/*
Class used to communicate with the broker.
Takes user input on whether to add or remove a service.
 */
class BrokerCom implements Runnable{
	public static int brokerPort = 49155;
	Scanner inputScanner;
	public static String[] serviceOptionList = {"1", "2"}; // Add number options to increase the number of available services
	public static String serviceOptionString = "\n1. Random Number Generator\n2. Hash Generator"; // Additional services needed to be added to this string when updated.

	public BrokerCom(){
		this.inputScanner = new Scanner(System.in);
	}
	
	public void run(){
		String userInput;
		while(true){
			System.out.println("\nWould you like to:\n1. Add a service\n2. Remove a service");
			userInput = inputScanner.nextLine();
			userInput = validateOption(1, userInput);
			if (userInput.equals("1")){
				System.out.println("\nWhat sevice would you like to add:"); 
				System.out.println(serviceOptionString);
				userInput = inputScanner.nextLine();
				userInput = validateOption(2, userInput);
				System.out.println("\nContacting Service Broker to fulfull request.");
				addService(userInput);
			}else{
				System.out.println("\nWhat sevice would you like to remove:");
				System.out.println(serviceOptionString);
				userInput = inputScanner.nextLine();
				userInput = validateOption(2, userInput);
				System.out.println("\nContacting Service Broker to fulfull request.");
				removeService(userInput);
			}
		}
	}
	
	public String validateOption(int mode, String userInput){
		if (mode == 1){ // Checks for add or remove service
			while(!(userInput.equals("1") || userInput.equals("2"))){
				System.out.println("\nPlease enter the number 1 or 2.\nWould you like to:\n1. Add a service\n2. Remove a service");
				userInput = inputScanner.nextLine();
			}			
		}else{ // Checks for desired service.
			while(!(Arrays.asList(serviceOptionList).contains(userInput))){
				System.out.println("\nPlease enter a number associated with the listed services.");
				System.out.println(serviceOptionString);
				userInput = inputScanner.nextLine();
			}	
		}
		return userInput;	
	}
	
	public void addService(String serviceNum){
		Socket s = null;
		try{
			s = new Socket("localhost", brokerPort);
			DataInputStream in = new DataInputStream( s.getInputStream());
			DataOutputStream out =new DataOutputStream( s.getOutputStream());
			out.writeUTF("addService");
			out.writeUTF(requestUName());
			out.writeUTF(requestPassword());
			int isValid = in.readInt();
			while(isValid == -1){
				System.out.println("\nUsername or password was incorrect.\nPlease try again.");
				out.writeUTF(requestUName());
				out.writeUTF(requestPassword());
				isValid = in.readInt();
			}
			System.out.println("\nUsername and password accepted!");
			// Add two factor authentication here
			out.writeUTF(serviceNum);
			if(in.readBoolean()){System.out.println("\nThe Service was successfully added to the broker.");}
			else{System.out.println("\nYou already provide this service.\nIt cannot be added again.");}
			s.close();	
		} catch (UnknownHostException e){System.out.println("\nSock:"+e.getMessage()); 
		} catch (EOFException e){System.out.println("\nEOF:"+e.getMessage());
		} catch (IOException e){
			System.out.println("\nIO:"+e.getMessage());
			System.out.println("Broker is currently unavailable.\nShutting down.");
			System.exit(0);			
		} finally {if(s!=null) try {s.close();}catch (IOException e){/*close failed*/}}
		
	}
	
	public void removeService(String serviceNum){
		Socket s = null;
		try{
			s = new Socket("localhost", brokerPort);
			DataInputStream in = new DataInputStream( s.getInputStream());
			DataOutputStream out =new DataOutputStream( s.getOutputStream());
			out.writeUTF("removeService");
			out.writeUTF(requestUName());
			out.writeUTF(requestPassword());
			int isValid = in.readInt();
			while(isValid == -1){
				System.out.println("\nUsername or password was incorrect.\nPlease try again.");
				out.writeUTF(requestUName());
				out.writeUTF(requestPassword());
				isValid = in.readInt();
			}			
			System.out.println("\nUsername and password accepted!");
			// Add two factor authentication here			
			out.writeUTF(serviceNum);
			if(in.readBoolean()){System.out.println("\nThe Service was successfully removed from the broker.");}
			else{System.out.println("\nYou do not provide this service.\nIt cannot be removed.");}
			s.close();				
		} catch (UnknownHostException e){System.out.println("\nSock:"+e.getMessage()); 
		} catch (EOFException e){System.out.println("\nEOF:"+e.getMessage());
		} catch (IOException e){
			System.out.println("\nIO:"+e.getMessage());
			System.out.println("Broker is currently unavailable.\nShutting down.");
			System.exit(0);			
		} finally {if(s!=null) try {s.close();}catch (IOException e){/*close failed*/}}
	}
	
	public String requestUName(){
		System.out.println("\nPlease enter your username:");
		return inputScanner.nextLine();
	}
	
	public String requestPassword(){
		System.out.println("\nPlease enter your password:");
		return inputScanner.nextLine();		
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
			if(request.equals("1")){
				getRandNum(in, out);
			}
			else if(request.equals("2")){
				
			}
			/* Additional else if statements can be added for new services.
			The serviceID number is what goes into the request.equals() statement.
			*/
			
			
		// error handling
		} catch(EOFException e) {System.out.println("EOF:"+e.getMessage());
		} catch(IOException e) {System.out.println("IO:"+e.getMessage());
		} finally { try {clientSocket.close();}catch (IOException e){/*close failed*/}}
	}
	// Get a new class that implements Serializable and just has functions
	public void getRandNum(DataInputStream in, DataOutputStream out){
		try{
			
		// error handling
		} catch(EOFException e) {System.out.println("EOF:"+e.getMessage());
		} catch(IOException e) {System.out.println("IO:"+e.getMessage());
		} finally { try {clientSocket.close();}catch (IOException e){/*close failed*/}}
		
	}
	
}