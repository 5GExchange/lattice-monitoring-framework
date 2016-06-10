// UDPTransmitterAndReceiver.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Oct 2008

package eu.reservoir.monitoring.control.udp;

import eu.reservoir.monitoring.core.TypeException;
import eu.reservoir.monitoring.distribution.*;
import java.net.*;
import java.io.*;

/**
 * This is a UDP transmitter for monitoring messages
 */
public class UDPTransmitterAndReceiver implements Runnable{
    /*
     * The transmittingAndReceiving that interacts with a DataSourceDelegate.
     */
    Transmitting transmittingAndReceiving = null;

    /*
     * The IP address being transmitted to
     */
    //InetSocketAddress udpAddr;

    /*
     * The socket being transmitted to
     */
    DatagramSocket socket;

    /*
     * A Packet being received 
     */
    DatagramPacket packet;

    /*
     * The IP address
     */
    //InetAddress address;

    /*
     * The port
     */
    //int port;

    
    static int PACKET_SIZE = 65535; // was 1500;
    
    
    Thread myThread;

    boolean threadRunning = false;
    
    Exception lastException;
    
    /*
     * The packet contents as a ByteArrayInputStream
     */
    ByteArrayInputStream byteStream;
    
    //private InetAddress srcAddr;
    //private int length;
    //private int srcPort;
    
    
    /**
     * Construct a transmitter for a particular IP address
     */
    
    /*
    public UDPTransmitterAndReceiver(Transmitting transmitting, InetSocketAddress dstAddr) throws IOException {
	this.udpAddr = dstAddr;

	this.transmittingAndReceiving = transmitting;
	this.address = dstAddr.getAddress();
	this.port = dstAddr.getPort();
        
	setUpSocket();
    }
    */
    
    public UDPTransmitterAndReceiver(Transmitting transmitting) throws IOException {
	//udpAddr = dstAddr;
	this.transmittingAndReceiving = transmitting;
	//this.address = dstAddr.getAddress();
	//this.port = dstAddr.getPort();

	setUpSocketWithoutPacketAddress();  
    }

    /**
     * Set up the socket for the given addr/port,
     * and also a pre-prepared Datagrapacket.
     */
    
    /*
    void setUpSocket() throws IOException {
	socket = new DatagramSocket();

	// allocate an emtpy packet for use later
	packet = new DatagramPacket(new byte[1], 1);
	packet.setAddress(address);
	packet.setPort(port);
    }
    */
    
    void setUpSocketWithoutPacketAddress() throws IOException {
	socket = new DatagramSocket();

	// allocate an emtpy packet for use later
	packet = new DatagramPacket(new byte[PACKET_SIZE], PACKET_SIZE);
	//packet.setAddress(address);
	//packet.setPort(port);
    }
    

    /**
     * Connect to the remote address now
     */
    /*
    public void connect()  throws IOException {
	// connect to the remote UDP socket
        
	socket.connect(udpAddr);

    }
*/
    
    
    /**
     * End the connection to the remote address now
     */
    public void end()  throws IOException {
	// disconnect now
	socket.disconnect();
    }

    /**
     * Send a message to UDP address,  with a given id.
     */
    public int transmit(ByteArrayOutputStream byteStream, int id) throws IOException {
	// set up the packet
	packet.setData(byteStream.toByteArray());
	packet.setLength(byteStream.size());

	// now send it
	socket.send(packet);
        
        
        
	//System.err.println("trans: " + id + " = " + byteStream.size());

	// notify the transmittingAndReceiving object
	if (transmittingAndReceiving != null) {
	    transmittingAndReceiving.transmitted(id);
        }

	return byteStream.size();
    }
    
    public int transmit(ByteArrayOutputStream byteStream, InetSocketAddress dstAddr, int id) throws IOException {
	// set up the packet
        
        DatagramPacket packetToSend = new DatagramPacket(new byte[PACKET_SIZE], PACKET_SIZE);
        
	packetToSend.setData(byteStream.toByteArray());
	packetToSend.setLength(byteStream.size());

        //TODO: check in case of multiple thread access
        packetToSend.setAddress(dstAddr.getAddress());
        packetToSend.setPort(dstAddr.getPort());
        
	// now send it
	socket.send(packetToSend);
        
        // notify the transmittingAndReceiving object
	if (transmittingAndReceiving != null) {
	    transmittingAndReceiving.transmitted(id);
        }

	return byteStream.size();
    }
    
    
    
