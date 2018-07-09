/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.fivegex.monitoring.appl.probes.delay.unidirectional;

import eu.reservoir.monitoring.appl.datarate.EveryNSeconds;
import eu.reservoir.monitoring.core.AbstractProbe;
import eu.reservoir.monitoring.core.Probe;
import eu.reservoir.monitoring.core.ProbeMeasurement;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author uceeftu
 */
public class DelaySourceProbe extends AbstractProbe implements Probe {
    int packets;
    int timeout;
    int dataRate;
    
    DatagramSocket dataSocket;
    DatagramSocket mgmSocket;
    
    InetAddress dataDestinationAddr;
    int dataDestinationPort;
    
    private Logger LOGGER = LoggerFactory.getLogger(DelaySourceProbe.class);
    
    
    
    public DelaySourceProbe(String probeName, 
                          String mgmLocalAddr,
                          String mgmLocalPort,
                          String dataLocalAddr,
                          String dataLocalPort,
                          String dataDestinationAddr, 
                          String dataDestinationPort,
                          String packets,
                          String timeout,
                          String dataRate) throws SocketException, UnknownHostException {
        
        dataSocket = new DatagramSocket(Integer.valueOf(dataLocalPort), InetAddress.getByName(dataLocalAddr));
        mgmSocket = new DatagramSocket(Integer.valueOf(mgmLocalPort), InetAddress.getByName(mgmLocalAddr));
        
        this.dataDestinationAddr = InetAddress.getByName(dataDestinationAddr);
        this.dataDestinationPort = Integer.valueOf(dataDestinationPort);
        
        this.packets = Integer.valueOf(packets);
        this.timeout = Integer.valueOf(timeout);
        this.dataRate = Integer.valueOf(dataRate);
        
        setName(probeName);
        setDataRate(new EveryNSeconds(this.dataRate));
    }
    
    
    
    
    @Override
    public void beginThreadBody() {
        int timedOut = mgmReceiveAndReply();
        if (timedOut == 0) {
            LOGGER.info("Time offset was calculated with no packet loss");
        }
        else {
            LOGGER.warn("Time offset was calculated with " + timedOut + " timed out packets");
        }
    }
    

    
    @Override
    public ProbeMeasurement collect() {
        dataSend();
        //does not provide any measurements, it only sends measurement packets periodically
        return null;
    }
    
    
    
    private void dataSend() {
        DatagramPacket pingPacket;
        String pingPayload;
        int sequenceNumber = 0;
        LOGGER.info("Sending measurements packets");
        try {
            while (sequenceNumber < packets) {
                long nsSend = System.nanoTime();
                pingPayload = "PING " + sequenceNumber + " " + nsSend + " \n";
                byte[] sendBuf = pingPayload.getBytes();
                pingPacket = new DatagramPacket(sendBuf, sendBuf.length, dataDestinationAddr, dataDestinationPort);
                dataSocket.send(pingPacket);
                LOGGER.debug("Sending Packet =>" + sequenceNumber);
                sequenceNumber++;
            }
            LOGGER.info("Done");
        } catch (IOException e) {
            LOGGER.error("Error while sending messages: " + e.getMessage());
        }
    }
    
    
    private int mgmReceiveAndReply() {
        DatagramPacket pingPacket;
        DatagramPacket replyPacket;
        
        Integer sequenceNumber = 0;
        Integer timedOut = 0;
        Integer received = 0;
        
        do {
            try {
                byte[] rcvBuf = new byte[1024];
                pingPacket = new DatagramPacket(rcvBuf, rcvBuf.length);
                mgmSocket.receive(pingPacket);
                long nsReceived = System.nanoTime();
                received++;
                
                String pingPayload = new String(pingPacket.getData());
                String [] pingPayloadFields = pingPayload.split(" ");

                sequenceNumber = Integer.valueOf(pingPayloadFields[1]);
                
                String nsDestinationSent = pingPayloadFields[2];
                LOGGER.debug("sequenceNumber => " + sequenceNumber);
                                
                InetAddress destinationHost = pingPacket.getAddress();
                int destinationPort = pingPacket.getPort();
                String replyPayload = "REPLY " + sequenceNumber + " " + nsDestinationSent + " " + nsReceived + " \n";
                byte [] sendBuf = replyPayload.getBytes();
                replyPacket = new DatagramPacket(sendBuf, sendBuf.length, destinationHost, destinationPort);
                
                mgmSocket.send(replyPacket);
                
                if (mgmSocket.getSoTimeout() == 0) {
                    mgmSocket.setSoTimeout(timeout);
                }
                
            } catch (SocketTimeoutException te) {
                LOGGER.warn("Timeout for packet: " + (sequenceNumber + 1));
                timedOut++; 
                if (timedOut == packets/2)
                    return timedOut;
                
            } catch (IOException ioe) {
                LOGGER.error("Error while sending REPLY message to PING: " + sequenceNumber);
            }
        } while (received < packets);
        return timedOut;
    }
}

