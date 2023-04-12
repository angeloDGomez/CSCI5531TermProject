import java.net.*;
import java.io.*;
import java.util.ArrayList;

public class MyBroker{
	// Array List acts as Broker Inventory
	public static ArrayList<BrokerItems> brokerInventory = new ArrayList<BrokerItems>();
	
	public static void main(String[] args){
		ServerSocket brokerSocket = null;
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
	public String IPadd;
	public String portNum;
	public int serviceID; 
	
	public BrokerItems(String IPadd, String portNum, int serviceID){
		this.IPadd = IPadd;
		this.portNum = portNum;
		this.serviceID = serviceID;		
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
			
			
			
		// error handling
		} catch(EOFException e) {System.out.println("EOF:"+e.getMessage());
		} catch(IOException e) {System.out.println("IO:"+e.getMessage());
		} finally { try {clientSocket.close();}catch (IOException e){/*close failed*/}}
		
		
	}
}