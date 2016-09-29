package eu.fivegex.demo;

import eu.fivegex.monitoring.control.udp.UDPDataSourceControlPlaneConsumer;
import eu.reservoir.monitoring.core.ControllableDataSource;
import eu.reservoir.monitoring.core.plane.ControlPlane;
import eu.reservoir.monitoring.distribution.udp.UDPDataPlaneProducer;
import eu.reservoir.monitoring.im.dht.DHTDataSourceInfoPlane;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Scanner;
import eu.reservoir.monitoring.core.DataSourceInteracter;

/**
 * This DataSource in a basic control point for probes that uses a Control Plane and an Info Plane and can be daemonized 
 */
public final class SimpleDataSourceDaemon extends Thread {
    ControllableDataSource ds;

    /*
     * Construct a SimpleDataSource with no loaded probes running as a daemon.
     */
    public SimpleDataSourceDaemon(String myDsName, 
                           String dataConsumerName, 
                           int dataConsumerPort,
                           String infoPlaneRootName,   
                           int infoPlaneRootPort,
                           int infoPlaneLocalPort,
                           String localControlEndPoint,
                           int controlPlaneLocalPort,
                           int controlRemotePort) throws UnknownHostException, FileNotFoundException, IOException {
        
        this.attachShutDownHook();
        
	// set up data source
	ds = new ControllableBasicDataSource(myDsName);

        
        System.out.println("Sending data to: " + dataConsumerName + ":" + dataConsumerPort);
        
        System.out.println("Using local host name: " + myDsName);
        
        System.out.println("Connecting to InfoPlaneRoot using : " + infoPlaneLocalPort + ":" + infoPlaneRootName + ":" + infoPlaneRootPort);
        System.out.println("Connecting to ControPlane using: " + controlPlaneLocalPort + ":" + myDsName);
        
        System.out.println("DataSource ID: " + ds.getID());

        // the below string gets the PID splitting PID@hostname
        System.out.println("Process ID: " + java.lang.management.ManagementFactory.getRuntimeMXBean().getName().split("@")[0]);
        
        // this will run in background, we log output in a log file
        File logFile = new File("/tmp/ds.log");
        
        if (!logFile.exists()) {
            System.out.println("Creating File");
	    logFile.createNewFile();
	  }
        
        if (!logFile.canWrite()) {
            //we should be able to write in our user home dir
            logFile = new File(System.getProperty("user.home" + "/ds.log"));
            if (!logFile.exists())
               logFile.createNewFile(); 
        }
        
        System.out.println("Redirecting output to " + logFile.getName());
        
        // closing the std streams to detach the process from the terminal and allowing ssh remote instantiation
        System.out.close(); 
        System.err.close();
        System.in.close();
        
        
        
	PrintStream ps = new PrintStream(logFile);
	System.setOut(ps);
        System.setErr(ps);
        
	// set up an IPaddress for data
	InetSocketAddress DataAddress = new InetSocketAddress(InetAddress.getByName(dataConsumerName), dataConsumerPort);

        InetSocketAddress ctrlAddress = new InetSocketAddress(InetAddress.getByName(localControlEndPoint), controlPlaneLocalPort);
        
        //  we are assuming here that the infoplane and control plane host of the controller are the same
        InetSocketAddress ctrlRemoteAddress = new InetSocketAddress(InetAddress.getByName(infoPlaneRootName), controlRemotePort);
        
	// set up data plane
	ds.setDataPlane(new UDPDataPlaneProducer(DataAddress));
        
        // set up control plane: a data source is a consumer of Control Messages 
        // ctrlAddress is the address:port where this DS will listen for ctrl messages
        // ctrlRemoteAddress is the port where the controller is listening for announce messages
        ControlPlane controlPlane = new UDPDataSourceControlPlaneConsumer(ctrlAddress, ctrlRemoteAddress);
        ((DataSourceInteracter)controlPlane).setDataSource(ds);
        
        ds.setControlPlane(controlPlane);
        
        //the root of the DHT is by default on infoPlaneRootName 6699
        ds.setInfoPlane(new DHTDataSourceInfoPlane(infoPlaneRootName, infoPlaneRootPort, infoPlaneLocalPort));
        
	if (!ds.connect()) 
            System.exit(1); //terminating as there was an error while connecting to the planes
        
        ds.getDataSourceDelegate().addDataSourceInfo(ds);
    }


    @Override
    public void run() {
        System.out.println("Removing information from InfoPlane before shutting down");
        try {
            this.ds.getInfoPlane().removeDataSourceInfo(ds);
        } catch (NullPointerException e) {
            System.out.println("It looks like we are not connected to the infoPlane - there is nothing to remove");
          }
    }
    
    public void attachShutDownHook() {
        Runtime.getRuntime().addShutdownHook(this);
    }
    
    
    public static void main(String [] args) {
        //System.out.println(args.length);
        try {
            String dsName = null;
            String dataConsumerAddr = null;
            int dataConsumerPort = 22997;
            String infoHost = null;
            int infoRemotePort= 6699;
            int infoLocalPort = 9999;
            String controlEndPoint = null;
            int controlLocalPort = 1111;
            int controllerRemotePort = 8888;
            
            if (args.length == 0) {
                // use existing settings
                String loopBack = InetAddress.getLoopbackAddress().getHostName();
                System.out.println("No arguments provided - running on loopback: " + loopBack);
                dsName = dataConsumerAddr = infoHost = controlEndPoint = loopBack;
                
            } else if (args.length == 6) {
                //dsName = args[0];
                
                dataConsumerAddr = args[0];
                
                Scanner sc = new Scanner(args[1]);
                dataConsumerPort = sc.nextInt();
                
                infoHost = args[2];
                
                sc = new Scanner(args[3]);
                infoRemotePort = sc.nextInt();
                
                sc= new Scanner(args[4]);
                infoLocalPort = sc.nextInt();
                
                // controlEndPoint=args[6];
                
                sc= new Scanner(args[5]);
                controlLocalPort = sc.nextInt();
                
                dsName = controlEndPoint = InetAddress.getLocalHost().getHostName();
                //dsName = controlEnd.getHostName(); 
                //System.out.println(controlEnd.toString());
                System.out.println(dsName);
                
            } else {
                System.err.println("use: SimpleDataSourceDaemon dcAddress dcPort infoHost infoRemotePort infoLocalPort controlLocalPort");
                System.exit(1);
            }            
            
            /*
            dsName: is saved in the infoplane to be used as the control endpoint for the DS (and as DS name)
            dataConsumerAddr: address of the destination dataConsumer 
            dataConsumerPort: port of the destination dataConsumer
            infoHost: host where the infoplane root node is running
            infoRemotePort: port where the info plane root node is listening
            infoLocalPort: port to be used by this DS to connect to the info plane
            controlLocalPort: port to be used locally for the contro plane
            */

            SimpleDataSourceDaemon hostMon = new SimpleDataSourceDaemon(dsName, 
                                                            dataConsumerAddr, 
                                                            dataConsumerPort, 
                                                            infoHost, 
                                                            infoRemotePort, 
                                                            infoLocalPort, 
                                                            controlEndPoint, 
                                                            controlLocalPort,
                                                            controllerRemotePort);
            
        } catch (Exception ex) {
            System.out.println("Error while starting the Data Source " + ex.getMessage());
	}
    }
}
