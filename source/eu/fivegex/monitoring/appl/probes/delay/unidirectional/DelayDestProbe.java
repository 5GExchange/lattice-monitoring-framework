/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.fivegex.monitoring.appl.probes.delay.unidirectional;

import eu.reservoir.monitoring.appl.datarate.EveryNSeconds;
import eu.reservoir.monitoring.core.AbstractProbe;
import eu.reservoir.monitoring.core.DefaultProbeAttribute;
import eu.reservoir.monitoring.core.DefaultProbeValue;
import eu.reservoir.monitoring.core.Probe;
import eu.reservoir.monitoring.core.ProbeAttributeType;
import eu.reservoir.monitoring.core.ProbeMeasurement;
import eu.reservoir.monitoring.core.ProbeValue;
import eu.reservoir.monitoring.core.ProducerMeasurement;
import eu.reservoir.monitoring.core.TypeException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author uceeftu
 */
public class DelayDestProbe extends AbstractProbe implements Probe {
    int packets = 5;
    int timeout = 1000;
    int dataRate = 30; // seconds
    
    UDPReceiver dataReceiver;
    
    DatagramSocket mgmSocket;
    InetAddress mgmSourceAddr;
    int mgmSourcePort;
    
    Long timeOffset;
    
    LinkedBlockingQueue<Long> queue;
    
    private Logger LOGGER = LoggerFactory.getLogger(DelayDestProbe.class);
    
    
    
    public DelayDestProbe(String probeName,
                        String mgmLocalAddr,
                        String mgmLocalPort,
                        String dataLocalAddr,
                        String dataLocalPort,
                        String mgmSourceAddr, 
                        String mgmSourcePort,
                        String packets,
                        String timeout,
                        String dataRate) throws SocketException, UnknownHostException {
        
        this.mgmSourceAddr = InetAddress.getByName(mgmSourceAddr);
        this.mgmSourcePort = Integer.valueOf(mgmSourcePort);
        
        this.packets = Integer.valueOf(packets);
        this.timeout = Integer.valueOf(timeout);
        this.dataRate = Integer.valueOf(dataRate);
        
        queue = new LinkedBlockingQueue<>();
        
        mgmSocket = new DatagramSocket(Integer.valueOf(mgmLocalPort), InetAddress.getByName(mgmLocalAddr));
        dataReceiver = new UDPReceiver(Integer.valueOf(dataLocalPort), dataLocalAddr, queue, this.packets, this.timeout); 
        
        setName(probeName);
        setDataRate(new EveryNSeconds(this.dataRate));

        addProbeAttribute(new DefaultProbeAttribute(0, "link", ProbeAttributeType.STRING, "id"));
        addProbeAttribute(new DefaultProbeAttribute(1, "delay", ProbeAttributeType.LONG, "milliseconds"));
    }  
    
    
    
    
    @Override
    public void beginThreadBody() {
        timeOffset = measureTimeOffset();
        LOGGER.info("Calculated time offset: " + timeOffset);
        LOGGER.info("Starting measuring link delay");
        dataReceiver.start();
    }
    
    
    @Override
    public void endThreadBody() {
        dataReceiver.stop();
    }
    
    
    @Override
    public ProbeMeasurement collect() {
        
        try {
            LOGGER.info("queue size: " + queue.size());
            Long dataDelay = queue.take();
            LOGGER.info("Measured delay just taken off the queue: " + dataDelay);
            
            ArrayList<ProbeValue> list = new ArrayList<>(2);
            list.add(new DefaultProbeValue(0, "vnf1vnf2")); // TODO check this and see if we need to use a parameter
            list.add(new DefaultProbeValue(1, dataDelay + timeOffset));
            
            ProbeMeasurement m = new ProducerMeasurement(this, list, "Link");
            LOGGER.debug("Returning measurement: " + m.toString());
            return m;   
        } catch (InterruptedException ie) {
            LOGGER.error("Received interrupt: shutting down probe thread");
            super.threadRunning = false;
            
        } catch (TypeException te) {
            LOGGER.error("Error while adding probe attribute: " + te.getMessage());
        } catch (Exception e) {
            LOGGER.error("Error " + e.getMessage());
        }
        return null;
    }

    
    private Long measureTimeOffset() {
        
        DatagramPacket pingPacket;
        DatagramPacket replyPacket;
        
        
	int sequenceNumber = 0;
        int timedOut = 0;

        long timeOffsetSum = 0;
        
	while (sequenceNumber < packets) {            
            long nsSend = System.nanoTime();
            
            String pingPayload = "PING " + sequenceNumber + " " + nsSend + " \n";
            byte[] sendBuf = pingPayload.getBytes();
            
            pingPacket = new DatagramPacket(sendBuf, sendBuf.length, mgmSourceAddr, mgmSourcePort);
            
            try {
                mgmSocket.send(pingPacket);
                
                byte[] rcvBuf = new byte[1024];
		replyPacket = new DatagramPacket(rcvBuf, rcvBuf.length);
                mgmSocket.setSoTimeout(timeout);
		mgmSocket.receive(replyPacket);

		long nsReceived = System.nanoTime();
                
                String pongPayload = new String(replyPacket.getData());
                String [] pongPayloadFields = pongPayload.split(" ");
                
                Integer receivedSequenceNumber = Integer.valueOf(pongPayloadFields[1]);
                Long nsSent = Long.valueOf(pongPayloadFields[2]);
                Long nsSourceReceived = Long.valueOf(pongPayloadFields[3]);
                
                LOGGER.debug("receivedSequenceNumber => " + receivedSequenceNumber);
                
                // this should always be true as an exception would be raised before
                if (receivedSequenceNumber.equals(sequenceNumber) && nsSent.equals(nsSend)) {
                    // we double check we got the correct reply
                    long rtt = (nsReceived - nsSend);
                
                    long timeOffsetSample = nsSourceReceived - nsSent - (rtt/2);

                    timeOffsetSum += timeOffsetSample;
                    LOGGER.debug("Offset evaluated: " + timeOffsetSample);
                }
		} catch (SocketTimeoutException e) {
                    timedOut++;
                    LOGGER.warn("Timeout for REPLY packet " + sequenceNumber);
		} catch (IOException e) {
                    LOGGER.warn("Error while sending PING packet: " + sequenceNumber);
                }
            
		sequenceNumber++;
	}

        Long avgTimeOffsetMs = timeOffsetSum/(packets - timedOut)/(1000*1000);
        LOGGER.debug("AVG offset => " + avgTimeOffsetMs);
        return avgTimeOffsetMs;
    }
}

