package eu.fivegex.monitoring.test;

import eu.reservoir.monitoring.core.ID;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import us.monoid.json.JSONArray;

import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;
import us.monoid.web.Resty;

import static us.monoid.web.Resty.delete;
import static us.monoid.web.Resty.put;
import static us.monoid.web.Resty.content;
import static us.monoid.web.Resty.form;

/**
 * Makes REST calls to VIM / GlobalController using Resty
 */
public class LatticeTest {
    // A URI for a Lattice Controller to interact with
    String vimURI;
    Resty rest;
    int port;
    
    //general attributes
    String endPointAddress;
    String endPointName;
    String endPointUserName;

    String dataConsumerAddress;
    String dataConsumerPort;
    
    String controllerAddress;
    String remoteInfoPort;
    String localInfoPort;
    String localControlPort;
    
    //docker related attributes
    String dockerHost;
    String dockerPort;
    String dockerContainerID;
    String dockerContainerName;
    
    
    
    
    /**
     * Construct a LatticeTest
     * using defaults of localhost and port 6666
     */
    public LatticeTest(Map configuration) throws UnknownHostException, IOException {
        this((String)configuration.get("RESTAddress"), Integer.valueOf((String)configuration.get("RESTPort")));
        
        endPointAddress = (String)configuration.get("endPointAddress");
        endPointName = (String)configuration.get("endPointName");
        endPointUserName = (String)configuration.get("endPointUserName");

        dataConsumerAddress = (String)configuration.get("dataConsumerAddress");
        dataConsumerPort = (String)configuration.get("dataConsumerPort");
        
        controllerAddress = (String)configuration.get("controllerAddress");
        dataConsumerPort = (String)configuration.get("dataConsumerPort");
        
        controllerAddress = (String)configuration.get("controllerAddress");
        remoteInfoPort = (String)configuration.get("remoteInfoPort");
        localInfoPort = (String)configuration.get("localInfoPort");
        localControlPort = (String)configuration.get("localControlPort");
        
        dockerHost = (String)configuration.get("dockerHost");
        dockerPort = (String)configuration.get("dockerPort");
        dockerContainerID = (String)configuration.get("dockerContainerID");
        dockerContainerName = (String)configuration.get("dockerContainerName");        
    }

    /**
     * Constructor for a VimClient
     * to the ManagementConsole of a Lattice Controller.
     * @param addr the name of the host
     * @param port the port the server is listening on
     */
    public LatticeTest(String addr, int port) throws UnknownHostException, IOException  {
        initialize(InetAddress.getByName(addr), port);
    }

    /**
     * Constructor for a VimClient
     * to the ManagementConsole of a Lattice Controller.
     * @param addr the InetAddress of the host
     * @param port the port the server is listening on
     */
    public LatticeTest(InetAddress addr, int port) throws UnknownHostException, IOException  {
        initialize(addr, port);
    }

    /**
     * Initialize
     */
    private synchronized void initialize(InetAddress addr, int port) {
        this.port = port;
        vimURI = "http://" + addr.getHostName() + ":" + Integer.toString(port);
        rest = new Resty();
    }

    /**
     * Get the port this VimClient is connecting to
     */
    public int getPort() {
        return port;
    }

    
    //curl -X POST http://localhost:6666/datasource/<dsUUID>/probe/?className=<probeClassName>\&args=<arg1>+<arg2>+<argN>
    public JSONObject loadProbeOnDsByID(String ID, String name, String args) throws JSONException {
        try {
            String uri = vimURI + "/datasource/" + ID + "/probe/?className=" + name + "&args=" + java.net.URLEncoder.encode(args, "UTF-8");
            //System.out.println(uri);
            JSONObject jsobj = rest.json(uri, form("")).toObject();

            return jsobj;

        } catch (IOException ioe) {
            throw new JSONException("loadProbe FAILED" + " IOException: " + ioe.getMessage());
        }
    }
    
    
    //curl -X POST http://localhost:6666/datasource/<dsName>/probe/?className=<probeClassName>\&args=<arg1>+<arg2>+<argN>
    public JSONObject loadProbeOnDsByName(String dsName, String name, String args) throws JSONException {
        try {
            String uri = vimURI + "/datasource/" + dsName + "/probe/?className=" + name + "&args=" + java.net.URLEncoder.encode(args, "UTF-8");
            
            // adding form data causes a POST
            JSONObject jsobj = rest.json(uri, form("")).toObject();

            return jsobj;

        } catch (IOException ioe) {
            throw new JSONException("loadProbe FAILED" + " IOException: " + ioe.getMessage());
        }
    }
    
