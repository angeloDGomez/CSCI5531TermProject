import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Random;

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
	
	public String getUName(){
		return userName;
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
			if(request.equals("addService")){
				
			}else if(request.equals("removeService")){
				
			}else if(request.equals("registerProvider")){
				int provPort = generatePort();
				boolean invalidUser = true;
				String userName = "";
				while(invalidUser){
					userName = in.readUTF();
					out.writeBoolean(nameTaken(userName));
					invalidUser = in.readBoolean();
				}
				
			}else{//else statement is only called by Service Requester
				
			}	
		// error handling
		} catch(EOFException e) {System.out.println("EOF:"+e.getMessage());
		} catch(IOException e) {System.out.println("IO:"+e.getMessage());
		} finally { try {clientSocket.close();}catch (IOException e){/*close failed*/}}
	}
	
	public int generatePort(){
		Random rand = new Random();
		int provPort = 49155;
		while(MyBroker.providerPorts.contains(provPort)){
			provPort = rand.nextInt((65535 - 49152) + 1) + 49152;
		}
		MyBroker.providerPorts.add(provPort);
		return provPort;
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
	/*
	// i/o stuff needs to be done in run()
	// it needs the try catch stuff
	public void registerProvider(){
		// validate unique userName String user = in.readUTF();
		
		
		String pass = in.readUTF();
		String provIP = clientSocket.getInetAddress().toString();
		//BrokerItem newProvider = new BrokerItem(provIP, Integer.toString(provPort), user, pass);
		out.writeInt(provPort);
		//System.out.printf("New provider " + %s + " has been registerd.\n". user);
		}*/
	
}