package eHills;

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
  private ObjectOutputStream oos;

  protected ClientHandler(Server server, Socket clientSocket, ObjectOutputStream oos) {
    this.server = server;
    this.clientSocket = clientSocket;
    this.oos = oos;
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
  
  protected void sendObject(Object o) {
	  System.out.println("Sending Object");
	  try {
		oos.writeObject(o);
		oos.flush();
	} catch (IOException e) {
		e.printStackTrace();
	}
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
        	try {
				Thread.sleep(500);
				if(response.equals("LOGIN")) {
	        		this.sendObject(this.server.getUserHist(in[1]));
	        	} 
			} catch (InterruptedException e) {
				e.printStackTrace();
			}       	
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
