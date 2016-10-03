// UDPTransmitter.java
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
 * This is a UDP transmitter for monitoring messages
 */
public final class UDPTransmitterSyncReply {
    /*
     * The transmitting that interacts with a DataSourceDelegate.
     */
    Transmitting transmitting = null;

    /*
     * The socket being transmitted to
     */
    DatagramSocket socket;

    /*
     * A packet being transmitted 
     */
    DatagramPacket packet;

    /*
     * A packet being received 
     */
    DatagramPacket replyPacket;
    
    /*
     * The destination IP dstAddress
     */
    InetAddress dstAddress;

    /*
     * The destination dsPort
     */
    int dsPort;

    
    static int PACKET_SIZE = 65535; // was 1500;
    
    /**
     * Construct a transmitter for a particular IP destination Address
     */
    public UDPTransmitterSyncReply(Transmitting transmitting, InetSocketAddress dstAddr) throws IOException {
        this(transmitting);
        
	this.dstAddress = dstAddr.getAddress();
	this.dsPort = dstAddr.getPort();
        
	packet.setAddress(dstAddress);
	packet.setPort(dsPort);
        
        //check
        //connect();
    }
    
     /**
     * Construct a transmitter without a particular IP dstAddress
     */
    public UDPTransmitterSyncReply(Transmitting transmitting) throws IOException {
        this.transmitting=transmitting;
	socket = new DatagramSocket();
        
        // allocate emtpy packet for sending messages later
	packet = new DatagramPacket(new byte[1], 1);
        
        // allocate emtpy packet for receiving messages later
        replyPacket = new DatagramPacket(new byte[PACKET_SIZE], PACKET_SIZE);
    }
    

    /**
     * Connect to the remote dstAddress now
     */
    public void connect()  throws IOException {
	// connect to the remote UDP socket
        socket.connect(dstAddress, dsPort);

    }

    /**
     * End the connection to the remote dstAddress now
     */
    public void end()  throws IOException {
	// disconnect now
	//socket.disconnect();
        socket.close();
    }

    /**
     * Send a message to UDP dstAddress,  with a given id.
     */
    public int transmit(ByteArrayOutputStream byteStream, int id) throws IOException {
	// set up the packet
	packet.setData(byteStream.toByteArray());
	packet.setLength(byteStream.size());

	// now send it
	socket.send(packet);
        
	// notify the transmitting object
	if (transmitting != null) {
	    transmitting.transmitted(id);
        }

	return byteStream.size();
    }
    
    
    public Object transmitAndWaitReply(ByteArrayOutputStream byteStream) throws IOException, TypeException {
        packet.setData(byteStream.toByteArray());
	packet.setLength(byteStream.size());

	// now send it
	socket.send(packet);

	// notify the transmitting object
	if (transmitting != null) {
            //DatagramPacket replyPacket = new DatagramPacket(new byte[PACKET_SIZE], PACKET_SIZE);

            //this sets the timeout to 5 secs
            socket.setSoTimeout(5000);
            
            // we block this thread until a reply message is received
            socket.receive(replyPacket);
            //System.out.println("Received reply from Src address: " + replyPacket.getAddress() + " Src port: " + replyPacket.getPort());
            
            if (transmitting instanceof TransmittingAndReceiving) {
                ByteArrayInputStream theBytes = new ExposedByteArrayInputStream(replyPacket.getData(), 0, replyPacket.getLength());
                UDPTransmissionMetaData metaData = new UDPTransmissionMetaData(replyPacket.getLength(), replyPacket.getAddress(), this.dstAddress, replyPacket.getPort());
                return ((TransmittingAndReceiving)transmitting).receivedReply(theBytes, metaData);
            }
           
        }

	return null;    
    }
    
    
    public Object transmitAndWaitReply(ByteArrayOutputStream byteStream, UDPControlTransmissionMetaData MessageMetaData, int id) throws IOException, TypeException {
        // setting destination parameters in the packet now        
        packet.setAddress(MessageMetaData.getAddress());
	packet.setPort(MessageMetaData.getPort());
        
        return transmitAndWaitReply(byteStream);
    }    
}
