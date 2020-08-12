package eHills;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import javax.imageio.ImageIO;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.Socket;
//import com.google.gson.Gson;
//import com.google.gson.GsonBuilder;
import java.net.URL;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class ClientMain extends Application{

  private static String host = "127.0.0.1";
  private static String userName;
  private BufferedReader reader;
  private PrintWriter writer;
  private Scanner consoleInput = new Scanner(System.in);
  private Stage curr = null;
  private VBox history = new VBox();
  private ArrayList<Item> database;
  private ObjectInputStream ois;
  private ArrayList<Label> userHistory = new ArrayList<Label>();
  private VBox pHistory = new VBox();
  private Map<String, Label> itemState = new HashMap<String, Label>();

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
	  curr = new Stage();
	  curr.setOnCloseRequest(new EventHandler<WindowEvent>() {
		  @Override
		  public void handle(WindowEvent event) {
			  try {
				reader.close();
				writer.close();
				ois.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			  System.exit(0);
		  }
	  });
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
	  curr.setTitle("Auction Screen:" + userName);
	  //main pane
	  BorderPane bp = new BorderPane();
	  //tab pane for listings and history
	  TabPane tb = new TabPane();
	  //Creating tabs
	  //auctions tab (listings)
	  Tab auc = new Tab("Auctions");
	  auc.setClosable(false);
	  //scroll pane for the listings under the 
	  ScrollPane sp = new ScrollPane();
	  sp.setPadding(new Insets(10, 30, 10, 30));
	  sp.vbarPolicyProperty().setValue(ScrollPane.ScrollBarPolicy.ALWAYS);
	  BackgroundFill bf1 = new BackgroundFill(Color.SKYBLUE, CornerRadii.EMPTY, Insets.EMPTY);
  	  Background back1 = new Background(bf1);
  	  sp.setBackground(back1);
	  VBox listing = new VBox();
	  listing.setMaxWidth(sp.getMaxWidth());
	  listing.setBackground(back1);
	  listing.setAlignment(Pos.TOP_CENTER);
	  listing.setSpacing(20);
	  listing.setMaxWidth(900);
	  
	  for(Item i: database) {
		  GridPane g = new GridPane();
		  g.setStyle("-fx-border-color: black");
		  g.setPadding(new Insets(10, 10, 10, 10));
		  g.setVgap(10);
		  g.setHgap(10);
		  Image image = new Image(i.pic, 200, 0, true, false);
		  ImageView iv = new ImageView(image);
		  GridPane.setConstraints(iv, 0, 0);
		  Label name  = new Label(i.product);
		  GridPane.setConstraints(name, 0, 1);
		  Label currPrice;
		  if(!i.open) {
			  currPrice  = new Label("Bid Closed");
		  } else {
			  currPrice  = new Label("Bid Value at: " + i.highestBid);
		  }
		  itemState.put(i.product, currPrice);
		  GridPane.setConstraints(currPrice, 1, 1);
		  TextField value = new TextField();
		  GridPane.setConstraints(value, 0, 2);
		  Button bid = new Button("Make Bid");
		  GridPane.setConstraints(bid, 1, 2);
		  
		  bid.setOnAction(new EventHandler<ActionEvent>() {
			  @Override
			  public void handle(ActionEvent event) {
				  String input = value.getText();
				  if(!input.equals("")) {
					  Double num = Double.parseDouble(input);
					  writer.println("BID " + i.product + " " + userName + " " + num);
					  writer.flush();
				  } else {
					  writer.println("BID " + i.product + " " + userName + " " + (i.highestBid + 5.00));
					  writer.flush();
				  }
			  }
		  });
		  
		  g.getChildren().addAll(iv, name, currPrice, value, bid);
		  listing.getChildren().add(g);
	  }
	  //add everything to each other for the auction tab
	  sp.setContent(listing);
	  auc.setContent(sp);
	  //Create History Tab
	  Tab hist = new Tab("Personal History");
	  hist.setClosable(false);
	  if(userHistory != null) {
		  pHistory.getChildren().addAll(userHistory);
	  }
	  pHistory.setPadding(new Insets(50, 50, 50, 50));
	  ScrollPane sp_pHist = new ScrollPane();
	  sp_pHist.vbarPolicyProperty().setValue(ScrollPane.ScrollBarPolicy.ALWAYS);
	  sp_pHist.setContent(pHistory);
	  hist.setContent(sp_pHist);
	  
	  tb.getTabs().add(auc);
	  tb.getTabs().add(hist);
	  //Create Logout Button
	  VBox exit = new VBox();
	  exit.setAlignment(Pos.TOP_RIGHT);;
	  Button logout = new Button("Logout");
	  logout.setOnAction(new EventHandler<ActionEvent>() {
		  @Override
		  public void handle(ActionEvent event) {
			  String name = userName;
			  writer.println("LOGOUT " + name);
			  writer.flush();
		  }
	  });
	  exit.getChildren().add(logout);
	  
	  BackgroundFill bf = new BackgroundFill(Color.DARKKHAKI, CornerRadii.EMPTY, Insets.EMPTY);
  	  Background back = new Background(bf);
  	  exit.setBackground(back);
  	  history.setBackground(back);
  	  exit.setStyle("-fx-border-color: black");
  	  history.setStyle("-fx-border-color: black");
  	  tb.setStyle("-fx-border-color: black");
  	  
  	  exit.setPadding(new Insets(10, 10, 10, 10));
  	  history.setPadding(new Insets(10, 10, 10, 10));
  	  
  	  ScrollPane sp_Hist = new ScrollPane();
	  sp_Hist.vbarPolicyProperty().setValue(ScrollPane.ScrollBarPolicy.ALWAYS);
	  sp_Hist.setContent(history);
	  
	  Image logo = new Image(getClass().getResourceAsStream("/pics/logo.png"));
	  ImageView logoIV = new ImageView(logo);
	  
	  bp.setCenter(tb);
	  bp.setBottom(exit);
	  bp.setRight(sp_Hist);
	  bp.setTop(logoIV);
	  
	  bp.setBackground(back1);
	  
	  Scene scene = new Scene(bp, 900, 1000);
	  curr.setScene(scene);
	  curr.show();
  }
  
  private void updateItemState(String itemName, String state) {
	  if(!itemState.isEmpty()) {
		  itemState.get(itemName).setText(state);
	  }
  }
  
  private void updateItem(String product, String bidder, Double bidValue) {
	  for(Item i: database) {
		  if(i.product.equals(product)) {
			  i.validBid(bidValue, bidder);
		  }
	  }
  }
  
  private void updateHistory(String latest) {
	  Label recent = new Label(latest);
	  history.getChildren().add(0, recent);
  }
  
  private void updatePHist(String personal) {
	  Label pHist = new Label(personal);
	  pHist.setFont(Font.font("Courier New", 24));
	  pHistory.getChildren().add(0, pHist);
  }
  
  public void alertPopUp(Integer error) {
	  Stage alert = new Stage();
	  alert.setTitle("Error");
	  VBox alertMessage = new VBox();
	  alertMessage.setAlignment(Pos.CENTER);
	  Label mess = new Label();
	  switch(error) {
	  	case 0:
	  		mess.setText("User already exists. Try Again.");
	  		break;
	  		
	  	case 1:
	  		mess.setText("User does not exist or User is already logged in. Try Again.");
	  		break;
	  		
	  	case 2:
	  		mess.setText("Invalid Bid. Try Again");
	  		break;
	  		
	  	case 3:
	  		mess.setText("Item Auction Closed. Try Another Item");
	  		break;
	  		
	  	default:
	  		break;
	  }
	  
	  alertMessage.getChildren().add(mess);
	  
	  Scene scene = new Scene(alertMessage, 300, 150);
	  alert.setScene(scene);
	  alert.show();
  }

  private void setUpNetworking() throws Exception {
    @SuppressWarnings("resource")
    Socket socket = new Socket(host, 4242);
    System.out.println("Connecting to... " + socket);
    reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    writer = new PrintWriter(socket.getOutputStream());

    InputStream is = socket.getInputStream();
    ois = new ObjectInputStream(is);
    database = (ArrayList<Item>) ois.readObject();
    ArrayList<String> bidhistory =(ArrayList<String>) ois.readObject();
    for(String s: bidhistory) {
    	updateHistory(s);
    }
    
//    ois.close();
//    is.close();
    
    Thread aThread = new Thread(new Auctioner());

    aThread.start();
    System.out.println("Connected");
    login();
  }
  
  class Auctioner implements Runnable {
	  
	  @Override
	  public void run() {
		  String fromServer;
		  
		  try {
			  while((fromServer = reader.readLine()) != null) {
				  String[] todo = fromServer.split(" ");
				  
				  if(todo[0].equals("LOGIN")) {
					  try {
						  ArrayList<String> pers = (ArrayList<String>) ois.readObject();
						  for(String s: pers) {
							  userHistory.add(new Label(s));
						  }
						} catch (ClassNotFoundException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}
					  Platform.runLater(new Runnable() {
						  @Override
						  public void run() {
							  auctionScreen();
						  }
					  });
				  } else if(todo[0].equals("ERROR")) {
					  Integer error = Integer.parseInt(todo[1]);
					  Platform.runLater(new Runnable() {
						  @Override
						  public void run() {
							  alertPopUp(error);
						  }
					  });
				  } else if(todo[0].equals("LOGOUT")) {
					  Platform.runLater(new Runnable() {
						  @Override
						  public void run() {
							  curr.close();
							  userHistory.clear();
							  pHistory.getChildren().clear();
							  login();
						  }
					  });
				  } else if(todo[0].equals("BID")) {
					  String update = todo[2] + " BIDDED " + todo[3] + " for " + todo[1];
					  String state = "Bid Value at: " + todo[3];
					  Platform.runLater(new Runnable() {
						  @Override
						  public void run() {
							  Double val = Double.parseDouble(todo[3]);
							  updateItem(todo[1], todo[2], val);
							  updateItemState(todo[1], state);
							  updateHistory(update);
							  if(todo[2].equals(userName)) {
								  updatePHist(update);
							  }
						  }
					  });
				  } else if(todo[0].equals("PURCHASE")) {
					  String update = todo[1] + " WON by " + todo[2] + " for " + todo[3];
					  Platform.runLater(new Runnable() {
						  @Override
						  public void run() {
							  Double val = Double.parseDouble(todo[3]);
							  updateItem(todo[1], todo[2], val);
							  updateItemState(todo[1], "Bid Closed");
							  updateHistory(update);
							  if(todo[2].equals(userName)) {
								  updatePHist(update);
							  }
						  }
					  });
				  }
			  }
		  } catch (Exception e) {
			  
		  }
	  }
  }

}
