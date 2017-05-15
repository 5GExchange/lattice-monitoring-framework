/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.fivegex.monitoring.appl.probes.rtt;

import eu.reservoir.monitoring.core.AbstractProbe;
import eu.reservoir.monitoring.core.DefaultProbeAttribute;
import eu.reservoir.monitoring.core.DefaultProbeValue;
import eu.reservoir.monitoring.core.Probe;
import eu.reservoir.monitoring.core.ProbeAttributeType;
import eu.reservoir.monitoring.core.ProbeMeasurement;
import eu.reservoir.monitoring.core.ProbeValue;
import eu.reservoir.monitoring.core.ProducerMeasurement;
import eu.reservoir.monitoring.core.Rational;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import org.slf4j.LoggerFactory;

/**
 *
 * @author uceeftu
 */
public class RTTProbeClient extends AbstractProbe implements Probe  {
    
    DatagramSocket socket;
    
    private static final int MAX_TIMEOUT = 1000;
    private static final int REQUESTS_NUM = 10;
    InetAddress serverHost;
    int serverPort;

    public RTTProbeClient(String name, String serverHost, String serverPort) throws UnknownHostException, SocketException {
        socket = new DatagramSocket();
        this.serverHost = InetAddress.getByName(serverHost);
        this.serverPort = Integer.valueOf(serverPort);
        
        setName(name);
        setDataRate(new Rational(360, 1));

        addProbeAttribute(new DefaultProbeAttribute(0, "mean_rtt", ProbeAttributeType.FLOAT, "msec"));
    }
    
    private float measureMeanDelay() {
        DatagramPacket ping;
	int sequence_number = 0;
        int timedOut = 0;
        
        float delaySum = 0;
        
	// Processing 10 requests loop.
	while (sequence_number < REQUESTS_NUM) {            
            long nsSend = System.nanoTime();
                        
            // Create string to send, and transfer i to a Byte Array
            String str = "PING " + sequence_number + " " + nsSend + " \n";
            byte[] buf;
            buf = str.getBytes();
            
            // Create a datagram packet to send as an UDP packet.
            ping = new DatagramPacket(buf, buf.length, serverHost, serverPort);
            
            try {
                // Send the Ping datagram to the server
                socket.send(ping);
                
		// Set up the timeout 1000 ms = 1 sec
		socket.setSoTimeout(MAX_TIMEOUT);
                
		// Set up an UDP packet for receiving
		DatagramPacket response = new DatagramPacket(new byte[1024], 1024);
                
		// Try to receive the response from the ping
		socket.receive(response);

		long nsReceived = System.nanoTime();
                
                float delay = (float)(nsReceived - nsSend)/(1000*1000);
                
		// Accumulate the delay in msec
		delaySum += delay;
                
		} catch (IOException e) {
                    // Print which packet has timed out
                    timedOut++;
                    LoggerFactory.getLogger(RTTProbeClient.class).debug("Timeout for packet " + sequence_number);
		}
            
		// next packet
		sequence_number ++;
	}
        
        return delaySum/(REQUESTS_NUM - timedOut); //filtering out the timed out pings
        
    }
    
    
    @Override
    public ProbeMeasurement collect() {
        int attrCount = 1;
        ArrayList<ProbeValue> list = new ArrayList<ProbeValue>(attrCount);
        
        try {
            list.add(new DefaultProbeValue(0, measureMeanDelay()));
        } catch (Exception e) {
            return null;
        }
        return new ProducerMeasurement(this, list, "MeanRTT");	
    }  
}
