import java.net.*;
import java.io.*;
import java.util.ArrayList;

public class ServiceBroker{
	
	// Need a way to store the addresses and ports of service providers.
	// Formatting of data string: IPaddress-Port#
	public static ArrayList<String> rngAvail = new ArrayList<String>();
	public static ArrayList<String> hashAvail = new ArrayList<String>();
	
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

class ClientHandler implements Runnable{
	private DataInputStream in;
	private DataOutputStream out;
	private Socket clientSocket;
	
	public ClientHandler(Socket client){
		this.clientSocket = client;
	}
	/*
	When interacting with the Service Provider, return a boolean based
	on if the add/removal of their IP and port number was successful or 
	return all IP addresses and port numbes currently in use.
	When interacting with the Service Requesters, it returns the 
	IP address and port number formatted together as a string if the service is 
	available and NA if the service is not.
	*/
	public void run(){
		try{
			in = new DataInputStream(clientSocket.getInputStream());
			out = new DataOutputStream(clientSocket.getOutputStream());
			String request = in.readUTF();
			if (request.equals("portAvail")){
				int rngSize = ServiceBroker.rngAvail.size();
				int hashSize = ServiceBroker.hashAvail.size();
				out.writeInt(rngSize + hashSize);
				if (rngSize > 0){
					for (int i = 0; i < rngSize; i++){
						out.writeUTF(ServiceBroker.rngAvail[i]);
					}
				}
				if (hashSize > 0){
					for (int i = 0; i < hashSize; i++){
						out.writeUTF(ServiceBroker.hashAvail[i]);
					}
				}	
			}else if (request.equals("addService")){
				String servPort = in.readUTF();
				String serv = in.readUTF();
				String ipAdd = clientSocket.getInetAddress().toString();
				String dataForList = ipAdd + "-" + servPort;
				if (serv.equals("randNum")){
					if(!ServiceBroker.rngAvail.contains(dataForList)){
						ServiceBroker.rngAvail.add(dataForList);
						out.writeBoolean(true);
					}else{out.writeBoolean(false);}
				}else{
					if(!ServiceBroker.hashAvail.contains(dataForList)){
						ServiceBroker.hashAvail.add(dataForList);
						out.writeBoolean(true);
					}else{out.writeBoolean(false);}					
				}
			}else if (request.equals("removeService")){
				String servPort = in.readUTF();
				String serv = in.readUTF();
				String ipAdd = clientSocket.getInetAddress().toString();
				String dataForList = ipAdd + "-" + servPort;
				if (serv.equals("randNum")){
					if(ServiceBroker.rngAvail.contains(dataForList)){
						ServiceBroker.rngAvail.remove(dataForList);
						out.writeBoolean(true);
					}else{out.writeBoolean(false);}
				}else{
					if(ServiceBroker.hashAvail.contains(dataForList)){
						ServiceBroker.hashAvail.remove(dataForList);
						out.writeBoolean(true);
					}else{out.writeBoolean(false);}	
				}
			}else if (request.equals("getRandNum")){
				if(ServiceBroker.rngAvail.size() == 0){
					out.writeUTF("NA");
				}else{
					int index = (int)(Math.random() * ServiceBroker.rngAvail.size());
					out.writeUTF(ServiceBroker.rngAvail.get(index));
				}
			}else{// getHash()
				if(ServiceBroker.hashAvail.size() == 0){
					out.writeUTF("NA");
				}else{
				int index = (int)(Math.random() * ServiceBroker.hashAvail.size());
				out.writeUTF(ServiceBroker.hashAvail.get(index));	
				}				
			}
		// error handling
		} catch(EOFException e) {System.out.println("EOF:"+e.getMessage());
		} catch(IOException e) {System.out.println("IO:"+e.getMessage());
		} finally { try {clientSocket.close();}catch (IOException e){/*close failed*/}}
	}
}