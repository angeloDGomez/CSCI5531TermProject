import java.net.*;
import java.io.*;
import java.util.Scanner;
import java.util.Random;
import java.util.ArrayList;

public class ServiceProvider{
	final static int brokerPort = 49155;
	Scanner inputScanner = new Scanner(System.in);
	
	public static main(String[] args){
		registerSelf();
		
	}

	public static registerSelf(){
		Socket s = null;
		try{
			s = new Socket("localhost", providerPort);
			DataInputStream in = new DataInputStream( s.getInputStream());
			DataOutputStream out =new DataOutputStream( s.getOutputStream());
			out.writeUTF("registerProvider");
			System.out.println("Please enter your desired username. (No more than 10 characters long)\n");
			String userName = inputScanner.nextLine();
			boolean invalidUser = true;
			while(invalidUser){
				out.writeUTF(userName);
				invalidUser = in.readBoolean();
				if (userName.length > 10){
					invalidUser = invalidUser || true;
				}else if(userName.length < 1){
					invalidUser = invalidUser || true;
				}
				if (invalidUser){
					System.out.println("Please enter your desired username. (No more than 10 characters long)\n");
					userName = inputScanner.nextLine();					
				}
				out.writeBoolean(invalidUser);
			}
			System.out.println("Please enter your desired password. (No more than 10 characters long)\n");
			String password = inputScanner.nextLine();
			/*
			while(userName.length > 10 || userName.length < 1){
				System.out.println("Please enter your desired password. (No more than 10 characters long)\n");
				password = inputScanner.nextLine();
			}*/
			out.writeUTF(password);			
			int myPort = in.readInt();
			
			
		// error handling
		} catch (UnknownHostException e){System.out.println("\nSock:"+e.getMessage()); 
		} catch (EOFException e){System.out.println("\nEOF:"+e.getMessage());
		} catch (IOException e){
			System.out.println("\nIO:"+e.getMessage());
			System.out.println("Broker is currently unavailable.\nShutting down.");
			System.exit(0);
		} finally {if(s!=null) try {s.close();}catch (IOException e){/*close failed*/}}			
		
		
		System.out.println("");
		
	}
}