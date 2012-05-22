import java.net.*;
import java.io.*;
import java.util.*;

// This class is only used in PUSH mode. It represents a push client which keeps checking the message buffer for messages.
// Once a message is detected in the buffer, it sends it to the Push Server which should be running & clears the buffer.
public class PushClient extends Thread{

  private long exptStartTime;

  // private attributes to be set in constructor
  private MessageBuffer msgBuffer;
  private String ipOfEventListener;
  private int portOfServer;
  private int period;

  // Constructor
  public PushClient (MessageBuffer msgBuffer, String ipOfEventListener, int portOfServer, int period){
    this.msgBuffer = msgBuffer;
    this.ipOfEventListener = ipOfEventListener;
    this.portOfServer = portOfServer;
    this.period = period;
  }

  // Returns true when its time to stop this whole thing
  private boolean exptTimeUp (){
    return (new Date().getTime()-exptStartTime >= (period*1000));
  }

  // Run method
  public void run(){
    exptStartTime = (new Date()).getTime();

    // create client to server (Event Listener)
    Socket socket = null;
    PrintWriter out = null;

    try {
      socket = new Socket (ipOfEventListener, portOfServer);
      out = new PrintWriter(socket.getOutputStream(), true);

      while (true){

        while (msgBuffer.lock.tryLock()) {
			// keep polling msgBuffer
	        while (!exptTimeUp() & msgBuffer.isEmpty())
	          ; // get stuck here until msgBuffer contains something or experiment period is up

	        

	        // send message to Event Listener & clear message buffer
	        out.println(msgBuffer.getWholeMsg());
	        System.out.println("Sent: "+ msgBuffer.getWholeMsg());
	        System.out.println("---");
	        msgBuffer.clear();
			msgBuffer.lock.unlock();
		}
		
		// exit if period for experiment is up
        if (exptTimeUp())
          break;

      }
      // Cleanup
      out.close();
      socket.close();
    }
    catch (IOException e) {
      System.err.println("*** FATAL ERROR: " + e.getMessage());
      System.err.println("*** LIKELY REASON: Make sure Push Server is up and listening at the correct IP address and port.");
      System.exit(1);
    }
    catch (Exception e){
      System.err.println("*** FATAL ERROR: " + e.getMessage());
      System.exit(1);
    }
    System.out.println("PushClient exited normally...");
  }
}
