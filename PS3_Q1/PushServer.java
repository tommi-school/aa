import java.net.*;
import java.io.*;
import java.util.*;

// This class is only used in PUSH mode. It represents a push server which listens at a port & waits for the push client to send messages.
// The push client is expected to send messages whenever it sees messages in the message buffer.
public class PushServer{

  private long exptStartTime;

  // the following variables are used for statistical collection of msg delay values
  private long totalDelayRecorded = 0;
  private int noOfDelaysRecorded = 0;
  private long maxDelayRecorded = 0;
  private long minDelayRecorded = Long.MAX_VALUE;

  // the following variable keeps track of the network traffic costs in number of characters
  private int noOfCharSentThruNetwork = 0;

  // to be populated with data from pushserver.properties
  private int portOfServer;
  private int period;
  private int commOverhead;

  // main method. Where life begins.
  public static void main(String[] args) throws IOException {
    new PushServer().runExperiment();
  }

  // Constructor
  public PushServer(){
    // populate private attributes with values from pushserver.properties
    readPptyFile();
  }

  // returns true when its time to stop this whole thing
  private boolean exptTimeUp (){
    return (new Date().getTime()-exptStartTime >= (period*1000));
  }

  // does what the name implies. will print out to screen information about the message (inputLine) received
  private void processMsgAndCollectStats(String inputLine){
    System.out.println("Received: ");
    // inputLine is in this format: [timestamp]~[msg contents]~
    String[] temp = inputLine.split("~");
    int noOfMsg = temp.length/2;
    System.out.println("Number of messages: " + noOfMsg);

    long currentTime = (new Date()).getTime();
    for (int i=0; i<temp.length; i+=2){
      long msgTime = Long.parseLong(temp[i]);
      String msg = temp[i+1];
      System.out.println("Message timestamp: " + msgTime);
      System.out.println("Message text: " + msg);
      long delay = currentTime - msgTime;
      System.out.println("Message is delayed by: " + delay + " msec");
      System.out.println("---");

      // Collect stats for delays
      totalDelayRecorded += delay;
      noOfDelaysRecorded++;
      maxDelayRecorded = Math.max(maxDelayRecorded, delay);
      minDelayRecorded = Math.min(minDelayRecorded, delay);
    }
  }

  // Everything is done here
  public void runExperiment(){
    exptStartTime = (new Date()).getTime();

    // open server socket to listen to a client (Push Client)
    ServerSocket serverSocket = null;
    try {
      serverSocket = new ServerSocket(portOfServer);
      Socket clientSocket = null;
      BufferedReader in;

      System.out.println("Waiting for new event messages...");
      clientSocket = serverSocket.accept();

      in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

      String inputLine;
      while (!exptTimeUp() & ((inputLine=in.readLine())!=null)){

        // update noOfCharSentThruNetwork for messages received
        noOfCharSentThruNetwork += commOverhead;
        noOfCharSentThruNetwork += inputLine.length();

        processMsgAndCollectStats(inputLine);
      }

      // Will arrive here when period for experiment is up.
      // Clean up
      in.close();
      clientSocket.close();
      serverSocket.close();
      System.out.println("PushServer exited normally...");
    }
    catch (IOException e) {
      System.err.println("*** FATAL ERROR: " + e.getMessage());
    }
    catch (Exception e){
      System.err.println("*** FATAL ERROR: " + e.getMessage());
    }
    finally{
      // Show stats for delays
      if (noOfDelaysRecorded!=0){
        System.out.println("Message delay stats:");
        System.out.println("  Average delay recorded: " + totalDelayRecorded/noOfDelaysRecorded + " msec");
        System.out.println("  Maximum delay recorded: " + maxDelayRecorded + " msec");
        System.out.println("  Minimum delay recorded: " + minDelayRecorded + " msec");
      }
      // Show stats for network traffic
      System.out.println("Network traffic: " + noOfCharSentThruNetwork + " characters");
      System.out.println("---");
    }
  }

  // -------------------------------------------------------------------
  private void readPptyFile (){
    // Read properties file.
    Properties properties = new Properties();
    try {
      properties.load(new FileInputStream("pushserver.properties"));
      portOfServer = Integer.parseInt(properties.getProperty("portOfServer"));
      period = Integer.parseInt(properties.getProperty("period"));
      commOverhead = Integer.parseInt(properties.getProperty("commOverhead"));

      System.out.println("Read the following attributes from pushserver.properties:");
      System.out.println("  portOfServer..: " + portOfServer);
      System.out.println("  period........: " + period);
      System.out.println("  commOverhead..: " + commOverhead);
      System.out.println("---");
    }
    catch (NumberFormatException e){
      System.err.println("*** FATAL ERROR: " + e.getMessage());
      System.err.println("*** LIKELY REASON: Some properties in pushserver.properties are not formatted correctly");
      System.exit(1);
    }
    catch (IOException e) {
      System.err.println("*** FATAL ERROR: " + e.getMessage());
      System.err.println("*** LIKELY REASON: Ensure that pushserver.properties is found in the same directory as EventSource.class and that it is readable");
      System.exit(1);
    }
    catch (Exception e){
      System.err.println("*** FATAL ERROR: " + e.getMessage());
      System.err.println("*** LIKELY REASON: Ensure that pushserver.properties is correctly configured");
      System.exit(1);
    }
  }
}

