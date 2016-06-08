// UDPTransmitter.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Oct 2008

package eu.reservoir.monitoring.distribution.udp;

import eu.reservoir.monitoring.control.udp.TransmittingControl;
import eu.reservoir.monitoring.core.TypeException;
import eu.reservoir.monitoring.distribution.*;
import java.net.*;
import java.io.*;

/**
 * This is a UDP transmitter for monitoring messages
 */
public class UDPTransmitter {
    /*
     * The transmitting that interacts with a DataSourceDelegate.
     */
    Transmitting transmitting = null;

    /*
     * The IP address being transmitted to
     */
    InetSocketAddress udpAddr;

    /*
     * The socket being transmitted to
     */
    DatagramSocket socket;

    /*
     * A packet being transmitted 
     */
    DatagramPacket packet;

    /*
     * The IP address
     */
    InetAddress address;

    /*
     * The port
     */
    int port;

    
    static int PACKET_SIZE = 65535; // was 1500;
    
    /**
     * Construct a transmitter for a particular IP address
     */
    public UDPTransmitter(eu.reservoir.monitoring.distribution.Transmitting transmitting, InetSocketAddress dstAddr) throws IOException {
	udpAddr = dstAddr;

	this.transmitting = transmitting;
	this.address = dstAddr.getAddress();
	this.port = dstAddr.getPort();

        System.out.println("FT: dst address " + this.address);
        System.out.println("FT: dst port " + this.port);
        
        
	setUpSocket();
    }

    /**
     * Set up the socket for the given addr/port,
     * and also a pre-prepared Datagrapacket.
     */
    void setUpSocket() throws IOException {
	socket = new DatagramSocket();

	// allocate an emtpy packet for use later
	packet = new DatagramPacket(new byte[1], 1);
	packet.setAddress(address);
	packet.setPort(port);
    }

    /**
     * Connect to the remote address now
     */
    public void connect()  throws IOException {
	// connect to the remote UDP socket
        
	socket.connect(udpAddr);

    }

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

	// notify the transmitting object
	if (transmitting != null) {
	    transmitting.transmitted(id);
        }

	return byteStream.size();
    }
    
    
    public Object transmitAndWaitReply(ByteArrayOutputStream byteStream) throws IOException, TypeException {
        /* testing receive */
        
        packet.setData(byteStream.toByteArray());
	packet.setLength(byteStream.size());

	// now send it
	socket.send(packet);
        
	//System.err.println("trans: " + id + " = " + byteStream.size());

	// notify the transmitting object
	if (transmitting != null) {
            DatagramPacket replyPacket = new DatagramPacket(new byte[PACKET_SIZE], PACKET_SIZE);

            //this sets the timeout to 5 secs
            socket.setSoTimeout(5000);
            socket.receive(replyPacket);
            System.out.println("Received reply from Src address: " + replyPacket.getAddress() + " Src port: " + replyPacket.getPort());
            
            if (transmitting instanceof TransmittingControl) {
                ByteArrayInputStream theBytes = new ExposedByteArrayInputStream(replyPacket.getData(), 0, replyPacket.getLength());
                return ((TransmittingControl)transmitting).receivedReply(theBytes, null);
            }
           
        }

	return null;    
    }
    
}
