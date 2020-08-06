package eHills;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;

import com.google.gson.Gson;

class Server extends Observable {
	
	private static Map<String, ClientHandler> currUsers;
	private static List<String> users;
	private static List<Item> auctionList;

  public static void main(String[] args) {
    try {
    	new Server().runServer();
    } catch (Exception e) {e.printStackTrace();}
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

  protected void processRequest(String input) {
    String output = "Error";
    Gson gson = new Gson();
    Message1 message = gson.fromJson(input, Message1.class);
    try {
      String temp = "";
      switch (message.type) {
        case "upper":
          temp = message.input.toUpperCase();
          break;
        case "lower":
          temp = message.input.toLowerCase();
          break;
        case "strip":
          temp = message.input.replace(" ", "");
          break;
      }
      output = "";
      for (int i = 0; i < message.number; i++) {
        output += temp;
        output += " ";
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