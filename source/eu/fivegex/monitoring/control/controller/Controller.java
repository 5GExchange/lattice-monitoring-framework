/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.fivegex.monitoring.control.controller;

import cc.clayman.console.ManagementConsole;
import eu.fivegex.monitoring.control.ControlInterface;
import eu.fivegex.monitoring.control.ControlServiceException;
import eu.fivegex.monitoring.control.deployment.DataConsumerInfo;
import eu.fivegex.monitoring.control.deployment.DataSourceInfo;
import eu.fivegex.monitoring.control.deployment.DeploymentException;
import eu.fivegex.monitoring.control.probescatalogue.CatalogueException;
import eu.fivegex.monitoring.control.probescatalogue.JSONProbeCatalogue;
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
import eu.fivegex.monitoring.control.deployment.ssh.SSHServerEntityInfo;
import eu.fivegex.monitoring.control.deployment.ssh.SSHDeploymentManager;
import eu.fivegex.monitoring.control.zmq.ZMQControlPlaneXDRProducer;
import eu.fivegex.monitoring.im.delegate.InfoPlaneDelegate;
import java.net.InetAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.fivegex.monitoring.im.delegate.InfoPlaneDelegateInteracter;
import eu.fivegex.monitoring.im.zmq.ZMQControllerInfoPlane;
import eu.reservoir.monitoring.core.plane.ControlPlane;

/**
 *
 * @author uceeftu
 */
public class Controller extends AbstractPlaneInteracter implements ControlInterface<JSONObject, JSONException> {
    
    private static final Controller CONTROLLER = new Controller();
    private InfoPlaneDelegate controlInformationManager;
    
    private ManagementConsole JSONManagementConsole = null;
    private EntityDeploymentDelegate deploymentManager;
    private Boolean usingDeploymentManager;
    
    private String dsClassName;
    private String dcClassName;
    
    private JSONProbeCatalogue probeCatalogue;
    private static Logger LOGGER;

    private Controller() {}
     
    private void init(int infoPlanePort, int controlLocalPort, int managementPort, String probesPackage, String probesSuffix, Properties pr) {  
        
        this.usingDeploymentManager = Boolean.valueOf(pr.getProperty("deployment.enabled", "false"));
        
        String localJarPath = pr.getProperty("deployment.localJarPath");
        String jarFileName = pr.getProperty("deployment.jarFileName");
        String remoteJarPath = pr.getProperty("deployment.remoteJarPath");
        dsClassName = pr.getProperty("deployment.ds.className");
        dcClassName = pr.getProperty("deployment.dc.className");
        
        //Integer announceListeningPort = (Integer) pr.getOrDefault("control.announceport", 8888);
        Integer transmitterPoolSize = (Integer) pr.getOrDefault("control.poolsize", 8);
        
        // Controller is the root of the infoPlane - other nodes use it to perform bootstrap
        //InfoPlane infoPlane = new DHTInfoPlaneRoot(infoPlanePort);
        InfoPlane infoPlane = new ZMQControllerInfoPlane(infoPlanePort);
        
        // we get the ControlInformationManager from the InfoPlane
        controlInformationManager = ((InfoPlaneDelegateInteracter) infoPlane).getInfoPlaneDelegate();
        
        // setting the InfoPlane to send announce events to the ControlInformationManager
        //((DHTInfoPlaneRoot) infoPlane).addAnnounceEventListener(controlInformationManager);
        
	setInfoPlane(infoPlane);
        
        // create a control plane producer 
        // announcePort to listen for announce Messages from DSs/DCs
        // maxPoolSize to instantiate a pool of UDP Transmitters (each transmitter is not connected to any specific DS)
        // ControlPlane controlPlane = new UDPControlPlaneXDRProducer(8888, transmitterPoolSize);
        
        // create a control plane producer without announce listening capabilities 
        //ControlPlane controlPlane = new UDPControlPlaneXDRProducer(transmitterPoolSize);
        ControlPlane controlPlane = new ZMQControlPlaneXDRProducer(transmitterPoolSize, controlLocalPort); // FIXME: to be passed as property
        
        // setting the InfoPlaneDelegate to the Control Plane
        ((InfoPlaneDelegateInteracter) controlPlane).setInfoPlaneDelegate(controlInformationManager);
        //((UDPControlPlaneXDRProducer) controlPlane).addAnnounceEventListener(controlInformationManager);
        setControlPlane(controlPlane);
        
        connect();
        
        if (localJarPath != null && jarFileName != null && remoteJarPath != null) {
            if (this.usingDeploymentManager) {
                deploymentManager = new SSHDeploymentManager(localJarPath, jarFileName, remoteJarPath, controlInformationManager);
                LOGGER.info("Deployment Manager was started");
            }
            else {
                LOGGER.warn("Deployment Manager was not started");
                this.usingDeploymentManager = false;
            }
        }
        
        probeCatalogue = new JSONProbeCatalogue(probesPackage, probesSuffix);
        
        JSONManagementConsole=new JSONControllerManagementConsole(this, managementPort);
        JSONManagementConsole.start();   
    }
    
    
    public static Controller getInstance() {
        return CONTROLLER;
    }
    
    
    public InfoPlaneDelegate getInfoPlaneDelegate() {
        return controlInformationManager;
    }
    
    
    public ControllerControlPlane getControlHandle(){
            return (ControllerControlPlane)getControlPlane();
    }
    
    
    /* TODO: recheck and complete all the result.put msg in each method */
    
