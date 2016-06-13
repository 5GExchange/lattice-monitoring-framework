/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.reservoir.monitoring.control.controller;

import eu.reservoir.monitoring.appl.BasicConsumer;
import eu.reservoir.monitoring.control.udp.UDPControlPlaneProducer;
import eu.reservoir.monitoring.core.ID;
import eu.reservoir.monitoring.core.PlaneInteracter;
import eu.reservoir.monitoring.core.plane.ControlPlane;
import eu.reservoir.monitoring.core.plane.InfoPlane;
import eu.reservoir.monitoring.im.dht.DHTInfoPlaneRoot;
import java.io.IOException;
import java.util.Scanner;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;

/**
 *
 * @author uceeftu
 */
public class Controller {

    private static final Controller controller = new Controller();
    private PlaneInteracter planeInteracter;
    private InfoResolver resolver;
    
    private ControllerManagementConsole console = null;
    
    
    private Controller() {}
    
    private void init(String infoPlaneAddr, int infoPlanePort, int managementPort) {
        
        // we create an object to interact to the Lattice planes  
        planeInteracter = new BasicConsumer();
        
        // we create the root of the infoPlane (the controller needs to be started first)
        InfoPlane infoPlane = new DHTInfoPlaneRoot(infoPlaneAddr, infoPlanePort);
	planeInteracter.setInfoPlane(infoPlane);
        
        // we create the wrapper to the InfoPlane to resolve probes IDs to DSs IP:port
        resolver = new InfoResolver(infoPlane);
        
        // we create a control plane producer that is not connected to any specific Data source
        planeInteracter.setControlPlane(new UDPControlPlaneProducer(resolver));
        
        planeInteracter.connect();
        
        console=new ControllerManagementConsole(this, managementPort);
        console.start();
        
    }
    
    
    public static Controller getInstance() {
        return controller;
    }
    
    
    public ControlPlane getControlHandle(){
        return planeInteracter.getControlPlane();
    }
    
    
    public InfoResolver getResolver() {
        return resolver;
    }
    
    
    public JSONObject turnOffProbe(String id) throws JSONException {
        JSONObject result = new JSONObject();
        Boolean invocationResult;
        
        result.put("operation", "turnOffProbe");
        result.put("probeID",id);
        
        try {
            invocationResult = this.getControlHandle().turnOffProbe(ID.fromString(id));
            result.put("success", invocationResult);
        } catch (Exception ex) {
            result.put("success", false);
            result.put("msg", ex.getMessage());
        }
        
        return result;
    }
    
    
    JSONObject turnOnProbe(String id) throws JSONException {
        JSONObject result = new JSONObject();
        
        Boolean invocationResult;
        
        try {
            invocationResult = this.getControlHandle().turnOnProbe(ID.fromString(id));
            result.put("operation", "turnOnProbe");
            result.put("probeID",id);
            result.put("success", invocationResult);
        } catch (Exception ex) {
            result.put("success", false);
            result.put("msg", ex.getMessage());
        }
        
        return result;
        }
    
    
    JSONObject loadProbe(String id, String probeClassName, Object ... probeArgs) throws JSONException {
        JSONObject result = new JSONObject();
        
        ID createdProbeID;
        result.put("operation", "loadProbe");
        result.put("probeClassName",probeClassName);
        
        try {
            createdProbeID = this.getControlHandle().loadProbe(ID.fromString(id), probeClassName, probeArgs);
            result.put("createdProbeID", createdProbeID.toString());
            result.put("success", true);
        } catch (Exception ex) {
            result.put("success", false);
            result.put("msg", ex.getMessage());
        }
        
        return result;
        }
    
    
    
    public static void main(String[] args) throws IOException {
        try {
            Controller myController = Controller.getInstance();
            
            //set the control plane host and port: this will be the root of the DHT
            myController.init("localhost", 6699, 6666);
            //System.in.read();
            
            /*
            Thread thread = new Thread(){
            public void run(){
            System.out.println("Thread1 Running");
            System.out.println(myController.getControlHandle().turnOffProbe(ID.fromString("0-1-2-3-4")));
            System.out.println(System.currentTimeMillis());
            }
            };
            
            thread.start();
            
            
            
            Thread thread2 = new Thread(){
            public void run(){
            System.out.println("Thread2 Running");
            System.out.println(myController.getControlHandle().turnOffProbe(ID.fromString("0-3-5-2-4")));
            System.out.println(System.currentTimeMillis());
            }
            };
            
            thread2.start();
            */
            
            /*
            System.out.println(myController.getControlHandle().turnOffProbe(ID.fromString("0-1-2-3-4")));
            System.out.println(System.currentTimeMillis());
            
            System.out.println(myController.getControlHandle().turnOffProbe(ID.fromString("0-3-5-2-4")));
            System.out.println(System.currentTimeMillis());
            */
            
            /*
            Scanner keyboard = new Scanner(System.in);
            System.out.println("Enter the probe ID to switch off: ");
            String ID1 = keyboard.nextLine();
            
            myController.getControlHandle().turnOffProbe(ID.fromString(ID1));
            
            System.out.println("Enter the probe ID to switch off: ");
            String ID2 = keyboard.nextLine();
            
            myController.getControlHandle().turnOffProbe(ID.fromString(ID2));
            */
            
            /*
            Scanner keyboard = new Scanner(System.in);
            String dsID = keyboard.nextLine();
            
            myController.getControlHandle().loadProbe(ID.fromString(dsID), "eu.reservoir.demo.UserProcTableProbe", "ttys000");
            
            //myController.getControlHandle().loadProbe(ID.fromString(dsID), "eu.reservoir.demo.RandomProbe",  "MyelapsedTime", "elapsedTime", 15);
            
            
            String probeID = keyboard.nextLine();
            myController.getControlHandle().turnOnProbe(ID.fromString(probeID));
            */
        } catch (Exception ex) {
            System.out.println("Error while creating new probe" + ex.getMessage());
        }
        
    }
}
