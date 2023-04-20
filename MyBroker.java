import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.*;

public class MyBroker{
	// Array List acts as Broker Inventory
	public static ArrayList<BrokerItems> brokerInventory = new ArrayList<BrokerItems>();
	public static ArrayList<Integer> providerPorts = new ArrayList<Integer>();
	
	public static void main(String[] args){
		ServerSocket brokerSocket = null;
		providerPorts.add(49155);
		try {
			brokerSocket = new ServerSocket(49155);
			// get requests infinitely
			while (true){
				Socket clientSocket  = brokerSocket.accept();
				System.out.println("Client connected at " + clientSocket.getInetAddress() + " "+ clientSocket.getPort());
				ClientHandler newClient = new ClientHandler(clientSocket);
				new Thread(newClient).start();
			}
		}
		// error handling
		catch(IOException e){System.out.println("Listen :"+e.getMessage());}
		finally {
			if (brokerSocket != null) {
                try {brokerSocket.close();}
                catch (IOException e) {e.printStackTrace();}
            }
		}
	}
}

// Might change serviceID to a string later
// This class represents the objects in the broker's inventory
class BrokerItems{
	private String IPadd;
	private String portNum;
	private String userName;
	private String password;
	private ArrayList<Integer> services = new ArrayList<Integer>();
	
	public BrokerItems(String IPadd, String portNum, String user, String pass){
		this.IPadd = IPadd;
		this.portNum = portNum;
		this.userName = user;
		this.password = pass;
	}
	
	public String getIPadd(){
		return IPadd;
	}
	
	public String getPortNum(){
		return portNum;
	}
	
	public String getUName(){
		return userName;
	}
	
	public String getPassword(){
		return password;
	}
	
	public boolean addService(int newService){
		if (services.contains(newService)){return false;}
		else{
			services.add(newService);
			return true;
		}
	}
	
	public boolean removeService(int oldService){
		if(services.contains(oldService)){
			services.remove(Integer.valueOf(oldService));
			return true;
		}
		else{return false;}
	}
	
	public boolean serviceAvailable(int serviceID){
		return services.contains(serviceID);
	}
}

class ClientHandler implements Runnable{
	private DataInputStream in;
	private DataOutputStream out;
	private Socket clientSocket;
	
	public ClientHandler(Socket client){
		this.clientSocket = client;
	}
	
	public void run(){
		try{
			in = new DataInputStream(clientSocket.getInputStream());
			out = new DataOutputStream(clientSocket.getOutputStream());
			String request = in.readUTF();
			if(request.equals("addService") || request.equals("removeService")){
				int attempts = 0;
				String currUN = in.readUTF();
				String currPass = in.readUTF();
				int validUP = validUser(currUN, currPass);
				out.writeInt(validUP);
				while(validUP == -1){
					attempts += 1;
					out.writeInt(attempts);
					if (attempts == 3){break;}
					currUN = in.readUTF();
					currPass = in.readUTF();
					validUP = validUser(currUN, currPass);
					out.writeInt(validUP);
				}
				if (attempts < 3){
					BrokerItems currentClient = MyBroker.brokerInventory.get(validUP);
					TwoFA my2FA = new TwoFA(currentClient.getIPadd(), Integer.parseInt(currentClient.getPortNum()));
					Thread twoFAThread = new Thread(my2FA);
					twoFAThread.run();
					try { twoFAThread.join();} 
					catch (InterruptedException e) { System.out.println("Interrupted Exception");}
					int authCode = my2FA.getAuthCode();
					String authCodeS = Integer.toString(authCode);
					boolean confirm2FA = true;
					while(confirm2FA){
						String user2FA = in.readUTF();
						if(user2FA.equals(authCodeS)){
							confirm2FA = false;
							out.writeBoolean(false);
						}else{out.writeBoolean(true);}
					}
					int serviceID = Integer.valueOf(in.readUTF());
					if (request.equals("addService")){
						boolean successfulAdd = currentClient.addService(serviceID);
						out.writeBoolean(successfulAdd);
					}else{
						boolean successfulRemove = currentClient.removeService(serviceID);
						out.writeBoolean(successfulRemove);
					}				
				}
			}
			else if(request.equals("registerProvider")){
				int provPort = generatePort();
				// Get a valid user name.
				boolean invalidUser = true;
				String userName = "";
				while(invalidUser){
					userName = in.readUTF();
					invalidUser = containValidChars(userName);
					if(invalidUser){
						out.writeBoolean(invalidUser);
						out.writeInt(1);
					}
					else{
						invalidUser = nameTaken(userName);
						if (invalidUser){
							out.writeBoolean(invalidUser);
							out.writeInt(2);
						}
					}
				}
				out.writeBoolean(invalidUser);
				// Get a valid password.
				boolean invalidPassword = true;
				String pass = "";
				while (invalidPassword){
					pass = in.readUTF();
					invalidPassword = containValidChars(pass);
					if(invalidPassword){
						out.writeBoolean(invalidPassword);
					}
				}
				out.writeBoolean(invalidPassword);
				String provIP = clientSocket.getInetAddress().toString();
				provIP = provIP.substring(1, provIP.length());
				BrokerItems newProvider = new BrokerItems(provIP, Integer.toString(provPort), userName, pass);
				MyBroker.brokerInventory.add(newProvider);
				System.out.printf("New provider '%s' has been registerd.\n", userName);
				out.writeInt(provPort);
			}
			else{//else statement is only called by Service Requester
				int serviceID = Integer.valueOf(request);
				int brokerSize = MyBroker.brokerInventory.size();
				if (brokerSize > 1){
					Collections.shuffle(MyBroker.brokerInventory);
				}
				int i;
				for (i = 0; i < brokerSize; i++){
					if (MyBroker.brokerInventory.get(i).serviceAvailable(serviceID)){break;}
				}
				if (i == brokerSize){
					out.writeUTF("-1");
					out.writeUTF("-1");
				}else{
					out.writeUTF(MyBroker.brokerInventory.get(i).getIPadd());
					out.writeUTF(MyBroker.brokerInventory.get(i).getPortNum());
				}
			}	
		// error handling
		} catch(EOFException e) {System.out.println("EOF:"+e.getMessage());
		} catch(IOException e) {System.out.println("IO:"+e.getMessage());
		} finally { try {clientSocket.close();}catch (IOException e){/*close failed*/}}
	}
	
