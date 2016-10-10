/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.fivegex.monitoring.control.controller;

import eu.fivegex.monitoring.control.ControlServiceException;
import eu.fivegex.monitoring.control.deployment.DeploymentException;
import eu.fivegex.monitoring.control.probescatalogue.CatalogueException;
import eu.fivegex.monitoring.control.probescatalogue.JSONProbeCatalogue;
import eu.fivegex.monitoring.control.deployment.SSHDataSourcesDeploymentManager;
import eu.reservoir.monitoring.appl.datarate.EveryNSeconds;
import eu.fivegex.monitoring.control.udp.UDPControlPlaneXDRProducer;
import eu.reservoir.monitoring.core.AbstractPlaneInteracter;
import eu.reservoir.monitoring.core.ID;
import eu.reservoir.monitoring.core.plane.ControllerControlPlane;
import eu.reservoir.monitoring.core.plane.InfoPlane;
import eu.reservoir.monitoring.im.dht.DHTInfoPlaneRoot;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;
import eu.fivegex.monitoring.control.deployment.EntityDeploymentDelegate;

/**
 *
 * @author uceeftu
 */
public class Controller extends AbstractPlaneInteracter implements JSONControlInterface {
	
    private static final Controller controller = new Controller();
    private InformationManager informationManager;
    
    private ControllerManagementConsole console = null;
    private EntityDeploymentDelegate DSDeploymentManager; 
    private Boolean usingDSDeploymentManager;
    private JSONProbeCatalogue probeCatalogue;
    
    private Controller() {}
     
    private void init(String infoPlaneAddr, int infoPlanePort, int managementPort, String probesPackage, String probesSuffix, Properties pr) {        
        // Controller is the root of the infoPlane - other nodes use it to perform bootstrap
        InfoPlane infoPlane = new DHTInfoPlaneRoot(infoPlaneAddr, infoPlanePort);
	setInfoPlane(infoPlane);
        
        // we create the wrapper to the InfoPlane to resolve probes IDs to DSs IP:port, etc.
        informationManager = new InformationManager(infoPlane);
        
        // we create a control plane producer 
        // announcePort to listen for announce Messages from DSs/DCs
        // maxPoolSize to instantiate a pool of UDP Transmitters (each tranmistter is not connected to any specific DS)
        setControlPlane(new UDPControlPlaneXDRProducer(informationManager, 8888, 8)); // TODO: Use parameters to specify both port and maxPoolSize 
        
        connect();
        
        this.usingDSDeploymentManager = Boolean.valueOf(pr.getProperty("deployment.enabled", "false"));
        
        String localJarPath = pr.getProperty("deployment.localJarPath");
        String jarFileName = pr.getProperty("deployment.jarFileName");
        String remoteJarPath = pr.getProperty("deployment.remoteJarPath");
        String dsClassName = pr.getProperty("deployment.dsClassName");
        
        if (this.usingDSDeploymentManager && localJarPath != null && jarFileName != null && remoteJarPath != null && dsClassName != null) {
            DSDeploymentManager = new SSHDataSourcesDeploymentManager(localJarPath, jarFileName, remoteJarPath, dsClassName, informationManager);
            System.out.println("Controller main: Data Source Deployment Manager was started");
        }
        else {
            System.out.println("Controller main: Data Source Deployment Manager was not started");
            this.usingDSDeploymentManager = false;
        }
        
        probeCatalogue = new JSONProbeCatalogue(probesPackage, probesSuffix);
        
        console=new ControllerManagementConsole(this, managementPort);
        console.start();   
    }
    
    
    public static Controller getInstance() {
        return controller;
    }
    
    
    public ControllerControlPlane getControlHandle(){
            return (ControllerControlPlane)getControlPlane();
    }
    
    
    public InformationManager getResolver() {
        return informationManager;
    }
    
    
    @Override
    public JSONObject turnOffProbe(String id) throws JSONException {
        JSONObject result = new JSONObject();
        Boolean invocationResult;
        
        result.put("operation", "turnOffProbe");
        result.put("probeID",id);
        
        try {
            invocationResult = this.getControlHandle().turnOffProbe(ID.fromString(id));
            result.put("success", invocationResult);
        } catch (ControlServiceException ex) {
            result.put("success", false);
            result.put("msg", ex.getMessage());
        }
        
        return result;
    }
    
    
    @Override
    public JSONObject turnOnProbe(String id) throws JSONException {
        JSONObject result = new JSONObject();
        
        Boolean invocationResult;
        
        result.put("operation", "turnOnProbe");
        result.put("probeID",id);
        
        try {
            invocationResult = this.getControlHandle().turnOnProbe(ID.fromString(id));
            result.put("success", invocationResult);
        } catch (ControlServiceException ex) {
            result.put("success", false);
            result.put("msg", ex.getMessage());
        }
        
        return result;
        }
    
    
    @Override
    public JSONObject loadProbe(String id, String probeClassName, String probeArgs) throws JSONException {
        JSONObject result = new JSONObject();
        
        ID createdProbeID;
        
        result.put("operation", "loadProbe");
        result.put("probeClassName",probeClassName);
        
        Object [] probeArgsAsObjects = new Object[0];
        
        if (probeArgs != null) {
            probeArgsAsObjects = (Object[])probeArgs.split(" ");
        }
        
        try {
            createdProbeID = this.getControlHandle().loadProbe(ID.fromString(id), probeClassName, probeArgsAsObjects);
            result.put("createdProbeID", createdProbeID.toString());
            result.put("success", true);
        } catch (ControlServiceException ex) {
            result.put("success", false);
            result.put("msg", ex.getMessage());
        }
        return result;
        }
    
