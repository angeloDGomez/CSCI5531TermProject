import java.net.*;
import java.io.*;
import java.util.Scanner;
import java.util.Arrays;

public class ServiceRequester{
	static Scanner inputScanner = new Scanner(System.in);
	// List with available function names
	public static String[] serviceIDs = {"1", "2"};// add new service IDs here
	public static String serviceList = "1. Random Number Generator\n2. Hash Generator";// type out name of service with associated ID here
	
	public static void main(String[] args){
		String userChoice = getUserInput();
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
		System.out.printf("Hello,\n"+
		"Which of the following would you like to do: (Please enter a number from the options below.)\n%s\n0. Exit\n", serviceList);
		String userInput = inputScanner.nextLine();
		while(!(Arrays.asList(serviceIDs).contains(userInput) || userInput.equals("0"))){
			System.out.println("\nPlease enter a number associated with the listed services.");
			System.out.printf("%s\n0. Exit\n", serviceList);
			userInput = inputScanner.nextLine();
		}
		if(userInput.equals("0")){userInput = "-1";}
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
			System.out.println();
			return getUserInput();
		}	
	}	

	public static void doRequest(String reqNum){
		String[] ipPort = discoverService(reqNum);
		if (ipPort[0].equals("-1") && ipPort[1].equals("-1")){
			System.out.println("\nSorry,\nThat service is not available at this time.");
		}else{
			String providerIP = ipPort[0];
			String providerPort = ipPort[1];
			Socket s = null;
			try{
				s = new Socket(providerIP, Integer.parseInt(providerPort));
				DataInputStream in = new DataInputStream( s.getInputStream());
				DataOutputStream out = new DataOutputStream( s.getOutputStream());
				out.writeUTF(reqNum);
				boolean reqInProgress = true;
				int command;
				String toPrint;
				String toSend;
				while(reqInProgress){
					command = in.readInt();
					switch(command){
						case 1://Print.
							toPrint = in.readUTF();
							System.out.println(toPrint);
						case 2://Print and return user input.
							toPrint = in.readUTF();
							System.out.println(toPrint);
							toSend = inputScanner.nextLine();
							out.writeUTF(toSend);					
					}
					reqInProgress = in.readBoolean();
					
				}
			} catch (UnknownHostException e){System.out.println("\nSock:"+e.getMessage()); 
			} catch (EOFException e){System.out.println("\nEOF:"+e.getMessage());
			} catch (IOException e){System.out.println("\nIO:"+e.getMessage());
			} finally {if(s!=null) try {s.close();}catch (IOException e){/*close failed*/}}			
		}	
	}
	
	// Returns string array with IPAddress at index 1 and Port # at Index 2.
	public static String[] discoverService(String reqNum){
		Socket s = null;
		String[] data = {"-1", "-1"};
		try{
			// Service Broker's port
			int serverPort = 49155;
			s = new Socket("localhost", serverPort);
			DataInputStream in = new DataInputStream( s.getInputStream());
			DataOutputStream out =new DataOutputStream( s.getOutputStream());
			out.writeUTF(reqNum);
			data[0] = in.readUTF(); 
			data[1] = in.readUTF();
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