    // curl -X PUT http://localhost:6666/probe/<probeUUID>/?status=on
    public JSONObject turnProbeOn(String probeID) throws JSONException {
        try {
            String uri = vimURI + "/probe/" + probeID + "/?status=on";
            //System.out.println(uri);
            JSONObject jsobj = rest.json(uri, put(content(""))).toObject();

            return jsobj;
        } catch (IOException ioe) {
            throw new JSONException("turnProbeOn FAILED" + " IOException: " + ioe.getMessage());
        }
    }
    
    
    // curl -X PUT http://localhost:6666/probe/<probeUUID>/?status=off
    public JSONObject turnProbeOff(String probeID) throws JSONException {
        try {
            String uri = vimURI + "/probe/" + probeID + "/?status=off";
            //System.out.println(uri);
            JSONObject jsobj = rest.json(uri, put(content(""))).toObject();

            return jsobj;
        } catch (IOException ioe) {
            throw new JSONException("turnProbeOff FAILED" + " IOException: " + ioe.getMessage());
        }
    }
    
    
    // curl -X PUT http://localhost:6666/probe/<probeUUID>/?serviceid=<serviceUUID>
    public JSONObject setProbeServiceID(String probeID, String serviceID) throws JSONException {
        try {
            String uri = vimURI + "/probe/" + probeID + "/?serviceid=" + serviceID;
            
            JSONObject jsobj = rest.json(uri, put(content(""))).toObject();

            return jsobj;
        } catch (IOException ioe) {
            throw new JSONException("setProbeServiceID FAILED" + " IOException: " + ioe.getMessage());
        }
    }
    
    
    // curl -X PUT http://localhost:6666/probe/<probeUUID>/?sliceid=<sliceUUID>
    public JSONObject setProbeSliceID(String probeID, String sliceID) throws JSONException {
        try {
            String uri = vimURI + "/probe/" + probeID + "/?sliceid=" + sliceID;
            
            JSONObject jsobj = rest.json(uri, put(content(""))).toObject();

            return jsobj;
        } catch (IOException ioe) {
            throw new JSONException("setProbeSliceID FAILED" + " IOException: " + ioe.getMessage());
        }
    }
    
    
    // curl -X DELETE http://localhost:6666/probe/<probeUUID>
    public JSONObject unloadProbe(String probeID) throws JSONException {
        try {
            String uri = vimURI + "/probe/" + probeID;
            
            JSONObject jsobj = rest.json(uri, delete()).toObject();

            return jsobj;
        } catch (IOException ioe) {
            throw new JSONException("unloadProbe FAILED" + " IOException: " + ioe.getMessage());
        }
    }
    
   
    // curl -X GET http://localhost:6666/probe/catalogue/
    public JSONObject getProbesCatalogue() throws JSONException {
        try {
            String uri = vimURI + "/probe/catalogue/";
            
            JSONObject jsobj = rest.json(uri).toObject();

            return jsobj;
        } catch (IOException ioe) {
            throw new JSONException("getProbesCatalogue FAILED" + " IOException: " + ioe.getMessage());
        }
    }
    
    
    //curl -X POST http://localhost:6666/datasource/?endpoint=<endpoint>\&username=<username>\&args=arg1+arg2+argN
    public JSONObject deployDS(String endPoint, String userName, String args) throws JSONException {
        try {
            String uri = vimURI + "/datasource/?endpoint=" + endPoint + "&username=" + userName + "&args=" + args;
            //System.out.println(uri);
            JSONObject jsobj = rest.json(uri, form("")).toObject();

            return jsobj;
        } catch (IOException ioe) {
            throw new JSONException("deployDS FAILED" + " IOException: " + ioe.getMessage());
        }
    }
    
