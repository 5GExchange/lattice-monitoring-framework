package eu.fivegex.monitoring.appl.datasources;

import eu.reservoir.monitoring.core.DefaultControllableDataSource;
import eu.fivegex.monitoring.control.udp.UDPDataSourceControlPlaneXDRConsumer;
import eu.reservoir.monitoring.core.ControllableDataSource;
import eu.reservoir.monitoring.core.ID;
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

/**
 * This DataSource in a basic control point for probes that uses a Control Plane and an Info Plane and 
 * logs out/err to a file rather than standard streams.
 **/
public final class DataSourceDaemon extends Thread {
    ControllableDataSource dataSource;
    
    ID dataSourceID;
    
    PrintStream outStream;
    PrintStream errStream;
    
    /**
     * Construct a SimpleDataSource with no loaded probes running as a daemon.
     **/
    
    public DataSourceDaemon(
                           String myID,
                           String myDsName, 
                           String dataConsumerName, 
                           int dataConsumerPort,
                           String infoPlaneRootName,   
                           int infoPlaneRootPort,
                           int infoPlaneLocalPort,
                           String localControlEndPoint,
                           int controlPlaneLocalPort,
                           int controlRemotePort) 
           throws UnknownHostException, FileNotFoundException, IOException {
        
        this.attachShutDownHook();
        
        dataSourceID = ID.fromString(myID);
	dataSource = new DefaultControllableDataSource(myDsName, dataSourceID);
        
        setStreams();
        
        System.out.println("DataSource ID: " + dataSource.getID());
        System.out.println("Process ID: " + dataSource.getMyPID());
        System.out.println("Using host name: " + myDsName);
        System.out.println("Sending measurements to Data Consumer: " + dataConsumerName + ":" + dataConsumerPort);
        System.out.println("Connecting to the Info Plane using: " + infoPlaneLocalPort + ":" + infoPlaneRootName + ":" + infoPlaneRootPort);
        System.out.println("Connecting to the Control Plane using: " + controlPlaneLocalPort + ":" + myDsName);
        
	// set up an IPaddress for data and control
	InetSocketAddress DataAddress = new InetSocketAddress(InetAddress.getByName(dataConsumerName), dataConsumerPort);
        InetSocketAddress localCtrlAddress = new InetSocketAddress(InetAddress.getByName(localControlEndPoint), controlPlaneLocalPort);
        
        //  we are assuming here that the remote infoplane and control plane hosts are the same (i.e., the Controller address)
        InetSocketAddress remoteCtrlAddress = new InetSocketAddress(InetAddress.getByName(infoPlaneRootName), controlRemotePort);
        
        // set up control plane: a data source is a consumer of Control Messages 
        // localCtrlAddress is the address:port where this DS will listen for ctrl messages
        // remoteCtrlAddress is the remote address/port where the controller is listening for announce messages
        ControlPlane controlPlane = new UDPDataSourceControlPlaneXDRConsumer(localCtrlAddress, remoteCtrlAddress);
        
	// set up the planes
	dataSource.setDataPlane(new UDPDataPlaneProducer(DataAddress));
        dataSource.setControlPlane(controlPlane);
        dataSource.setInfoPlane(new DHTDataSourceInfoPlane(infoPlaneRootName, infoPlaneRootPort, infoPlaneLocalPort));
        
	if (!dataSource.connect()) 
            System.exit(1); //terminating as there was an error while connecting to the planes
    }


