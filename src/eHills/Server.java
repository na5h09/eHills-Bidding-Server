package eHills;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

class Server extends Observable {
	
	private static List<String> currUsers;
	private static List<String> users;
	private static List<Item> auctionList;

  public static void main(String[] args) {
    new Server().runServer();
  }

  private Server() {
	  currUsers = new ArrayList<String>();
	  users = new ArrayList<String>();
	  auctionList = new ArrayList<Item>();
	  
	  JSONParser parser = new JSONParser();
	  try {
		JSONObject jobj = (JSONObject) parser.parse(new FileReader("Product.json"));
		
		JSONArray itemsList = (JSONArray) jobj.get("items");
		for(int i = 0; i < itemsList.size(); i++) {
			JSONObject product = (JSONObject) itemsList.get(i);
			String name = (String) product.get("name");
			Double price = (Double) product.get("price");
			auctionList.add(new Item(name, price));
		}
		
		int i = 0;
		
	} catch (FileNotFoundException e) {
		e.printStackTrace();
	} catch (IOException e) {
		e.printStackTrace();
	} catch (ParseException e) {
		e.printStackTrace();
	}
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
      
      OutputStream os = clientSocket.getOutputStream();
      ObjectOutputStream oos = new ObjectOutputStream(os);
      oos.writeObject(auctionList);

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
				currUsers.add(input[1]);
				output = "LOGIN" + input[1];
			} else {
				System.out.println("User already exists");
			}
		} else if(input[0].equals("LOGIN")) {
			if(users.contains(input[1]) && !currUsers.contains(input[1])) {
				currUsers.add(input[1]);
				output = "LOGIN" + input[1];
			} else {
				System.out.println("User does not exist");
			}
		} else if(input[0].equals("BID")) {
			
		} else if(input[0].equals("LOGOUT")) {
			currUsers.remove(input[1]);
			output = "LOGOUT";
		}
		this.setChanged();
		this.notifyObservers(output);
    } catch (Exception e) {
    	e.printStackTrace();
    }
  }
  
  protected class Item {
	  public String product;
	  public Double maxPrice;
	  public Integer highestBid;
//	  public Image pic;
	  
	  public Item(String product, Double price) {
		  this.product = product;
		  this.maxPrice = price;
		  this.highestBid = 0;
	  }
	  
	  public void updateBid(Integer newBid) {
		  this.highestBid = newBid;
	  }
  }

}