    public Object transmitAndWaitReply(ByteArrayOutputStream byteStream, InetSocketAddress dstAddr, int id) throws IOException, TypeException, SocketTimeoutException {
        
        
        System.out.println("transmitAndWaitReply ***** ThreadName: --------> " + Thread.currentThread().getName());
        
        DatagramPacket packetToSend = new DatagramPacket(new byte[PACKET_SIZE], PACKET_SIZE);
        
	packetToSend.setData(byteStream.toByteArray());
	packetToSend.setLength(byteStream.size());

        //TODO: check in case of multiple thread access
        packetToSend.setAddress(dstAddr.getAddress());
        packetToSend.setPort(dstAddr.getPort());
        
	// now send it
	socket.send(packetToSend);
        
        // notify the transmittingAndReceiving object
	if (transmittingAndReceiving != null) {
	    transmittingAndReceiving.transmitted(id);
        }
        
	if (transmittingAndReceiving != null) {
            DatagramPacket replyPacket = new DatagramPacket(new byte[PACKET_SIZE], PACKET_SIZE);

            //this sets the timeout to 5 secs
            socket.setSoTimeout(5000);
            socket.receive(replyPacket);
            System.out.println("Received reply from Src address: " + replyPacket.getAddress() + " Src port: " + replyPacket.getPort());
            System.out.println("transmitAndWaitReply ***** ThreadName: --------> " + Thread.currentThread().getName() + " @ " + System.currentTimeMillis());
            
            if (transmittingAndReceiving instanceof TransmittingControl) {
                ByteArrayInputStream theBytes = new ExposedByteArrayInputStream(replyPacket.getData(), 0, replyPacket.getLength());
                return ((TransmittingControl)transmittingAndReceiving).receivedReply(theBytes, null);
            }
           
        }

	return null;    
    }
    
    
    
    public void listen()  throws IOException {
	// already bind to the address
	//socket.bind(address);
        System.out.println("Starting Receiving thread on port: " + socket.getLocalPort());
	// start the thread
	myThread = new Thread(this, "UDPTransmitterAndReceiver-" + Integer.toString(socket.getLocalPort()));

	myThread.start();
    }
    
    
    public void run() {
	// if we get here the thread must be running
	threadRunning = true;
        
	while (threadRunning) {
            
	    if (receive()) {
		// construct the transmission meta data
		//UDPTransmissionMetaData metaData = new UDPTransmissionMetaData(length, srcAddr, dstAddr, srcPort);


		// now notify the receiver with the message
		// and the address it came in on
		try {
                    
                    
                    if (transmittingAndReceiving instanceof TransmittingControl) {
                        ByteArrayInputStream theBytes = new ExposedByteArrayInputStream(packet.getData(), 0, packet.getLength());
                        ((TransmittingControl)transmittingAndReceiving).receivedReply(theBytes, null);
            
                    }
                    
		} catch (IOException ioe) {
		    //transmittingAndReceiving.error(ioe);
		} catch (TypeException te) {
		    //transmittingAndReceiving.error(te);
		}
	    } else {
		// the receive() failed
		// we find the exception in lastException
		//transmittingAndReceiving.error(lastException);
	    }
	}
    }
    
protected boolean receive() {
	try {
	    // clear lastException
	    lastException = null;

	    // receive from socket
	    socket.receive(packet);

            
	     System.out.println("FT: UDPReceiver Received " + packet.getLength() +
			   " bytes from "+ packet.getAddress() + 
			   ":" + packet.getPort()); 
            

	    // get an input stream over the data bytes of the packet
	    ByteArrayInputStream theBytes = new ExposedByteArrayInputStream(packet.getData(), 0, packet.getLength());

	    byteStream = theBytes;
	    //srcAddr = packet.getAddress();
	    //length = packet.getLength();
            //srcPort = packet.getPort();

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
    
    
    
    
    
    
    
    
}

