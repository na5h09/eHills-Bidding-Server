package eHills;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
//import com.google.gson.Gson;
//import com.google.gson.GsonBuilder;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;

public class ClientMain extends Application{

  private static String host = "127.0.0.1";
  private static String userName;
  private BufferedReader reader;
  private PrintWriter writer;
  private Scanner consoleInput = new Scanner(System.in);
  private Stage curr = null;
  private Map<String, Integer> items = new HashMap<String, Integer>();

  public static void main(String[] args) {
    try {
    	launch(args);
    } catch(Exception e) {e.printStackTrace();}
  }
  
  public void start(Stage primaryStage) {
	  curr = primaryStage;
	  try {
	      new ClientMain().setUpNetworking();
	  } catch (Exception e) {
	      e.printStackTrace();
	  }
	  
  }
  
  public void login() {
	  //curr.close();
	  curr = new Stage();
	  curr.setTitle("LOGIN -> eHills");
	  //Set the login portion for user atleast
	  VBox loginScreen = new VBox();
	  loginScreen.setSpacing(15);
	  loginScreen.setAlignment(Pos.CENTER);
	  
	  Label userText = new Label("Username:");
	  TextField user = new TextField();
	  user.setMaxWidth(250);
	  
	  Button login = new Button("Login");
	  login.setOnAction(new EventHandler<ActionEvent>() {
		  @Override
		  public void handle(ActionEvent event) {
			  String currName = user.getText();
			  
			  if(!currName.contentEquals("")) {
				  userName = currName;
				  writer.println("LOGIN " + currName);
				  writer.flush();
				  
			  }
		  }
	  });
	  
	  Button create = new Button("Create User");
	  create.setOnAction(new EventHandler<ActionEvent>() {
		  @Override
		  public void handle(ActionEvent event) {
			  String newName = user.getText();
			  
			  if(!newName.contentEquals("")) {
				  userName = newName;
				  writer.println("CREATE " + newName);
				  writer.flush();
				  
			  }
		  }
	  });
	  
	  loginScreen.getChildren().addAll(userText, user, login, create);
	  Scene scene = new Scene(loginScreen, 400, 400);
	  scene.setFill(Paint.valueOf(Color.AQUA.toString()));
	  curr.setScene(scene);
	  curr.show();
  }
  
  public void auctionScreen() {
	  curr.close();
	  curr = new Stage();
	  curr.setTitle("Auction Screen");
	  //main pane
	  BorderPane bp = new BorderPane();
	  //tab pane for listings and history
	  TabPane tb = new TabPane();
	  //Creating tabs
	  //auctions tab (listings)
	  Tab auc = new Tab("Auctions");
	  //scroll pane for the listings under the 
	  ScrollPane sp = new ScrollPane();
	  sp.vbarPolicyProperty().setValue(ScrollPane.ScrollBarPolicy.ALWAYS);
	  VBox listing = new VBox();
	  listing.setSpacing(20);
	  listing.setMaxWidth(900);
	  
	  for(Map.Entry<String, Integer> entry: items.entrySet()) {
		  GridPane g = new GridPane();
		  g.setVgap(10);
		  g.setHgap(10);
		  Label name  = new Label(entry.getKey());
		  GridPane.setConstraints(name, 0, 0);
		  Label currPrice  = new Label("Bid Value at: " + entry.getValue());
		  GridPane.setConstraints(currPrice, 1, 0);
		  TextField value = new TextField();
		  GridPane.setConstraints(value, 0, 1);
		  Button bid = new Button("Make Bid");
		  GridPane.setConstraints(bid, 1, 1);
		  
		  bid.setOnAction(new EventHandler<ActionEvent>() {
			  @Override
			  public void handle(ActionEvent event) {
				  String input = value.getText();
				  if(!input.equals("")) {
					  writer.print("BID " + input);
					  writer.flush();
				  } else {
					  writer.println("BID " + (entry.getValue() + 5));
					  writer.flush();
				  }
			  }
		  });
		  
		  g.getChildren().addAll(name, currPrice, value, bid);
		  listing.getChildren().add(g);
	  }
	  //add everything to each other for the auction tab
	  sp.setContent(listing);
	  auc.setContent(sp);
	  //Create History Tab
	  Tab hist = new Tab("Bid History");
	  
	  tb.getTabs().add(auc);
	  tb.getTabs().add(hist);
	  //Create Logout Button
	  VBox exit = new VBox();
	  Button logout = new Button("Logout");
	  logout.setOnAction(new EventHandler<ActionEvent>() {
		  @Override
		  public void handle(ActionEvent event) {
			  writer.print("LOGOUT " + userName);
			  writer.flush();
		  }
	  });
	  bp.setCenter(tb);
	  
	  Scene scene = new Scene(bp, 900, 1000);
	  curr.setScene(scene);
	  curr.show();
  }

  private void setUpNetworking() throws Exception {
    @SuppressWarnings("resource")
    Socket socket = new Socket(host, 4242);
    System.out.println("Connecting to... " + socket);
    reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    writer = new PrintWriter(socket.getOutputStream());

    Thread aThread = new Thread(new Auctioner());

    aThread.start();
    System.out.println("Connected");
    login();
  }

  protected void processRequest(String input) {
    return;
  }

  protected void sendToServer(String string) {
    System.out.println("Sending to server: " + string);
    writer.println(string);
    writer.flush();
  }
  
  class Auctioner implements Runnable {
	  public List<String> bidHistory = new ArrayList<String>();
	  
	  @Override
	  public void run() {
		  String fromServer;
		  
		  try {
			  while((fromServer = reader.readLine()) != null) {
				  String[] todo = fromServer.split(" ");
				  
				  if(todo[0].equals("LOGIN")) {
					  Platform.runLater(new Runnable() {
						  @Override
						  public void run() {
							  auctionScreen();
						  }
					  });
				  } else if(todo[0].equals("ERROR")) {
//					  Platform.runLater(new Runnable() {
//						  @Override
//						  pub
//					  });
				  } else if(todo[0].equals("LOGOUT")) {
					  Platform.runLater(new Runnable() {
						  @Override
						  public void run() {
							  login();
						  }
					  });
				  }
			  }
		  } catch (Exception e) {
			  
		  }
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
