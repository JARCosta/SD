package pt.tecnico.distledger.server;

import java.io.IOException;

public class ShutdownHook implements Runnable {
    public void run() {
		
      System.out.println("Press enter to shutdown");
      try {
        System.in.read();
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
}