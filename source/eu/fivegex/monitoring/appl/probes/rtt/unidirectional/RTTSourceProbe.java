/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.fivegex.monitoring.appl.probes.rtt.unidirectional;

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
public class RTTSourceProbe extends AbstractProbe implements Probe {
    private static final int MAX_TIMEOUT = 5000;
    private static final int REQUESTS_NUM = 20;
    
    DatagramSocket dataSocket;
    DatagramSocket mgmSocket;
    
    InetAddress dataDestinationAddr;
    int dataDestinationPort;
    
    private Logger LOGGER = LoggerFactory.getLogger(RTTSourceProbe.class);
    
    
    public RTTSourceProbe(String probeName, 
                          String mgmLocalAddr,
                          String mgmLocalPort,
                          String dataLocalAddr,
                          String dataLocalPort,
                          String dataDestinationAddr, 
                          String dataDestinationPort) throws SocketException, UnknownHostException {
        
        
        
        dataSocket = new DatagramSocket(Integer.valueOf(dataLocalPort), InetAddress.getByName(dataLocalAddr));
        mgmSocket = new DatagramSocket(Integer.valueOf(mgmLocalPort), InetAddress.getByName(mgmLocalAddr));
        
        this.dataDestinationAddr = InetAddress.getByName(dataDestinationAddr);
        this.dataDestinationPort = Integer.valueOf(dataDestinationPort);
        
        setName(probeName);
        setDataRate(new EveryNSeconds(60));
    }
    
    
    @Override
    public void beginThreadBody() {
        LOGGER.info("Calculating time offset: " + mgmReceiveAndReply());
    }
    

    
    @Override
    public ProbeMeasurement collect() {
        dataSend();
        //does not provide any measurements, it only sents measurement packets periodically
        return null;
    }
    
    
    
    private void dataSend() {
        DatagramPacket pingPacket;
        String pingPayload;
        int sequenceNumber = 0;
        
        try {
            while (sequenceNumber < REQUESTS_NUM) {
                long nsSend = System.nanoTime();
                pingPayload = "PING " + sequenceNumber + " " + nsSend + " \n";
                byte[] sendBuf = pingPayload.getBytes();
                pingPacket = new DatagramPacket(sendBuf, sendBuf.length, dataDestinationAddr, dataDestinationPort);
                dataSocket.send(pingPacket);
                LOGGER.debug("Sending Packet =>" + sequenceNumber);
                sequenceNumber++;
            }
        } catch (IOException e) {
            LOGGER.error("Error while sending messages: " + e.getMessage());
        }
    }
    
    
    private boolean mgmReceiveAndReply() {
        DatagramPacket pingPacket;
        DatagramPacket pongPacket;
        Integer sequenceNumberValue=0;
        Integer lastTimedOut;
        Integer timedOut=0;
        
        while (true) {
            try {
                byte[] rcvBuf = new byte[1024];
                pingPacket = new DatagramPacket(rcvBuf, rcvBuf.length);
                mgmSocket.receive(pingPacket);
                long nsReceived = System.nanoTime();
                
                String pingPayload = new String(pingPacket.getData());
                String [] pingPayloadFields = pingPayload.split(" ");
                
                String sequenceNumber = pingPayloadFields[1];
                sequenceNumberValue = Integer.valueOf(sequenceNumber);
                
                if (sequenceNumberValue < 0) {
                    break;
                }
                
                String nsDestinationSent = pingPayloadFields[2];
                LOGGER.debug("sequenceNumber => " + sequenceNumber);
                                
                InetAddress destinationHost = pingPacket.getAddress();
                int destinationPort = pingPacket.getPort();
                String pongPayload = "REPLY " + sequenceNumber + " " + nsDestinationSent + " " + nsReceived + " \n";
                byte [] sendBuf = pongPayload.getBytes();
                pongPacket = new DatagramPacket(sendBuf, sendBuf.length, destinationHost, destinationPort);
                
                mgmSocket.send(pongPacket);
                
                if (mgmSocket.getSoTimeout() == 0) {
                    mgmSocket.setSoTimeout(MAX_TIMEOUT);
                }
                
            } catch (SocketTimeoutException te) {
                LOGGER.warn("Timeout for packet " + sequenceNumberValue);
                lastTimedOut = sequenceNumberValue;
                if (sequenceNumberValue.equals(lastTimedOut)) 
                   timedOut++; 
                if (timedOut >= 3)
                    return false;
                
            } catch (IOException ioe) {
                LOGGER.error("Error while sending reply PONG message: " + ioe.getMessage());
            }
        }
        return true;
    }

}

