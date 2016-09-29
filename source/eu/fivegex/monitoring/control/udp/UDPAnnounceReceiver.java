// UDPTransmitterAndReceiver.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Oct 2008

package eu.fivegex.monitoring.control.udp;

import eu.reservoir.monitoring.core.TypeException;
import eu.reservoir.monitoring.distribution.*;
import eu.reservoir.monitoring.distribution.udp.UDPTransmissionMetaData;
import java.net.*;
import java.io.*;

/**
 * This is a UDP receiver for Announce messages
 */
public class UDPAnnounceReceiver implements Runnable{
     /*
     * The receiver that interactes messages.
     */
    ReceivingAnnounce receiver = null;

    /*
     * The socket doing the listening
     */
    DatagramSocket socket;

    /*
     * A packet to receive
     */
    DatagramPacket packet;

    /*
     * The IP address
     */
    //InetSocketAddress address;

    InetAddress dstAddr;

    /*
     * The port
     */
    int port;

    /*
     * My thread.
     */
    Thread myThread;

    boolean threadRunning = false;

    /*
     * A default packet size.
     */
    static int PACKET_SIZE = 65535; // was 1500;

    /*
     * The packet contents as a ByteArrayInputStream
     */
    ByteArrayInputStream byteStream;

    /*
     * The InetSocketAddress of the last packet received
     */
    InetAddress srcAddr;

    /*
     * The length of the last packet received
     */
    int length;

    /*
     * The source port of the last packet received
     */
    int srcPort;
    
    /*
     * The last exception received.
     */
    Exception lastException;


    public UDPAnnounceReceiver(ReceivingAnnounce receiving, int port) throws IOException {
	this.receiver = receiving;
	this.port = port;

	setUpSocket();
    }
    

    /**
     * Set up the socket for the given addr/port,
     * and also a pre-prepared DatagramPacket.
     */
    void setUpSocket() throws IOException {
	socket = new DatagramSocket(port);
        socket.setBroadcast(true);

	// allocate an emtpy packet for use later
	packet = new DatagramPacket(new byte[PACKET_SIZE], PACKET_SIZE);
    }

    /**
     * Join the address now
     * and start listening
     */
    public void listen()  throws IOException {
	// already bind to the address
	//socket.bind(address);
        
	// start the thread
	myThread = new Thread(this, "UDPAnnounceReceiver-" + Integer.toString(port));

	myThread.start();
    }

    /**
     * Leave the address now
     * and stop listening
     */
    public void end()  throws IOException {
	// stop the thread
        threadRunning = false;

        //System.err.println("UDPReceiver: about to do socket close");

        socket.close();

        //System.err.println("UDPReceiver: about to do socket disconnect");

        // disconnect
        socket.disconnect();

    }

    /**
     * Receive a  message from the multicast address.
     */
    protected boolean receive() {
	try {
	    // clear lastException
	    lastException = null;

	    // receive from socket
	    socket.receive(packet);

            /*
	     System.out.println("FT: UDPAnnounceReceiver Received " + packet.getLength() +
			   " bytes from "+ packet.getAddress() + 
			   ":" + packet.getPort()); 
            */

	    // get an input stream over the data bytes of the packet
	    ByteArrayInputStream theBytes = new ExposedByteArrayInputStream(packet.getData(), 0, packet.getLength());

	    byteStream = theBytes;
	    srcAddr = packet.getAddress();
	    length = packet.getLength();
            srcPort = packet.getPort();

	    // Currently we reuse the packet.
	    // This could be dangerous.

	    // Maybe we should do this
	    // allocate an emtpy packet for use later
	    // packet = new DatagramPacket(new byte[PACKET_SIZE], PACKET_SIZE);

	    // Reset the packet size for next time
	    packet.setLength(PACKET_SIZE);


	    return true;
	} catch (Exception e) {
	    // something went wrong
	    lastException = e;
	    return false;
	}
    }
    
    /**
     * The Runnable body
     */
    public void run() {
	// if we get here the thread must be running
	threadRunning = true;
        
	while (threadRunning) {
            
	    if (receive()) {
		// construct the transmission meta data
		UDPTransmissionMetaData metaData = new UDPTransmissionMetaData(length, srcAddr, dstAddr, srcPort);


		// now notify the receiver with the message
		// and the address it came in on
		try {
		    receiver.received(byteStream, metaData);
		} catch (IOException ioe) {
		    receiver.error(ioe);
		} catch (TypeException te) {
		    receiver.error(te);
		}
                  catch (Exception e) {
                      receiver.error(e);
                }
                  
	    } else {
		// the receive() failed
		// we find the exception in lastException
		receiver.error(lastException);
	    }
	}
    }
    
    
    
    
    
    
}

