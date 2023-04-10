import java.net.*;
import java.io.*;
import java.util.Scanner;

public class ServiceRequester{
	static Scanner inputScanner = new Scanner(System.in);
	public static void main(String args[]){
		String userChoice = getUserInput();
		while(true){
			if (userChoice.equals("3")){
				System.out.println("\nThank you!\nGood bye!");
				System.exit(0);
			}else if (userChoice.equals("1")){getRandNum();}
			else{getHash();}
			userChoice = contRequest();
		}
	}
	
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
		return userInput;
		
	}
	
	public static String discoverService(String reqName){
		Socket s = null;
		String data = "";
		try{
			// Service Broker's port
			int serverPort = 49155;
			s = new Socket("localhost", serverPort);
			DataInputStream in = new DataInputStream( s.getInputStream());
			DataOutputStream out =new DataOutputStream( s.getOutputStream());
			out.writeUTF(reqName);
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
	public static void getRandNum(){
		String ipPort = discoverService("getRandNum");
		if(ipPort.equals("NA") || ipPort.equals("")){
			System.out.println("\nSorry,\nThat service is not available at this time.");
		}else{
			int dashIndex = getDash(ipPort);
			String ipCurr = ipPort.substring(1, dashIndex);
			String portCurr = ipPort.substring(dashIndex + 1, ipPort.length());
			Socket s = null;
			try{
				s = new Socket(ipCurr, Integer.parseInt(portCurr));	
				DataInputStream in = new DataInputStream( s.getInputStream());
				DataOutputStream out =new DataOutputStream( s.getOutputStream());
				out.writeUTF("getRandNum");
				int[] LH = getLH();
				out.writeInt(LH[0]);
				out.writeInt(LH[1]);
				int myRandomNum = in.readInt();
				s.close();
				System.out.printf("\nYour random number is %d.", myRandomNum);		
			// error handling
			} catch (UnknownHostException e){System.out.println("\nSock:"+e.getMessage()); 
			} catch (EOFException e){System.out.println("\nEOF:"+e.getMessage());
			} catch (IOException e){System.out.println("\nIO:"+e.getMessage());
			} finally {if(s!=null) try {s.close();}catch (IOException e){/*close failed*/}}
		}
	}	
	
	public static void getHash(){
		String ipPort = discoverService("getHash");		
		if(ipPort.equals("NA") || ipPort.equals("")){
			System.out.println("\nSorry,\nThat service is not available at this time.");
		}else{
			int dashIndex = getDash(ipPort);
			String ipCurr = ipPort.substring(1, dashIndex);
			String portCurr = ipPort.substring(dashIndex + 1, ipPort.length());
			Socket s = null;
			try{
				s = new Socket(ipCurr, Integer.parseInt(portCurr));
				DataInputStream in = new DataInputStream( s.getInputStream());
				DataOutputStream out =new DataOutputStream( s.getOutputStream());
				out.writeUTF("getHash");
				String[] HS = getHS();
				out.writeUTF(HS[0]);
				out.writeUTF(HS[1]);
				// function that returns a list with the string to hash and the type of hash.
				
				
				
				
				s.close();
			// error handling
			} catch (UnknownHostException e){System.out.println("\nSock:"+e.getMessage()); 
			} catch (EOFException e){System.out.println("\nEOF:"+e.getMessage());
			} catch (IOException e){System.out.println("\nIO:"+e.getMessage());
			} finally {if(s!=null) try {s.close();}catch (IOException e){/*close failed*/}}
		}
	}
	
	// Returns index of the - in the string.
	public static int getDash(String ipPort){
		int i = 0;
		while(ipPort.charAt(i) != '-'){i++;}
		return i;	
	}

	// Gets Low and High values for rng range.
	public static int[] getLH(){
		int[] LH = new int[2];
		System.out.println("\nPlease enter a integer value for the bottom of your random number's range:");
		LH[0] = ensureInt(inputScanner.nextLine());
		System.out.println("\nPlease enter a integer value for the top of your random number's range:");
		int x = ensureInt(inputScanner.nextLine());
		while(LH[0] >= x){
			System.out.println("\nPlease enter a higher value.");
			x = ensureInt(inputScanner.nextLine());
		}
		LH[1] = x;
		return LH;
	}
	
	// Gets Hash type and String to hash.
	public static String[] getHS(){
		String[] HS = new String[2];
		System.out.println("\nWhat hashing method would you like to implement: (Please enter a number from the options below)"+
		"\n1. MD5\n2. SHA-1\n3. SHA-256");
		String userInput = inputScanner.nextLine();
		while(!(userInput.equals("1") || userInput.equals("2") || userInput.equals("3"))){
			System.out.println("\nPlease enter the number 1, 2, or 3.");
			userInput = inputScanner.nextLine();
		}
		HS[0] = userInput;
		System.out.println("\nPlease enter the message you would like to hash:");
		HS[1] = inputScanner.nextLine();
		return HS;
	}

	/* 
	Ensures that user input is an int
	important for getLH.
	 */
	public static int ensureInt(String userIn){
		String uI = userIn;
		int x = 0;
		while(true){
			try{
				x = Integer.parseInt(uI);
				break;
			}catch (NumberFormatException e){
				System.out.println("\nPlease enter a integer value.");
				uI = inputScanner.nextLine();
			}
		}
		return x;
	}
	
}