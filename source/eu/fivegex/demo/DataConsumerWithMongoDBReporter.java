// SimpleConsumerUDP.java
// Author: Poe
// Date: Sept 2016

package eu.fivegex.demo;


import eu.reservoir.monitoring.distribution.udp.UDPDataPlaneConsumer;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Scanner;

/**
 * This receives measurements from a UDP Data Plane.
 */
public class DataConsumerWithMongoDBReporter {
    // The Basic consumer
    MongodbConsumer consumer;

    /*
     * Construct a SimpleConsumerUDP
     */
    public DataConsumerWithMongoDBReporter(String addr, int dataPort, String dbAddr, int dbPort, String dbName) {
        // set up a BasicConsumer
        //consumer = new BasicConsumer();

        // set up a BasicConsumer for MongoDB
        consumer = new MongodbConsumer(dbAddr, dbPort, dbName);

        // set up an IP address for data
        InetSocketAddress address = new InetSocketAddress(addr, dataPort);

        // set up data plane
        consumer.setDataPlane(new UDPDataPlaneConsumer(address));

        consumer.connect();

    }
    public static void main(String [] args) {
        String currentHost="localhost";
        String mongoDBAddress = "192.168.56.102";
        int mongoDBPort = 27017;
        String mongDBName = "test";
        
        int port = 22997;
        try {
            currentHost = InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
        }

        if (args.length == 0) {
            new DataConsumerWithMongoDBReporter(currentHost, port, mongoDBAddress, mongoDBPort, mongDBName);
            System.err.println("DataConsumerWithMongoDBReporter listening on " + currentHost + "/" + port);
        } else if (args.length == 2) {
            String addr = args[0];

            Scanner sc = new Scanner(args[1]);
            port = sc.nextInt();

            new DataConsumerWithMongoDBReporter(addr, port, mongoDBAddress, mongoDBPort, mongDBName);
            System.err.println("DataConsumerWithMongoDBReporter listening on " + addr + "/" + port);
        } else {
            System.err.println("usage: DataConsumerWithMongoDBReporter localhost port");
            System.exit(1);
        }
    }

}
    