    @Override
    public JSONObject startDS(String endPoint, String port, String userName, String args) throws JSONException {
        JSONObject result = new JSONObject();
        
        ID startedDsID;
        
        result.put("operation", "startDS");
        result.put("endpoint",endPoint);

        if (this.usingDeploymentManager) {
            try {
                startedDsID = this.deploymentManager.startDataSource(new SSHServerEntityInfo(endPoint, Integer.valueOf(port), userName), 
                                                                       new DataSourceInfo(dsClassName, args));

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
                    result.put("msg", "DeploymentException while performing startDS operation: " + ex.getMessage());
              }
        }
        else {
            result.put("success", false);
            result.put("msg", "Deployment Manager is not running");
        }
        return result;
    }
    
    
    @Override
    //public JSONObject stopDS(String endPoint, String port, String userName) throws JSONException {
    public JSONObject stopDS(String dataSourceID) throws JSONException {
            
        JSONObject result = new JSONObject();
        
        result.put("operation", "stopDS");
        result.put("ID", dataSourceID);
        
        if (this.usingDeploymentManager) {
            try {
                this.deploymentManager.stopDataSource(ID.fromString(dataSourceID));
                result.put("success", true);
            } catch (DeploymentException ex) {
                result.put("success", false);
                result.put("msg", "DeploymentException while performing stopDS operation: " + ex.getMessage());
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
    public JSONObject loadProbe(String id, String probeClassName, String probeArgs) throws JSONException {
        JSONObject result = new JSONObject();
        
        ID createdProbeID;
        
        result.put("operation", "loadProbe");
        result.put("probeClassName",probeClassName);
        
        Object [] probeArgsAsObjects = (Object[]) null;//new Object[0];
        
        if (!probeArgs.isEmpty()) {
            probeArgsAsObjects = (Object[])probeArgs.split(" ");
        }
        
        try {
            createdProbeID = this.getControlHandle().loadProbe(ID.fromString(id), probeClassName, probeArgsAsObjects);
            result.put("createdProbeID", createdProbeID.toString());
            result.put("success", true);
        } catch (ControlServiceException ex) {
            result.put("success", false);
            result.put("msg", "ControlServiceException while performing loadProbe operation: " + ex.getMessage());
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
    public JSONObject startDC(String endPoint, String port, String userName, String args) throws JSONException {
        JSONObject result = new JSONObject();
        
        ID startedDcID;
        
        result.put("operation", "startDC");
        result.put("endpoint",endPoint);

        if (this.usingDeploymentManager) {
            try {
                //this.DCDeploymentManager.deployEntity(endPoint, userName); // possible side-effect: updated jars will not be overwritten
                startedDcID = this.deploymentManager.startDataConsumer(new SSHServerEntityInfo(endPoint, Integer.valueOf(port), userName), 
                                                                         new DataConsumerInfo(dcClassName, args));

                if (startedDcID == null) {
                    result.put("msg", "en error occured while starting the DC on the specified endpoint");
                    result.put("success", false);
                }

                else {
                    result.put("ID", startedDcID.toString());
                    result.put("success", true);
                }

            } catch (DeploymentException ex) {
                    result.put("success", false);
                    result.put("msg", "DeploymentException while performing startDC operation: " + ex.getMessage());
              }
        }
        else {
            result.put("success", false);
            result.put("msg", "Data Consumers Deployment Manager is not running");
        }
        return result;
    } 
    

    @Override
    public JSONObject stopDC(String dataConsumerID) throws JSONException {
        JSONObject result = new JSONObject();
        
        result.put("operation", "stopDC");
        result.put("ID", dataConsumerID);
        
        if (this.usingDeploymentManager) {
            try {
                this.deploymentManager.stopDataConsumer(ID.fromString(dataConsumerID));
                result.put("success", true);
            } catch (DeploymentException ex) {
                result.put("success", false);
                result.put("msg", "DeploymentException while performing stopDC operation: " + ex.getMessage());
              }
        }
        else {
            result.put("success", false);
            result.put("msg", "Data Consumer Deployment Manager is not running");
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
            result.put("msg", "ControlServiceException while performing getDataConsumerMeasurementRate operation: " + ex.getMessage());
        }
        
        return result;
    }
    
    
    @Override
    public JSONObject loadReporter(String id, String reporterClassName, String reporterArgs) throws JSONException {
        JSONObject result = new JSONObject();
        
        ID createdReporterID;
        
        result.put("operation", "loadReporter");
        result.put("reporterClassName",reporterClassName);
        
        Object [] reporterArgsAsObjects = (Object[]) null; //= new Object[0];
        
        if (!reporterArgs.isEmpty()) {
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
            JSONObject dataSources = this.controlInformationManager.getDataSources();
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
            JSONObject dataConsumers = this.controlInformationManager.getDataConsumers();
            result.put("dataconsumers", dataConsumers);
            result.put("success", true);
        } catch (JSONException ex) {
            result.put("success", false);
            result.put("msg", "JSONException while performing getDataConsumers operation: " + ex.getMessage());
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
    
    
    
    
    
    public static void main(String[] args) {
        System.setProperty(org.slf4j.impl.SimpleLogger.LOG_FILE_KEY, "System.out");
        System.setProperty(org.slf4j.impl.SimpleLogger.SHOW_SHORT_LOG_NAME_KEY, "true");
        System.setProperty(org.slf4j.impl.SimpleLogger.SHOW_THREAD_NAME_KEY, "false");
        
        LOGGER = LoggerFactory.getLogger(Controller.class);
        
        Properties prop = new Properties();
	InputStream input = null;
        String propertiesFile = null;
        
        // setting some default values
        String remoteInfoHost = InetAddress.getLoopbackAddress().getHostName();
        int infoPlanePort = 6699; // the same port is used as remote and local by DHTInfoPlaneRoot
        int restConsolePort = 6666;
        int controlLocalPort = 5555;
        String probePackage = "eu.fivegex.monitoring.appl.probes";
        String probeSuffix = "Probe";
        
        switch (args.length) {
            case 0:
                propertiesFile = System.getProperty("user.home") + "/controller.properties";
                break;
            case 1:
                propertiesFile = args[0];
                break;
            default:
                LOGGER.error("Controller main: please use: java Controller [file.properties]");
                System.exit(1);
        }
        
	try {
            //remoteInfoHost = InetAddress.getLocalHost().getHostName();
            input = new FileInputStream(propertiesFile);

            // load a properties file
            prop.load(input);

            // get the property value and print it out
            infoPlanePort = Integer.parseInt(prop.getProperty("info.localport"));
            restConsolePort = Integer.parseInt(prop.getProperty("restconsole.localport"));
            probePackage = prop.getProperty("probes.package");
            probeSuffix = prop.getProperty("probes.suffix");
            
	} catch (IOException ex) {
		LOGGER.error("Error while opening the property file: " + ex.getMessage());
                LOGGER.error("Falling back to default configuration values");
	} catch (NumberFormatException ex) {
                LOGGER.error("Error while parsing property file: " + propertiesFile + ", " + ex.getMessage());
                LOGGER.error("Falling back to default configuration values");
        } finally {        
            if (input != null) {
                try {
                    input.close();
                    } catch (IOException e) {        
                    }
            }
        }
        
        Controller myController = Controller.getInstance();
        myController.init(infoPlanePort, controlLocalPort, restConsolePort, probePackage, probeSuffix, prop);
    }
}