    @Override
    public JSONObject unloadProbe(String id) throws JSONException {
        JSONObject result = new JSONObject();
        
        Boolean invocationResult;
        
        result.put("operation", "unlodProbe");
        result.put("probeID",id);
        
        try {
            invocationResult = this.getControlHandle().unloadProbe(ID.fromString(id));
            result.put("success", invocationResult);
        } catch (ControlServiceException ex) {
            result.put("success", false);
            result.put("msg", ex.getMessage());
        }
        
        return result;
        }
     
    @Override
    public JSONObject setProbeServiceID(String probeID, String serviceID) throws JSONException {
        JSONObject result = new JSONObject();
        
        Boolean invocationResult;
        
        result.put("operation", "setProbeServiceID");
        result.put("probeID",probeID);
        result.put("serviceID",serviceID);
        
        try {
            invocationResult = this.getControlHandle().setProbeServiceID(ID.fromString(probeID), ID.fromString(serviceID));
            result.put("success", invocationResult);
        } catch (ControlServiceException ex) {
            result.put("success", false);
            result.put("msg", ex.getMessage());
        }
        
        return result;
    }
    
    
    @Override
    public JSONObject setProbeGroupID(String probeID, String groupID) throws JSONException {
        JSONObject result = new JSONObject();
        
        Boolean invocationResult;
        
        result.put("operation", "setProbeGroupID");
        result.put("probeID",probeID);
        result.put("sliceID",groupID);
        
        try {
            invocationResult = this.getControlHandle().setProbeGroupID(ID.fromString(probeID), ID.fromString(groupID));
            result.put("success", invocationResult);
        } catch (ControlServiceException ex) {
            result.put("success", false);
            result.put("msg", ex.getMessage());
        }
        
        return result;
    }

    
    @Override
    public JSONObject startDS(String endPoint, String userName, String args) throws JSONException {
        JSONObject result = new JSONObject();
        
        ID startedDsID;
        
        result.put("operation", "startDS");
        result.put("endpoint",endPoint);

        if (this.usingDSDeploymentManager) {
            try {
                this.DSDeploymentManager.deployEntity(endPoint, userName);
                startedDsID = this.DSDeploymentManager.startEntity(endPoint, userName, args);

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
    
    
    @Override
    public JSONObject stopDS(String endPoint, String userName) throws JSONException {
        JSONObject result = new JSONObject();
        
        result.put("operation", "stopDS");
        result.put("endpoint",endPoint);
        
        if (this.usingDSDeploymentManager) {
            try {
                Boolean returnValue = this.DSDeploymentManager.stopEntity(endPoint, userName);
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
    
    @Override
    public JSONObject getDataSourceInfo(String dsID) throws JSONException {
        JSONObject result = new JSONObject();
         
        String dsName;
        
        result.put("operation", "getDataSourceInfo");
        result.put("ID", dsID);
        try {
            dsName = (String) this.getControlHandle().getDataSourceInfo(ID.fromString(dsID));
            result.put("name", dsName);
            result.put("success", true);
        } catch (ControlServiceException ex) {
            result.put("success", false);
            result.put("msg", ex.getMessage());
        } 
        return result;
     }
    
    
    @Override
    public JSONObject getProbesCatalogue() throws JSONException {
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
    
    
    @Override
    public JSONObject setProbeDataRate(String probeID, String dataRate) throws JSONException {
        JSONObject result = new JSONObject();
        Boolean invocationResult;
        
        result.put("operation", "setProbeDataRate");
        
        result.put("probeID",probeID);
        result.put("rate",dataRate);
        
        try {
            invocationResult = this.getControlHandle().setProbeDataRate(ID.fromString(probeID), new EveryNSeconds(Integer.valueOf(dataRate)));
            result.put("success", invocationResult);
        } catch (ControlServiceException ex) {
            result.put("success", false);
            result.put("msg", ex.getMessage());
        }
        
        return result;
    }
    
    
    @Override
    public JSONObject getDataConsumerMeasurementRate(String dcID) throws JSONException {
        JSONObject result = new JSONObject();
        Float rate;
        
        result.put("operation", "getDataConsumerMeasurementRate");
        
        result.put("ID",dcID);
        
        try {
            System.out.println("Invoking getDataConsumerMeasurementRate: " + dcID);
            rate = this.getControlHandle().getDCMeasurementsRate(ID.fromString(dcID));
            result.put("rate", rate.toString());
            result.put("success", true);
        } catch (ControlServiceException ex) {
            result.put("success", false);
            result.put("msg", "ControlServiceException while performing getDataConsumerMeasurementRate opertaion: " + ex.getMessage());
        }
        
        return result;
    }
    
    
    @Override
    public JSONObject loadReporter(String id, String reporterClassName, String reporterArgs) throws JSONException {
        JSONObject result = new JSONObject();
        
        ID createdReporterID;
        
        result.put("operation", "loadReporter");
        result.put("reporterClassName",reporterClassName);
        
        Object [] reporterArgsAsObjects = new Object[0];
        
        if (reporterArgs != null) {
            reporterArgsAsObjects = (Object[])reporterArgs.split(" ");
        }
        
        try {
            createdReporterID = this.getControlHandle().loadReporter(ID.fromString(id), reporterClassName, reporterArgsAsObjects);
            result.put("createdReporterID", createdReporterID.toString());
            result.put("success", true);
        } catch (ControlServiceException ex) {
            result.put("success", false);
            result.put("msg", "ControlServiceException while performing loadReporter operation: " + ex.getMessage());
        }
        return result;
        }
    
    
    @Override
    public JSONObject unloadReporter(String id) throws JSONException {
        JSONObject result = new JSONObject();
        
        Boolean invocationResult;
        
        result.put("operation", "unlodReporter");
        result.put("reporterID",id);
        
        try {
            invocationResult = this.getControlHandle().unloadReporter(ID.fromString(id));
            result.put("success", invocationResult);
        } catch (ControlServiceException ex) {
            result.put("success", false);
            result.put("msg", "ControlServiceException while performing unloadReporter operation: " + ex.getMessage());
        }
        return result;
        }
    
    @Override
    public JSONObject getDataSources() throws JSONException {
        JSONObject result = new JSONObject();
        
        result.put("operation", "getDataSources");
        
        try {
            JSONObject dataSources = this.informationManager.getDataSourcesAsJSON();
            result.put("datasources", dataSources);
            result.put("success", true);
        } catch (JSONException ex) {
            result.put("success", false);
            result.put("msg", "JSONException while performing getDataSources operation: " + ex.getMessage());
          }
        return result;  
    }
    
    
    @Override
    public JSONObject getDataConsumers() throws JSONException {
        JSONObject result = new JSONObject();
        
        result.put("operation", "getDataConsumers");
        
        try {
            JSONObject dataConsumers = this.informationManager.getDataConsumersAsJSON();
            result.put("dataconsumers", dataConsumers);
            result.put("success", true);
        } catch (JSONException ex) {
            result.put("success", false);
            result.put("msg", "JSONException while performing getDataConsumers operation: " + ex.getMessage());
          }
        return result;  
    }
    
    
    
    
    public static void main(String[] args) {
        Properties prop = new Properties();
	InputStream input = null;
        String propertiesFile = null;
        
        // setting some default values
        String remoteInfoHost="localhost"; // controller is the DHT root: connecting locally
        int infoPlanePort = 6699; // the same port is used as remote and local by DHTInfoPlaneRoot
        int restConsolePort = 6666;
        String probePackage = "eu.fivegex.monitoring.appl.probes";
        String probeSuffix = "Probe";
        
        if (args.length == 0)
            propertiesFile = System.getProperty("user.home") + "/controller.properties";
        else if (args.length == 1)
            propertiesFile = args[0];
        else {
            System.err.println("Controller main: please use: java Controller [file.properties]");
            System.exit(1);
        }
        
	try {
            input = new FileInputStream(propertiesFile);

            // load a properties file
            prop.load(input);

            // get the property value and print it out
            infoPlanePort = Integer.parseInt(prop.getProperty("info.localport"));
            restConsolePort = Integer.parseInt(prop.getProperty("restconsole.localport"));
            probePackage = prop.getProperty("probes.package");
            probeSuffix = prop.getProperty("probes.suffix");
            
	} catch (IOException ex) {
		System.err.println("Controller main: error while opening the property file: " + ex.getMessage());
                System.err.println("Controller main: falling back to default configuration values");
	} catch (NumberFormatException ex) {
                System.err.println("Controller main: error while parsing property file: " + propertiesFile + ", " + ex.getMessage());
                System.err.println("Controller main: falling back to default configuration values");
        } finally {        
            if (input != null) {
                try {
                    input.close();
                    } catch (IOException e) {        
                    }
            }
        }
        
        Controller myController = Controller.getInstance();
        myController.init(remoteInfoHost, infoPlanePort, restConsolePort, probePackage, probeSuffix, prop);
    }
}
