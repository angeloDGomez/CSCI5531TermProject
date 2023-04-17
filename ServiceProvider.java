import java.net.*;
import java.io.*;
import java.util.Scanner;
import java.util.Random;
import java.util.ArrayList;

public class ServiceProvider{
	final static int brokerPort = 49155;
	public static Scanner inputScanner = new Scanner(System.in);
	
	public static void main(String[] args){
		int myPort = registerSelf();
		if (myPort == -1){
			System.out.println("ERROR: Something wrong happened in system set up. Shutting Down.");
			System.exit(0);
		}
		
	}

	public static int registerSelf(){
		Socket s = null;
		try{
			s = new Socket("localhost", brokerPort);
			DataInputStream in = new DataInputStream( s.getInputStream());
			DataOutputStream out =new DataOutputStream( s.getOutputStream());
			out.writeUTF("registerProvider");
			System.out.println("Please enter your desired username.\n");
			String userName = inputScanner.nextLine();
			boolean invalidUser = true;
			while(invalidUser){
				out.writeUTF(userName);
				invalidUser = in.readBoolean();
				// Waiting to hear from professor about required length and characters for user name and password.
				if (invalidUser){
					System.out.println("Please enter your desired username.\n");
					userName = inputScanner.nextLine();					
				}
			}
			System.out.println("Please enter your desired password.\n");
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
	final int portNum;
	Scanner inputScanner;

	public BrokerCom(int portNum){
		this.portNum = portNum;
		this.inputScanner = new Scanner(System.in);
	}
	
	public void run(){
		String userInput;
		
	}

}

///		System.out.println("");