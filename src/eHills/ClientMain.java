package eHills;

import java.util.Scanner;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ClientMain extends Application{

  private static String host = "127.0.0.1";
  private BufferedReader fromServer;
  private PrintWriter toServer;
  private Scanner consoleInput = new Scanner(System.in);

  public static void main(String[] args) {
    try {
      new ClientMain().setUpNetworking();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  public void start(Stage primaryStage) {
	  login(primaryStage);
	  
  }
  
  public void login(Stage curr) {
	  curr.close();
	  curr = new Stage();
	  curr.setTitle("LOGIN -> eHills");
	  //Set the login portion for user atleast
	  VBox loginScreen = new VBox();
	  loginScreen.setSpacing(15);
	  loginScreen.setAlignment(Pos.CENTER);
	  
	  Label userText = new Label("Username:");
	  TextField user = new TextField();
	  
	  Button login = new Button("Login");
	  login.setOnAction(new EventHandler<ActionEvent>() {
		  @Override
		  public void handle(ActionEvent event) {
			  String currName = user.getText();
			  
			  if(!currName.contentEquals("")) {
				  toServer.println("LOGIN" + currName);
				  toServer.flush();
				  
			  }
		  }
	  });
	  
	  Button create = new Button("Create User");
	  create.setOnAction(new EventHandler<ActionEvent>() {
		  @Override
		  public void handle(ActionEvent event) {
			  
		  }
	  });
	  
	  curr.show();
  }
  
  public void auctionScreen(Stage curr) {
	  curr.close();
	  
	  curr.show();
  }

  private void setUpNetworking() throws Exception {
    @SuppressWarnings("resource")
    Socket socket = new Socket(host, 4242);
    System.out.println("Connecting to... " + socket);
    fromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    toServer = new PrintWriter(socket.getOutputStream());

    Thread aThread = new Thread(new Auctioner());

    aThread.start();
  }

  protected void processRequest(String input) {
    return;
  }

  protected void sendToServer(String string) {
    System.out.println("Sending to server: " + string);
    toServer.println(string);
    toServer.flush();
  }
  
  class Auctioner implements Runnable {
	  
	  @Override
	  public void run() {
		  
	  }
  }

}