	// may need to implment this in a way that it uses the brokerInventory
	public int generatePort(){
		Random rand = new Random();
		int provPort = 49155;
		while(MyBroker.providerPorts.contains(provPort)){
			provPort = rand.nextInt((65535 - 49152) + 1) + 49152;
		}
		MyBroker.providerPorts.add(provPort);
		return provPort;
	}
	
	public boolean containValidChars(String toCheck){
		char x;
		boolean uCaseFlag = false;
		boolean lCaseFlag = false;
		boolean numFlag = false;
		for(int i = 0; i < toCheck.length(); i++){
			x = toCheck.charAt(i);
			if (Character.isDigit(x)){numFlag=true;}
			else if (Character.isUpperCase(x)){uCaseFlag = true;}
			else if (Character.isLowerCase(x)){lCaseFlag = true;}
			if (uCaseFlag && lCaseFlag && numFlag){return false;}
		}
		return true;
	}
	
	public boolean nameTaken(String uname){
		if (MyBroker.brokerInventory.size() == 0){
			return false;
		}
		else{
			boolean avail = true;
			for (int i = 0; i < MyBroker.brokerInventory.size(); i++){
				if (MyBroker.brokerInventory.get(i).getUName().equals(uname)){
					avail = false;
				}
			}
			return !avail;
		}	
	}
	
	public int validUser(String uname, String password){
		int i;
		for (i = 0; i < MyBroker.brokerInventory.size(); i++){
			if (MyBroker.brokerInventory.get(i).getUName().equals(uname)){break;}
		}
		if (i == MyBroker.brokerInventory.size()){return -1;}
		else if (MyBroker.brokerInventory.get(i).getPassword().equals(password)){return i;}
		else{return -1;}	
	}
}

class TwoFA implements Runnable{
	private String ipAdd;
	private int port;
	private int authCode = 0;
	
	public TwoFA(String ipAdd, int port){
		this.ipAdd = ipAdd;
		this.port = port;
	}
	
	public void run(){
		Socket s = null;
		Random rand = new Random();
		try{
			s = new Socket(ipAdd, port);
			DataInputStream in = new DataInputStream( s.getInputStream());
			DataOutputStream out = new DataOutputStream( s.getOutputStream());
			out.writeUTF("0");
			int i;
			for (i = 100000; i >= 1; i = i/10){
				authCode += (rand.nextInt(10) * i);
			}
			out.writeInt(authCode);
			s.close();
		} catch (UnknownHostException e){System.out.println("\nSock:"+e.getMessage()); 
		} catch(EOFException e) {System.out.println("EOF:"+e.getMessage());
		} catch(IOException e) {System.out.println("IO:"+e.getMessage());
		} finally {if(s!=null) try {s.close();}catch (IOException e){/*close failed*/}}
	}
	
	public int getAuthCode(){
		return authCode;
	}
	
}