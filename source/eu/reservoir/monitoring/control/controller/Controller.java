/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.reservoir.monitoring.control.controller;

import eu.fivegex.demo.CatalogueException;
import eu.fivegex.demo.JSONProbeCatalogue;
import eu.fivegex.demo.SSHDeploymentManager;
import eu.reservoir.monitoring.appl.BasicConsumer;
import eu.reservoir.monitoring.control.udp.UDPControlPlaneProducer;
import eu.reservoir.monitoring.core.ID;
import eu.reservoir.monitoring.core.PlaneInteracter;
import eu.reservoir.monitoring.core.plane.ControlPlane;
import eu.reservoir.monitoring.core.plane.InfoPlane;
import eu.reservoir.monitoring.im.dht.DHTInfoPlaneRoot;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.Properties;
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
    private Boolean usingDeploymentManager;
    
    private Controller() {}
    
    private void init(String infoPlaneAddr, int infoPlanePort, int managementPort, String probesPackage, Properties pr) {
        
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
        
        this.usingDeploymentManager = Boolean.valueOf(pr.getProperty("deployment.enabled", "false"));
        
        String localJarPath = pr.getProperty("deployment.localJarPath");
        String jarFileName = pr.getProperty("deployment.jarFileName");
        String remoteJarPath = pr.getProperty("deployment.remoteJarPath");
        String dsClassName = pr.getProperty("deployment.dsClassName");
        
        if (this.usingDeploymentManager && localJarPath != null && jarFileName != null && remoteJarPath != null && dsClassName != null) {
            deploymentManager = new SSHDeploymentManager(localJarPath, jarFileName, remoteJarPath, dsClassName);
            System.out.println("Deployment Manager was started");
        }
        else {
            System.out.println("Deployment Manager was not started");
            this.usingDeploymentManager = false;
        }
        
        
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
        
        Object [] probeArgsAsObjects = new Object[0];
        
        if (probeArgs != null) {
            probeArgsAsObjects = (Object[])probeArgs.split(" ");
        }
        
        /*
        System.out.println("Received " + probeArgsAsObjects.length + " arguments:");
        for (Object o : probeArgsAsObjects)
            System.out.println((String)o);
        */
        
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

    
    JSONObject startDS(String endPoint, String userName, String args) throws JSONException {
        JSONObject result = new JSONObject();
        
        ID startedDsID;
        
        result.put("operation", "startDS");
        result.put("endpoint",endPoint);

        if (this.usingDeploymentManager) {
            try {
                this.deploymentManager.deployDS(endPoint, userName);
                startedDsID = this.deploymentManager.startDS(endPoint, userName, args);

                if (startedDsID == null) {
                    result.put("msg", "en error occured while starting the DS on the specified endpoint");
                    result.put("success", false);
                }

                else {
                    result.put("ID", startedDsID.toString());
                    result.put("success", true);
                }

            } catch (DeploymentException ex) {
                    result.put("success", false);
                    result.put("msg", ex.getMessage());
              }
        }
        else {
            result.put("success", false);
            result.put("msg", "Deployment Manager is not running");
        }
        return result;
    }
    
    
    JSONObject stopDS(String endPoint, String userName) throws JSONException {
        JSONObject result = new JSONObject();
        
        result.put("operation", "stopDS");
        result.put("endpoint",endPoint);
        
        if (this.usingDeploymentManager) {
            try {
                Boolean returnValue = this.deploymentManager.stopDS(endPoint, userName);
                result.put("success", returnValue);
            } catch (DeploymentException ex) {
                result.put("success", false);
                result.put("msg", ex.getMessage());
              }
        }
        else {
            result.put("success", false);
            result.put("msg", "Deployment Manager is not running");
        }
        return result;
    }
    
    JSONObject getProbesCatalogue() throws JSONException {
        JSONObject result = new JSONObject();
        
        result.put("operation", "getProbesCatalogue");
        
        try {
            JSONObject catalogue = this.probeCatalogue.getProbeCatalogue();
            result.put("probesCatalogue", catalogue);
            result.put("success", true);
        } catch (CatalogueException ex) {
            result.put("success", false);
            result.put("msg", ex.getMessage());
          }
        
        return result;   
    }
    
    
    
    
    public static void main(String[] args) throws IOException {
        Properties prop = new Properties();
	InputStream input = null;
        String propertiesFile = null;
        
        // setting some default values
        int infoPlaneLocalPort = 6699;
        int restConsolePort = 6666;
        String probePackage = "eu.fivegex.demo.probes";
        
        if (args.length == 0)
            propertiesFile = System.getProperty("user.home") + "/controller.properties";
        else if (args.length == 1)
            propertiesFile = args[0];
        else {
            System.out.println("Please use: java Controller [file.properties]");
            System.exit(1);
        }
        
	try {
            input = new FileInputStream(propertiesFile);

            // load a properties file
            prop.load(input);

            // get the property value and print it out
            infoPlaneLocalPort = Integer.parseInt(prop.getProperty("info.localport"));
            restConsolePort = Integer.parseInt(prop.getProperty("restconsole.localport"));
            probePackage = prop.getProperty("probes.package");
            
	} catch (IOException ex) {
		System.out.println("Error while opening the property file: " + ex.getMessage());
                System.out.println("Falling back to default configuration values");
	} catch (NumberFormatException ex) {
                System.out.println("Error while parsing property file: " + propertiesFile + ", " + ex.getMessage());
                System.out.println("Falling back to default configuration values");
        } finally {        
            if (input != null) {
                try {
                    input.close();
                    } catch (IOException e) {        
                    }
            }
        }
        
        Controller myController = Controller.getInstance();
        String currentHost="localhost";
        
         try {
            currentHost = InetAddress.getLocalHost().getHostName();   
            System.out.println(currentHost);
        } catch (Exception e) {
        }
         
        myController.init(currentHost, infoPlaneLocalPort, restConsolePort, probePackage, prop);
        
    }
}
