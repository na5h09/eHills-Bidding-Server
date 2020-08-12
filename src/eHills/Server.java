package eHills;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
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

import javafx.scene.control.Label;

class Server extends Observable implements Serializable {
	
	private static List<String> currUsers;
	private static Map<String, ArrayList<String>> users;
	private static List<String> bidHist;
	private static List<Item> auctionList;

  public static void main(String[] args) {
    new Server().runServer();
  }

  private Server() {
	  currUsers = new ArrayList<String>();
	  users = new HashMap<String, ArrayList<String>>();
	  bidHist = new ArrayList<String>();
	  auctionList = new ArrayList<Item>();
	  
	  JSONParser parser = new JSONParser();
	  try {
		JSONObject jobj = (JSONObject) parser.parse(new FileReader("Product.json"));
		
		JSONArray itemsList = (JSONArray) jobj.get("items");
		for(int i = 0; i < itemsList.size(); i++) {
			JSONObject product = (JSONObject) itemsList.get(i);
			String name = (String) product.get("name");
			Double price = (Double) product.get("price");
			String image = (String) product.get("image");
			auctionList.add(new Item(name, price, image));
		}
		
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
      oos.flush();
      oos.writeObject(bidHist);
      oos.flush();
      
      ClientHandler handler = new ClientHandler(this, clientSocket, oos);
      this.addObserver(handler);
      
//      oos.close();
//      os.close();

      Thread t = new Thread(handler);
      t.start();
    }
  }

  protected String processRequest(String[] input) {
    String output = "ERROR";
	try {
		if(input[0].equals("CREATE")) {
			if(!users.containsKey(input[1])) {
				users.put(input[1], new ArrayList<String>());
				currUsers.add(input[1]);
				output = "LOGIN";
			} else {
				output = "ERROR " + 0;
			}
			
			return output;
		} else if(input[0].equals("LOGIN")) {
			if(users.containsKey(input[1]) && !currUsers.contains(input[1])) {
				currUsers.add(input[1]);
				output = "LOGIN";
			} else {
				output = "ERROR " + 1;
			}
			
			return output;
		} else if(input[0].equals("BID")) {
			Integer index = productIndex(input[1]);
			if(auctionList.get(index).open) {
				Double bid = Double.parseDouble(input[3]);
				if(auctionList.get(index).validBid(bid, input[2])) {
					if(!auctionList.get(index).open) {
						output = "PURCHASE " + input[1] + " " + input[2] + " " + input[3];
						String update = input[1] + " WON by " + input[2] + " for " + input[3];
						users.get(input[2]).add(update);
						bidHist.add(update);
					} else {
						output = "BID " + input[1] + " " + input[2] + " " + input[3];
						String update = input[2] + " BIDDED " + input[3] + " for " + input[1];
						users.get(input[2]).add(update);
						bidHist.add(update);
					}
				} else {
					output = "ERROR " + 2;
					return output;
				}
			} else {
				output = "ERROR " + 3;
				return output;
			}
		} else if(input[0].equals("LOGOUT")) {
			currUsers.remove(input[1]);
			output = "LOGOUT";
			return output;
		}
		this.setChanged();
		this.notifyObservers(output);
    } catch (Exception e) {
    	e.printStackTrace();
    }
	
	return "";
  }
  
  protected ArrayList<String> getUserHist(String user) {
	  return users.get(user);
  }
  
  private Integer productIndex(String name) {
	  for(Item i: auctionList) {
		  if(i.product.equals(name)) {
			  return auctionList.indexOf(i);
		  }
	  }
	  
	  return 0;
  }
  
}