    //curl -X DELETE http://localhost:6666/datasource/?endpoint=<endpoint>\&username=<username>
    public JSONObject stopDS(String endPoint, String userName) throws JSONException {
        try {
            String uri = vimURI + "/datasource/?endpoint=" + endPoint + "&username=" + userName;
            
            JSONObject jsobj = rest.json(uri, delete()).toObject();

            return jsobj;
        } catch (IOException ioe) {
            throw new JSONException("stopDS FAILED" + " IOException: " + ioe.getMessage());
        }
    }
    
    
    private String instantiateDS() throws Exception {
        JSONObject out;
        
        System.out.println("Deploying DS on endpoint: " + endPointName);
        
        try {
            out = deployDS(endPointName, endPointUserName, dataConsumerAddress + "+" + 
                          dataConsumerPort + "+" +
                          controllerAddress + "+" +
                          remoteInfoPort + "+" +
                          localInfoPort + "+" +
                          localControlPort
                          );
            
            return out.getString("ID");
        }
        catch (JSONException e) {
            throw new Exception("Error while instantiating DS: " + e.getMessage());
        }
    }
    
    
    private void unloadDS(String dsID) throws Exception {
        JSONObject out;
        System.out.println("Stopping DS on endpoint: "  + endPointAddress + " - DS id: " + dsID);
        try {
            out = stopDS(endPointAddress, endPointUserName);  

            if (!out.getString("success").equals("true"))
                throw new Exception("Error while stopping DS: " + dsID); 
        }
        catch (JSONException e) {
            throw new Exception("Error while unloading DS: " + e.getMessage());
        }
    }
    
    private void testMemoryInfoProbe(String probeName, String dsID, String serviceID, String sliceID) throws Exception {
        String probeClassName = "eu.fivegex.demo.probes.MemoryInfoProbe";
        JSONObject out;
        
        try {
            System.out.println("Creating probe on endpoint: " + endPointName + " - DS id: " + dsID);
            System.out.println("Dinamically loading probe class: " + probeClassName);
            
            out = loadProbeOnDsByID(dsID, probeClassName, probeName);
            
            String probeID = out.getString("createdProbeID");

            System.out.println("Setting serviceID " + serviceID + " on probe " + probeID + " on endpoint " + endPointName  + " - DS id: " + dsID);
            setProbeServiceID(probeID, serviceID);

            System.out.println("Setting sliceID " + sliceID + " on probe " + probeID + " on endpoint " + endPointName  + " - DS id: " + dsID);
            setProbeSliceID(probeID, sliceID);
            
            System.out.println("Turning on probe " + probeID + " on endpoint " + endPointName  + " - DS id: " + dsID);
            turnProbeOn(probeID);
            
            Thread.sleep(5000);
            
            System.out.println("Turning off probe " + probeID + " on endpoint " + endPointName + " - DS id: " + dsID);
            turnProbeOff(probeID);
        }
        catch (InterruptedException ex) {
            return;
        } 
        catch (JSONException ex) {
            throw new Exception("Test Case MemoryInfoProbe Failed! " + "\nReason: " + ex.getMessage());
        }  
    }
    
