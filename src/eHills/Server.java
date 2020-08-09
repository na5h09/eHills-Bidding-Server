package eHills;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;

//import com.google.gson.Gson;

class Server extends Observable {
	
	private static Map<String, ClientHandler> currUsers;
	private static List<String> users;
	private static List<Item> auctionList;

  public static void main(String[] args) {
    new Server().runServer();
  }

  private Server() {
	  currUsers = new HashMap<String, ClientHandler>();
	  users = new ArrayList<String>();
	  auctionList = new ArrayList<Item>(); //change to JSON reader output
  }
  
  private void runServer() {
    try {
      setUpNetworking();
    } catch (Exception e) {
      e.printStackTrace();
      return;
    }
  }

  private void setUpNetworking() throws Exception {
    @SuppressWarnings("resource")
    ServerSocket serverSock = new ServerSocket(4242);
    while (true) {
      Socket clientSocket = serverSock.accept();
      System.out.println("Connecting to... " + clientSocket);

      ClientHandler handler = new ClientHandler(this, clientSocket);
      this.addObserver(handler);

      Thread t = new Thread(handler);
      t.start();
    }
  }

  protected void processRequest(String[] input) {
    String output = "ERROR";
	try {
		if(input[0].equals("CREATE")) {
			if(!users.contains(input[1])) {
				users.add(input[1]);
				output = "LOGIN";
			} else {
				System.out.println("User already exists");
			}
		} else if(input[0].equals("LOGIN")) {
			if(users.contains(input[1])) {
				output = "LOGIN";
			} else {
				System.out.println("User does not exist");
			}
		} else if(input[0].equals("BID")) {
			
		}
		this.setChanged();
		this.notifyObservers(output);
    } catch (Exception e) {
    	e.printStackTrace();
    }
  }
  
  protected class Item {
	  public String product;
	  public Integer maxPrice;
	  public Integer highestBid;
//	  public Image pic;
	  
	  public Item(String product, Integer price) {
		  this.product = product;
		  this.maxPrice = price;
		  this.highestBid = 0;
	  }
	  
	  public void updateBid(Integer newBid) {
		  this.highestBid = newBid;
	  }
  }

}