/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.fivegex.monitoring.appl.dataconsumers;

/**
 *
 * @author uceeftu
 */

import eu.fivegex.monitoring.control.udp.UDPDataConsumerControlPlaneXDRConsumer;
import eu.reservoir.monitoring.core.DataConsumerInteracter;
import eu.reservoir.monitoring.core.ID;
import eu.reservoir.monitoring.core.plane.ControlPlane;
import eu.reservoir.monitoring.core.plane.InfoPlane;
import eu.reservoir.monitoring.distribution.udp.UDPDataPlaneConsumer;
import eu.reservoir.monitoring.im.dht.DHTDataConsumerInfoPlane;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Scanner;

/**
 * This receives measurements from a UDP Data Plane.
 */
public final class ControllableDataConsumerDaemon extends Thread {
    DefaultControllableDataConsumer consumer;
    
    ID dataConsumerID;
    
    PrintStream outStream;
    PrintStream errStream;

    /*
     * Construct a controllable SimpleControllableDataConsumer
     */
    public ControllableDataConsumerDaemon(String myID,
                                          String dataAddr, 
                                          int dataPort, 
                                          String infoPlaneRootName,   
                                          int infoPlaneRootPort,
                                          int infoPlaneLocalPort,
                                          String controlAddr,
                                          int controlPort,
                                          int controlRemotePort) throws UnknownHostException, IOException {
        
        this.attachShutDownHook();
        
        dataConsumerID = ID.fromString(myID);
	// set up a DefaultControllableDataConsumer
	consumer = new DefaultControllableDataConsumer("controllable-DC", dataConsumerID);
        
        setStreams();
        
        System.out.println("Data Consumer ID: " + consumer.getID());
        System.out.println("Process ID: " + consumer.getMyPID());
        System.out.println("Connecting to the Info Plane using: " + infoPlaneLocalPort + ":" + infoPlaneRootName + ":" + infoPlaneRootPort);
        System.out.println("Connecting to the Control Plane using: " + controlPort + ":" + controlAddr);

	// set up an IP address for data and control
	InetSocketAddress address = new InetSocketAddress(InetAddress.getByName(dataAddr), dataPort);
        InetSocketAddress ctrlAddress = new InetSocketAddress(InetAddress.getByName(controlAddr), controlPort);
        
        //  we are assuming here that the infoplane and control plane host of the controller are the same
        InetSocketAddress ctrlRemoteAddress = new InetSocketAddress(InetAddress.getByName(infoPlaneRootName), controlRemotePort);
        
	// set up data plane
	consumer.setDataPlane(new UDPDataPlaneConsumer(address));
        
        ControlPlane controlPlane = new UDPDataConsumerControlPlaneXDRConsumer(ctrlAddress, ctrlRemoteAddress);
        ((DataConsumerInteracter) controlPlane).setDataConsumer(consumer);
        consumer.setControlPlane(controlPlane);
        
        InfoPlane infoPlane = new DHTDataConsumerInfoPlane(infoPlaneRootName, infoPlaneRootPort, infoPlaneLocalPort);
        ((DataConsumerInteracter) infoPlane).setDataConsumer(consumer);
        consumer.setInfoPlane(infoPlane);

        if (!consumer.connect()) 
            System.exit(1); //terminating as there was an error while connecting to the planes
    }

    
    void setStreams() throws IOException {
        String outFileName = "data-consumer-" + dataConsumerID + "-out.log";
        String errFileName = "data-consumer-" + dataConsumerID + "-err.log";
        
        //String outFileName = "data-consumer-" + "out.log";
        //String errFileName = "data-consumer-" + "err.log";
        
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
            // first performs deannounce and then disconnect for each of the planes
            consumer.disconnect(); 
        } catch (Exception e) {
            System.out.println("Something went wrong while Disconnecting from the planes " + e.getMessage());
          }
    }
    
    public void attachShutDownHook() {
        Runtime.getRuntime().addShutdownHook(this);
    }
    
    
    public static void main(String [] args) {
        try {
            String dcID = ID.generate().toString();
            String dcAddr = null;
            int dataPort = 22997;
            String infoHost = null;
            int infoRemotePort= 6699;
            int infoLocalPort = 10000;
            String controlEndPoint = null;
            int controlLocalPort = 2222;
            int controllerRemotePort = 8888;
            
            Scanner sc;
                    
            switch (args.length) {
                case 0:
                    String loopBack = InetAddress.getLoopbackAddress().getHostName();
                    System.out.println("No arguments provided - running on loopback: " + loopBack);
                    dcAddr = infoHost = controlEndPoint = loopBack;
                    System.out.println("DataConsumerWithMeasurementRate listening on Data plane: " + dcAddr + "/" + dataPort);
                    System.out.println("DataConsumerWithMeasurementRate listening on Control plane: " + controlEndPoint + "/" + controlLocalPort);
                    break;
                case 5:
                    sc = new Scanner(args[0]);
                    dataPort = sc.nextInt();
                    infoHost = args[1];
                    sc = new Scanner(args[2]);
                    infoRemotePort = sc.nextInt();
                    sc= new Scanner(args[3]);
                    infoLocalPort = sc.nextInt();
                    sc= new Scanner(args[4]);
                    controlLocalPort = sc.nextInt();
                    dcAddr = controlEndPoint = InetAddress.getLocalHost().getHostName();
                    break;
                case 6:
                    dcID = args[0];
                    sc = new Scanner(args[1]);
                    dataPort = sc.nextInt();
                    infoHost = args[2];
                    sc = new Scanner(args[3]);
                    infoRemotePort = sc.nextInt();
                    sc= new Scanner(args[4]);
                    infoLocalPort = sc.nextInt();
                    sc= new Scanner(args[5]);
                    controlLocalPort = sc.nextInt();
                    dcAddr = controlEndPoint = InetAddress.getLocalHost().getHostName();
                    //System.err.println("DataConsumerWithMeasurementRate listening on Data plane: " + dcAddr + "/" + dataPort);
                    //System.err.println("DataConsumerWithMeasurementRate listening on Control plane: " + controlEndPoint + "/" + controlLocalPort);
                    break;
                default:
                    System.err.println("usage: SimpleControllableDataConsumer dcID localdataPort infoHost infoRemotePort infoLocalPort controlLocalPort");
                    System.exit(1);
            }
            ControllableDataConsumerDaemon dc = new ControllableDataConsumerDaemon(dcID, 
                                                                                   dcAddr, 
                                                                                   dataPort, 
                                                                                   infoHost, 
                                                                                   infoRemotePort, 
                                                                                   infoLocalPort, 
                                                                                   controlEndPoint, 
                                                                                   controlLocalPort, 
                                                                                   controllerRemotePort);
        } catch (Exception e) {
            System.out.println("Error " + e.getMessage());
        } 

    }
}


