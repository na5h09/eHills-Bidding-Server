package eHills;

/*
 * eHills ClientHandler.java
 * EE422C Final Project submission by
 * Replace <...> with your actual data.
 * <Pranesh Satish>
 * <ps32534>
 * <Student1 5-digit Unique No.>
 * Spring 2020
 */

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.IOException;
import java.net.Socket;
import java.util.Observer;
import java.util.Observable;

class ClientHandler implements Runnable, Observer {

  private Server server;
  private Socket clientSocket;
  private BufferedReader fromClient;
  private PrintWriter toClient;

  protected ClientHandler(Server server, Socket clientSocket) {
    this.server = server;
    this.clientSocket = clientSocket;
    try {
      fromClient = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
      toClient = new PrintWriter(this.clientSocket.getOutputStream());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  protected void sendToClient(String string) {
    System.out.println("Sending to client: " + string);
    toClient.println(string);
    toClient.flush();
  }

  @Override
  public void run() {
    String input;
    try {
      while ((input = fromClient.readLine()) != null) {
        String[] in = input.split(" ");
        String response;
        synchronized(this) {
        	response = this.server.processRequest(in);
        }

        if(!response.equals("")) {
        	this.sendToClient(response);
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void update(Observable o, Object arg) {
    this.sendToClient((String) arg);
  }
}
