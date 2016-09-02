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
public class SyncUDPTransmitterAndReceiver {
    /*
     * The transmittingAndReceiving that interacts with a DataSourceDelegate.
     */
    Transmitting transmittingAndReceiving = null;
    
    /*
     * The socket being transmitted to
     */
    DatagramSocket socket;
    
    static int PACKET_SIZE = 65535; // was 1500;
  
    
    
    public SyncUDPTransmitterAndReceiver(Transmitting transmitting) throws IOException {
	this.transmittingAndReceiving = transmitting;
	setUpSocketWithoutPacketAddress();  
    }

    
    void setUpSocketWithoutPacketAddress() throws IOException {
	socket = new DatagramSocket();
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
        
        
        //System.out.println("transmitAndWaitReply ***** ThreadName: --------> " + Thread.currentThread().getName());
        
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
            
            if (transmittingAndReceiving instanceof TransmittingControl) {
                ByteArrayInputStream theBytes = new ExposedByteArrayInputStream(replyPacket.getData(), 0, replyPacket.getLength());
                return ((TransmittingControl)transmittingAndReceiving).receivedReply(theBytes, null);
            }
           
        }

	return null;    
    }
}   
    