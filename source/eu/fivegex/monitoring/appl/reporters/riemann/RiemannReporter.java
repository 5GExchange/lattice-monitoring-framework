///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package eu.fivegex.monitoring.appl.reporters.riemann;
//
//import eu.fivegex.monitoring.appl.reporters.riemann.Proto.Attribute;
//import eu.fivegex.monitoring.appl.reporters.riemann.Proto.Event;
//import eu.fivegex.monitoring.appl.reporters.riemann.Proto.Msg;
//import eu.reservoir.monitoring.core.AbstractReporter;
//import eu.reservoir.monitoring.core.Measurement;
//import eu.reservoir.monitoring.core.ProbeValue;
//import eu.reservoir.monitoring.core.ProbeValueWithName;
//import eu.reservoir.monitoring.core.Timestamp;
//import java.io.IOException;
//import java.net.DatagramPacket;
//import java.net.DatagramSocket;
//import java.net.InetAddress;
//import java.util.ArrayList;
//import java.util.Iterator;
//import java.util.List;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
///**
// *
// * @author uceeftu
// */
//public class RiemannReporter extends AbstractReporter {
//    InetAddress address;
//    int port;
//    
//    Event event;
//    List<Event> events;
//    
//    Msg message;
//    Attribute attribute;
//    
//    DatagramSocket sender;
//    DatagramPacket packet;
//    
//    private Logger LOGGER = LoggerFactory.getLogger(RiemannReporter.class);
//    
//    
//    public RiemannReporter(String address, String port) throws IOException {
//        super("riemann-reporter");
//        this.address = InetAddress.getByName(address);
//        this.port = Integer.valueOf(port);      
//        
//        sender = new DatagramSocket();
//    }
//    
//    
//    
//    @Override
//    public void report(Measurement m) {
//        events = new ArrayList<>();
//        
//        LOGGER.debug("Received measurement: " + m.toString());
//        
//        Iterator<ProbeValue> values = m.getValues().iterator();
//        
//        String resourceId = (String)values.next().getValue();
//        Timestamp timestamp = m.getTimestamp();
//        
//        while (values.hasNext()) {
//            ProbeValue probeAttribute = values.next();
//            event = Proto.Event.newBuilder()
//                              .setHost(resourceId)
//                              .setService(((ProbeValueWithName)probeAttribute).getName())
//                              .setMetricF(((float)probeAttribute.getValue()))
//                              //.setState("critical")
//                              .setDescription(m.getServiceID().toString()) 
//                              // using description to store the serviceID can be changed to use a custom attribute:
//                              // attribute = Proto.Attribute.newBuilder().setKey("custom").setValue("xxxyyyzzz").build();
//                              .setTimeMicros(timestamp.value())
//                              //.addTags("http")
//                              //.addAttributes(attribute)
//                              .build();
//            
//            /*formattedMeasurement.append(((ProbeValueWithName)probeAttribute).getName()) e.g., cpu_percentage
//                                .append("," + "serviceid=") 
//                                .append(m.getServiceID()) i.e., the service UUID
//                                .append("," + "resourceid=")
//                                .append(resourceId) i.e., the NF id: vCDN1_UUID
//                                .append(" " + "value=")
//                                .append(probeAttribute.getValue()) i.e., the metric value (usually a float value)
//                                .append(" ")
//                                .append(timestamp)
//                                .append("\n");
//            }*/
//            
//            events.add(event); 
//        }
//        
//        message= Msg.newBuilder().addAllEvents(events).build();
//        LOGGER.debug(message.toString());
//        
//        try {
//            packet = new DatagramPacket(message.toByteArray(), message.toByteArray().length, address, port);
//            sender.send(packet);
//        } catch (IOException e) {
//            LOGGER.error("Error while sending measurement message to Riemann: " + e.getMessage());
//        }
//    }
//    
//}