    void setStreams() throws IOException {
        String outFileName = "data-source-" + dataSourceID + "-out.log";
        String errFileName = "data-source-" + dataSourceID + "-err.log";
        
        //String outFileName = "data-source-" + "out.log";
        //String errFileName = "data-source-" + "err.log";
        
        File outLogFile;
        File errLogFile;
        
        outLogFile = new File("/tmp/" + outFileName);
        errLogFile = new File("/tmp/" + errFileName);
        
        if (!outLogFile.exists()) {
	    outLogFile.createNewFile();
	}
        
        if (!outLogFile.canWrite()) {
            outLogFile = new File(System.getProperty("user.home") + "/" + outFileName);
            if (!outLogFile.exists())
               outLogFile.createNewFile(); 
        }
        
        if (!errLogFile.exists()) {
	    errLogFile.createNewFile();
	}
        
        if (!errLogFile.canWrite()) {
            errLogFile = new File(System.getProperty("user.home") + "/" + errFileName);
            if (!errLogFile.exists())
               errLogFile.createNewFile(); 
        }
        
        outStream = new PrintStream(outLogFile);
        errStream = new PrintStream(errLogFile);
	
        System.setOut(outStream);
        System.setErr(errStream);
        System.in.close();
    }
    
    
    @Override
    public void run() {
        System.out.println("Disconnecting from the planes before shutting down");
        try {
            // will first do deannounce and then disconnect from each of the planes
            dataSource.disconnect();
        } catch (Exception e) {
            System.err.println("Something went wrong while disconnecting from the planes " + e.getMessage());
          }
        finally {
            outStream.close();
            errStream.close();
        }
    }
    
    public void attachShutDownHook() {
        Runtime.getRuntime().addShutdownHook(this);
    }
    
    
    public static void main(String [] args) {
        try {
            String dsID = ID.generate().toString();
            String dsName = null;
            String dataConsumerAddr = null;
            int dataConsumerPort = 22997;
            String infoHost = null;
            int infoRemotePort= 6699;
            int infoLocalPort = 9999;
            String controlEndPoint = null;
            int controlLocalPort = 1111;
            int controllerRemotePort = 8888;
            
            Scanner sc;
                    
            switch (args.length) {
                case 0:
                    // use existing settings
                    String loopBack = InetAddress.getLoopbackAddress().getHostName();
                    dsName = dataConsumerAddr = infoHost = controlEndPoint = loopBack;
                    break;
                case 6:
                    dataConsumerAddr = args[0];
                    sc = new Scanner(args[1]);
                    dataConsumerPort = sc.nextInt();
                    infoHost = args[2];
                    sc = new Scanner(args[3]);
                    infoRemotePort = sc.nextInt();
                    sc= new Scanner(args[4]);
                    infoLocalPort = sc.nextInt();
                    sc= new Scanner(args[5]);
                    controlLocalPort = sc.nextInt();
                    dsName = controlEndPoint = InetAddress.getLocalHost().getHostName();
                    break;
                case 7:
                    dsID = args[0];
                    dataConsumerAddr = args[1];
                    sc = new Scanner(args[2]);
                    dataConsumerPort = sc.nextInt();
                    infoHost = args[3];
                    sc = new Scanner(args[4]);
                    infoRemotePort = sc.nextInt();
                    sc= new Scanner(args[5]);
                    infoLocalPort = sc.nextInt();
                    sc= new Scanner(args[6]);
                    controlLocalPort = sc.nextInt();
                    dsName = controlEndPoint = InetAddress.getLocalHost().getHostName();
                    break;    
                default:
                    System.err.println("use: SimpleDataSourceDaemon dsID dcAddress dcPort infoHost infoRemotePort infoLocalPort controlLocalPort");
                    System.exit(1);
            }            
            
            /*
            dsID: is the UUID this Data Source will use
            dsName: is saved in the infoplane to be used as the control endpoint for the DS (and as DS name)
            dataConsumerAddr: address of the destination dataConsumer 
            dataConsumerPort: port of the destination dataConsumer
            infoHost: host where the infoplane root node is running
            infoRemotePort: port where the info plane root node is listening
            infoLocalPort: port to be used by this DS to connect to the info plane
            controlLocalPort: port to be used locally for the contro plane
            */
            
            DataSourceDaemon hostMon = new DataSourceDaemon(
                                                            dsID,
                                                            dsName, 
                                                            dataConsumerAddr, 
                                                            dataConsumerPort, 
                                                            infoHost, 
                                                            infoRemotePort, 
                                                            infoLocalPort, 
                                                            controlEndPoint, 
                                                            controlLocalPort,
                                                            controllerRemotePort);
        } catch (Exception ex) {
            System.err.println("Error while starting the Data Source " + ex.getMessage());
	}
    }
}
