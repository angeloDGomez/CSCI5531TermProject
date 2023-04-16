import java.net.*;
import java.io.*;
import java.util.Scanner;

public class ServiceRequester{
	Scanner inputScanner = new Scanner(System.in);
	String userChoice;
	
	public static void main(String[] args){
		userChoice = getUserInput();
		while(true){
			if (userChoice.equals("-1")){
				System.out.println("\nThank you!\nGood bye!");
				System.exit(0);
			}else{
				doRequest(userChoice);
			}
			userChoice = contRequest();
		}
		
	}
	
	// getUserInput must be updated manually in order for 
	// the requester to gain access to new services in the broker. 
	public static String getUserInput(){
		System.out.println("Hello,\n"+
		"Which of the following would you like to do: (Please enter a number from the options below.)\n"+
		"1. Random Number Generator\n"+
		"2. Hash Generator\n"+
		"3. Exit");
		String userInput = inputScanner.nextLine();
		while(!(userInput.equals("1") || userInput.equals("2") || userInput.equals("3"))){
			System.out.println("\nPlease enter the number 1, 2, or 3.");
			userInput = inputScanner.nextLine();
		}
		if(userInput.equals("3")){userInput = "-1";}
		return userInput;
	}
	
	// Check if user would like to perform another task.
	public static String contRequest(){
		System.out.println("\nWould you like to perform another task? (Y/N)");
		String userInput = inputScanner.nextLine();
		while(!(userInput.equals("Y") || userInput.equals("N") || userInput.equals("y") || userInput.equals("n"))){
			System.out.println("\nPlease enter Y or N.");
			userInput = inputScanner.nextLine();
			System.out.println();
		}
		if (userInput.equals("N") || userInput.equals("n")){
			System.out.println("\nThank you!\nGood bye!");
			System.exit(0);
			return "null";
		}else{
			return getUserInput();
		}	
	}	

	public static void doRequest(String reqNum){
		
	}
	
	public static String discoverService(String reqNum){
		Socket s = null;
		String data = "";
		try{
			// Service Broker's port
			int serverPort = 49155;
			s = new Socket("localhost", serverPort);
			DataInputStream in = new DataInputStream( s.getInputStream());
			DataOutputStream out =new DataOutputStream( s.getOutputStream());
			out.writeUTF(reqNum);
			data = in.readUTF(); 
			s.close();
			return data;
		// error handling
		} catch (UnknownHostException e){System.out.println("\nSock:"+e.getMessage()); 
		} catch (EOFException e){System.out.println("\nEOF:"+e.getMessage());
		} catch (IOException e){System.out.println("\nIO:"+e.getMessage());
		} finally {if(s!=null) try {s.close();}catch (IOException e){/*close failed*/}}
		return data;
	}
}