    private void testDockerProbe(String probeName, String dsID, String serviceID, String sliceID) throws Exception { 
        String probeClassName = "eu.fivegex.demo.probes.docker.DockerProbe";
        JSONObject out;
        
        try {
            System.out.println("Creating probe on endpoint: " + endPointName + " - DS id: " + dsID);
            System.out.println("Dinamically loading probe class: " + probeClassName);
            
            out = loadProbeOnDsByID(dsID, probeClassName, dockerHost + "+" +
                                     dockerPort + "+" +
                                     probeName + "+" +
                                     dockerContainerID + "+" +
                                     dockerContainerName
                                    );
            
            String probeID = out.getString("createdProbeID");
            
            System.out.println("Setting serviceID " + serviceID + " on probe " + probeID + " on endpoint " + endPointName  + " - DS id: " + dsID);
            setProbeServiceID(probeID, serviceID);
            
            System.out.println("Setting sliceID " + sliceID + " on probe " + probeID + " on endpoint " + endPointName  + " - DS id: " + dsID);
            setProbeSliceID(probeID, sliceID);
            
            System.out.println("Turning on probe " + probeID + " on endpoint " + endPointName  + " - DS id: " + dsID);
            turnProbeOn(probeID);
            
            Thread.sleep(5000);
            
            System.out.println("Turning off probe " + probeID + " on endpoint " + endPointName + " - DS id: " + dsID);
            turnProbeOff(probeID);   
        }
        catch (InterruptedException ex) {
            return;
        }
        catch (JSONException ex) {
            throw new Exception("Test Case DockerProbe Failed! " + "\nReason: " + ex.getMessage());
        }
    }
    
    
    private MongoDBInteracter createMongoDBEntry(String serviceID, String probeName) throws JSONException, ParseException, IOException {
        String mongoDBAddress = "192.168.56.102";
        String mongoDBCollection = "cs";
        int mongoDBPort = 27017;
        
        JSONObject obj = new JSONObject();
        obj.put("agreementId", serviceID);
        obj.put("name", "Lattice Test");
        obj.put("maxResult", 10);
        obj.put("kpiList", new JSONArray().put(probeName));
        
        MongoDBInteracter mongo = new MongoDBInteracter(mongoDBAddress, mongoDBPort, mongoDBCollection);
        mongo.createMongoDBEntry(obj);
        return mongo; //just a bad thing
    }
    
    
    
    
    public static void main(String[] args) {
        LatticeTest client = null;
        String dsID = null;
        boolean errorStatus = false;
        
        try {
            Map <String, String> settings = new HashMap<>();
            Properties prop = new Properties();
            InputStream input = null;
            String propertiesFile = null;
            
            if (args.length == 0)
                propertiesFile = System.getProperty("user.home") + "/latticeTest.properties";
            else if (args.length == 1)
                propertiesFile = args[0];
            else {
                System.out.println("Please use: java LatticeTest [file.properties]");
                System.exit(1);
            }
            
            input = new FileInputStream(propertiesFile);
            prop.load(input);
            
            settings.put("RESTAddress", prop.getProperty("rest.address"));
            settings.put("RESTPort", prop.getProperty("rest.port"));
            
            settings.put("endPointAddress", prop.getProperty("endpoint.address"));
            settings.put("endPointName", prop.getProperty("endpoint.name"));
            settings.put("endPointUserName", prop.getProperty("endpoint.user"));
            
            settings.put("dataConsumerAddress", prop.getProperty("dc.address"));
            settings.put("dataConsumerPort", prop.getProperty("dc.port"));

            settings.put("controllerAddress", prop.getProperty("controller.address"));
            settings.put("remoteInfoPort", prop.getProperty("controller.remoteinfoport"));
            settings.put("localInfoPort", prop.getProperty("controller.localinfoport"));
            settings.put("localControlPort", prop.getProperty("controller.localcontrolport"));
            
            settings.put("dockerHost", prop.getProperty("docker.host"));
            settings.put("dockerPort", prop.getProperty("docker.port"));
            settings.put("dockerContainerID", prop.getProperty("docker.containerid"));
            settings.put("dockerContainerName", "testContainer@" + prop.getProperty("docker.host"));
            
            client = new LatticeTest(settings);

            // instantiating a new DS on the endpoint as per configuration (field endPointAddress)
            dsID = client.instantiateDS();
            
            // generating service/slice IDs to be associated to all the test probes
            String serviceID = ID.generate().toString();
            String sliceID = ID.generate().toString();
            String probeName = "testMemoryProbe";
            
            // creating entry in DB
            MongoDBInteracter m = client.createMongoDBEntry(serviceID, probeName);
            
            // instantiating some test probes on the previous DS
            client.testMemoryInfoProbe(probeName, dsID, serviceID, sliceID);
            //client.testDockerProbe("testDockerProbe", dsID, serviceID, sliceID);
            
            if (m.getMongoDBEntry(serviceID) == null)
                throw new Exception("Cannot find any entries with service ID " + serviceID + " in the DB");
                
            
        }
        catch (Exception e) {
            System.out.println("\n************************************************** TEST FAILED **************************************************\n" + 
                               e.getMessage() + 
                               "\n*****************************************************************************************************************\n");
            errorStatus = true;
        }
        finally {
            // stopping the previous instantiated DS anyway
            try {
                client.unloadDS(dsID);
            }
            catch (Exception e) { // the DS was either already stopped or not running
            }
        }
    if (errorStatus)
        System.exit(1);
    }
}
