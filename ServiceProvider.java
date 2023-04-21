import java.net.*;
import java.io.*;
import java.util.Scanner;
import java.util.Random;
import java.util.ArrayList;
import java.util.Arrays;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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
			System.out.println("Please enter your desired username.\n"+
			"It must include an upper case letter, a lower case letter, and a number.");
			String userName = inputScanner.nextLine();
			boolean invalidUser = true;
			while(invalidUser){
				out.writeUTF(userName);
				invalidUser = in.readBoolean();
				if (invalidUser){
					int errorCode = in.readInt();
					if(errorCode == 1){System.out.println("\nThat username is missing required characters.\nPlease try a different username.");} 
					else if(errorCode == 2){System.out.println("\nThat username is taken.\nPlease try a different username.");}
					userName = inputScanner.nextLine();					
				}
			}
			System.out.println("\nPlease enter your desired password.\nIt must include an upper case letter, a lower case letter, and a number.");
			String password = inputScanner.nextLine();
			boolean invalidPassword = true;
			while(invalidPassword){
				out.writeUTF(password);
				invalidPassword = in.readBoolean();
				if (invalidPassword){
					System.out.println("\nThat password is missing required characters.\nPlease try a different password.");
					password = inputScanner.nextLine();
				}
			}	
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
				manageService("addService", userInput);
			}else{
				System.out.println("\nWhat sevice would you like to remove:");
				System.out.println(serviceOptionString);
				userInput = inputScanner.nextLine();
				userInput = validateOption(2, userInput);
				System.out.println("\nContacting Service Broker to fulfull request.");
				manageService("removeService", userInput);
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
	
	public void manageService(String actionName, String serviceNum){
		Socket s = null;
		try{
			s = new Socket("localhost", brokerPort);
			DataInputStream in = new DataInputStream( s.getInputStream());
			DataOutputStream out =new DataOutputStream( s.getOutputStream());
			out.writeUTF(actionName);
			out.writeUTF(requestUName());
			out.writeUTF(requestPassword());
			int isValid = in.readInt();
			int attempts = 0;
			while(isValid == -1){
				attempts = in.readInt();
				System.out.printf("\nYou have %d attempts remaining.\n", (3 -attempts));
				if (attempts == 3){break;}
				System.out.println("\nUsername or password was incorrect.\nPlease try again.");
				out.writeUTF(requestUName());
				out.writeUTF(requestPassword());
				isValid = in.readInt();
			}
			if(attempts == 3){System.out.println("Ending add service process.");}
			else{
				System.out.println("\nUsername and password accepted!\n");
				boolean confirm2FA = in.readBoolean(); // recieve true after 2FA was sent to the provider's IP
				attempts = 0;
				System.out.println("\nPlease enter your two-factor authentication code:");
				String twoFACode = inputScanner.nextLine();
				out.writeUTF(twoFACode);
				confirm2FA = in.readBoolean();
				while(confirm2FA){
					attempts = in.readInt();
					System.out.printf("\nYou have %d attempts remaining.\n", (3 -attempts));
					if (attempts == 3){break;}
					System.out.println("\nTwo-Factor authentication code was incorrect.\nPlease try again.");
					twoFACode = inputScanner.nextLine();
					out.writeUTF(twoFACode);
					confirm2FA = in.readBoolean();
				}
				if (attempts == 3){System.out.println("Ending add service process.");}
				else{
					out.writeUTF(serviceNum);
					if (actionName.equals("addService")){
						if(in.readBoolean()){System.out.println("\nThe Service was successfully added to the broker.");}
						else{System.out.println("\nYou already provide this service.\nIt cannot be added again.");}
					}else{
						if(in.readBoolean()){System.out.println("\nThe Service was successfully removed from the broker.");}
						else{System.out.println("\nYou do not provide this service.\nIt cannot be removed.");}		
					}
				}
			}
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
			if (request.equals("0")){
				int authCode = in.readInt();
				System.out.printf("The following is your two-factor authentication code:\n%d\n", authCode);
				out.writeBoolean(true);
			}
			else if(request.equals("1")){
				getRandNum(in, out);
			}
			else if(request.equals("2")){
				getHash(in, out);
			}
			/* Additional else if statements can be added for new services.
			The serviceID number is what goes into the request.equals() statement.
			*/
		// error handling
		} catch(EOFException e) {System.out.println("EOF:"+e.getMessage());
		} catch(IOException e) {System.out.println("IO:"+e.getMessage());
		} finally { try {clientSocket.close();}catch (IOException e){/*close failed*/}}
	}
	
	public void getRandNum(DataInputStream in, DataOutputStream out){
		try{
			int l = 0;
			int h = 0;
			String currIn;
			boolean keepLooping = true;
			// Get lower value of RNG range.
			while(keepLooping){
				out.writeInt(2);
				out.writeUTF("\nPlease enter a integer value for the bottom of your random number's range:");
				currIn = in.readUTF();
				keepLooping = ensureInt(currIn);
				if (keepLooping){
					out.writeBoolean(true);
					out.writeInt(1);
					out.writeUTF("\nThis is not an integer value.");
				}
				else{l = Integer.parseInt(currIn);}
				out.writeBoolean(true);	
			}
			keepLooping = true;
			// Get upper value of RNG range.
			while(keepLooping){
				out.writeInt(2);
				out.writeUTF("\nPlease enter a integer value for the top of your random number's range:");
				currIn = in.readUTF();
				keepLooping = ensureInt(currIn);
				if(keepLooping){
					out.writeBoolean(true);
					out.writeInt(1);
					out.writeUTF("\nThat is not an integer value.");
				}else if (Integer.parseInt(currIn) <= l){
					out.writeBoolean(true);
					out.writeInt(1);
					out.writeUTF("\nPlease enter a higher value.");
					keepLooping = true;
				}else{h = Integer.parseInt(currIn);}
				out.writeBoolean(true);
			}
			// Calculate and return random number.
			Random rand = new Random();
			int randomNum = rand.nextInt((h - l) + 1) + l;
			out.writeInt(1);
			String finalMsg = String.format("\nYour random number is %d.", randomNum);
			out.writeUTF(finalMsg);
			out.writeBoolean(false);
		// error handling
		} catch(EOFException e) {System.out.println("EOF:"+e.getMessage());
		} catch(IOException e) {System.out.println("IO:"+e.getMessage());
		} finally { try {clientSocket.close();}catch (IOException e){/*close failed*/}}	
	}
	
	public boolean ensureInt(String intToTest){
		try{
			int x = Integer.parseInt(intToTest);
			return false;
		}catch(NumberFormatException e){
			return true;
		}
	}
	
	public void getHash(DataInputStream in, DataOutputStream out){
		try{
			String stringToHash = "";
			String hashedString = "";
			int hashType = 0;
			String currIn;
			boolean keepLooping = true;
			String[] hashMethodID = {"1", "2", "3"}; // add more hash method IDs here
			String hashMethodString = "\nWhat hashing method would you like to implement: (Please enter a number from the options below)\n"+
			"1. MD5\n2. SHA-1\n3. SHA-256"; // add more hashing methods here if implemented
			// Get the user's desired hashing method.
			while(keepLooping){
				out.writeInt(2);
				out.writeUTF(hashMethodString);
				currIn = in.readUTF();
				keepLooping = validHashID(hashMethodID, currIn);
				if (keepLooping){
					out.writeBoolean(true);
					out.writeInt(1);
					out.writeUTF("\nPlease enter a number associated with a hashing method");
				}else{hashType = Integer.parseInt(currIn);}
				out.writeBoolean(true);
			}
			// Get the string the user wishes to hash.
			out.writeInt(2);
			out.writeUTF("\nPlease enter the message you would like to hash:");
			stringToHash = in.readUTF();
			out.writeBoolean(true);
			// Print out hashed message.
			out.writeInt(1);
			hashedString = calcHash(hashType, stringToHash);
			out.writeUTF("\nYour hashed input is:\n");
			out.writeBoolean(true);
			out.writeInt(1);
			out.writeUTF(hashedString);
			out.writeBoolean(false);
		// error handling
		} catch(EOFException e) {System.out.println("EOF:"+e.getMessage());
		} catch(IOException e) {System.out.println("IO:"+e.getMessage());
		} finally { try {clientSocket.close();}catch (IOException e){/*close failed*/}}	
	}
	
	public boolean validHashID(String[] hashMethodID, String testHashID){
		if (Arrays.asList(hashMethodID).contains(testHashID)){
			return false;
		}else{return true;}
	}
	
	public String calcHash(int hashType, String strToHash){
		// MD5
		if (hashType == 1){
			try{
				MessageDigest md = MessageDigest.getInstance("MD5");
				byte[] messageDigest = md.digest(strToHash.getBytes());				
				BigInteger no = new BigInteger(1, messageDigest);
				String hashtext = no.toString(16);
				while (hashtext.length() < 32) {hashtext = "0" + hashtext;}
				return hashtext;
			}catch (NoSuchAlgorithmException e){
				throw new RuntimeException(e);
			}			
		// SHA-1
		}else if(hashType == 2){
			try{
				MessageDigest md = MessageDigest.getInstance("SHA-1");
				byte[] messageDigest = md.digest(strToHash.getBytes()); 
				BigInteger no = new BigInteger(1, messageDigest);	 
				String hashtext = no.toString(16);
				while (hashtext.length() < 32) {hashtext = "0" + hashtext;}
				return hashtext;				
			}catch (NoSuchAlgorithmException e){
				throw new RuntimeException(e);
			}
		// SHA-256
		}else if (hashType ==3){
			try{
				MessageDigest md = MessageDigest.getInstance("SHA-256");
				byte[] messageDigest = md.digest(strToHash.getBytes());
				BigInteger no = new BigInteger(1, messageDigest);
				String hashtext = no.toString(16);
				while (hashtext.length() < 32) {hashtext = "0" + hashtext;}
				return hashtext;				
			}catch (NoSuchAlgorithmException e){
				throw new RuntimeException(e);
			}			
		}
		
		return "";
	}
	
}