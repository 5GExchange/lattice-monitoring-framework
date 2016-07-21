/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.reservoir.monitoring.control.controller;

import eu.fivegex.demo.JSONProbeCatalogue;
import eu.fivegex.demo.SSHDeploymentManager;
import eu.reservoir.monitoring.appl.BasicConsumer;
import eu.reservoir.monitoring.control.udp.UDPControlPlaneProducer;
import eu.reservoir.monitoring.core.ID;
import eu.reservoir.monitoring.core.PlaneInteracter;
import eu.reservoir.monitoring.core.plane.ControlPlane;
import eu.reservoir.monitoring.core.plane.InfoPlane;
import eu.reservoir.monitoring.im.dht.DHTInfoPlaneRoot;
import java.io.IOException;
import java.net.InetAddress;
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
    private DeploymentDelegate deploymentManager; 
    private JSONProbeCatalogue probeCatalogue;
    
    private Controller() {}
    
    private void init(String infoPlaneAddr, int infoPlanePort, int managementPort, String probesPackage) {
        
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
        
        // paramaters should be read from a conf file
        deploymentManager = new SSHDeploymentManager("/Users/uceeftu/Work/lattice-monitoring-framework/5Gex-Lattice/dist",
                                                     "5GEx-Lattice.jar",
                                                     "/tmp",
                                                     "eu.fivegex.demo.SimpleDataSourceDaemon");
        
        probeCatalogue = new JSONProbeCatalogue(probesPackage);
        
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
    
    
    JSONObject turnOffProbe(String id) throws JSONException {
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
        
        result.put("operation", "turnOnProbe");
        result.put("probeID",id);
        
        try {
            invocationResult = this.getControlHandle().turnOnProbe(ID.fromString(id));
            result.put("success", invocationResult);
        } catch (Exception ex) {
            result.put("success", false);
            result.put("msg", ex.getMessage());
        }
        
        return result;
        }
    
    
    JSONObject loadProbe(String id, String probeClassName, String probeArgs) throws JSONException {
        JSONObject result = new JSONObject();
        
        ID createdProbeID;
        
        result.put("operation", "loadProbe");
        result.put("probeClassName",probeClassName);
        
        Object [] probeArgsAsObjects = (Object[])probeArgs.split(" ");
        
        System.out.println("Received arguments:");
        for (Object o : probeArgsAsObjects)
            System.out.println((String)o);
        
        try {
            createdProbeID = this.getControlHandle().loadProbe(ID.fromString(id), probeClassName, probeArgsAsObjects);
            result.put("createdProbeID", createdProbeID.toString());
            result.put("success", true);
        } catch (Exception ex) {
            result.put("success", false);
            result.put("msg", ex.getMessage());
        }
        return result;
        }
    
    JSONObject unloadProbe(String id) throws JSONException {
        JSONObject result = new JSONObject();
        
        Boolean invocationResult;
        
        result.put("operation", "unlodProbe");
        result.put("probeID",id);
        
        try {
            invocationResult = this.getControlHandle().unloadProbe(ID.fromString(id));
            result.put("success", invocationResult);
        } catch (Exception ex) {
            result.put("success", false);
            result.put("msg", ex.getMessage());
        }
        
        return result;
        }
    
    JSONObject setProbeServiceID(String probeID, String serviceID) throws JSONException {
        JSONObject result = new JSONObject();
        
        Boolean invocationResult;
        
        result.put("operation", "setProbeServiceID");
        result.put("probeID",probeID);
        result.put("serviceID",serviceID);
        
        try {
            invocationResult = this.getControlHandle().setProbeServiceID(ID.fromString(probeID), ID.fromString(serviceID));
            result.put("success", invocationResult);
        } catch (Exception ex) {
            result.put("success", false);
            result.put("msg", ex.getMessage());
        }
        
        return result;
    }

    
    JSONObject startDS(String endPoint, String userName) throws JSONException {
        JSONObject result = new JSONObject();
        
        ID createdDsID;
        
        result.put("operation", "startDS");
        result.put("endpoint",endPoint);
        
        try {
            // we should check here if a DS is already deployed/running on that endpoint
            //if (
                this.deploymentManager.deployDS(endPoint, userName); //{
                createdDsID = this.deploymentManager.startDS(endPoint, userName, "");
                result.put("createdDsID", createdDsID.toString());
                result.put("success", true);
            //}
        } catch (DeploymentException ex) {
            result.put("success", false);
            result.put("msg", ex.getMessage());
          }
        
        return result;
    }
    
    
    JSONObject getProbesCatalogue() throws JSONException {
        JSONObject result = new JSONObject();
        
        result.put("operation", "getProbesCatalogue");
        
        try {
            this.probeCatalogue.searchForProbes();
            this.probeCatalogue.generateProbesCatalogue();
            JSONObject catalogue = this.probeCatalogue.getProbeCatalogue();
            result.put("probesCatalogue", catalogue);
            result.put("success", true);
        } catch (JSONException | ClassNotFoundException | IOException ex) {
            result.put("success", false);
            result.put("msg", ex.getMessage());
          }
        
        return result;   
    }
    
    
    
    
    public static void main(String[] args) throws IOException {
        Controller myController = Controller.getInstance();

        //set the control plane host and port: this will be the root of the DHT

        String currentHost="localhost";
        int infoPlaneLocalPort = 6699;
        int restConsolePort = 6666;
        String probePackage = "eu.fivegex.demo.probes";

         try {
            currentHost = InetAddress.getLocalHost().getHostName();   
            System.out.println(currentHost);
        } catch (Exception e) {
        }        


        myController.init(currentHost, infoPlaneLocalPort, restConsolePort, probePackage);
        
    }
}
