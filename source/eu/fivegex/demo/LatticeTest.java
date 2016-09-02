package eu.fivegex.demo;

import eu.reservoir.monitoring.core.ID;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;

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

    /**
     * Construct a VimClient
     * using defaults of localhost and port 6666
     */
    public LatticeTest() throws UnknownHostException, IOException {
        this("localhost", 6666);
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

        //Logger.getLogger("log").logln(USR.STDOUT, "globalControllerURI: " + vimURI);

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
            System.out.println(uri);
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
            System.out.println(uri);
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
            System.out.println(uri);
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
            System.out.println(uri);
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
    
    
    
    
    public static void main(String[] args) {
        try {
            LatticeTest client = new LatticeTest("localhost", 6666);

            /* just for testing */

            JSONObject out;

            String endPointAddress = "172.16.1.11";
            String endPoint = "test-vnf.novalocal";

            String dataConsumerAddress = "172.16.1.7";
            String dataConsumerPort = "22997";
            String controllerAddress = "172.16.1.7";
            String remoteInfoPort = "6699";
            String localInfoPort = "9999";
            String localControlPort = "1111";
            
            String userName = "lattice"; //"osboxes";
            
            //String probeClassName = "eu.fivegex.demo.probes.RandomProbe";
            
            String probeClassName = "eu.fivegex.demo.probes.docker.DockerProbe";
            String dockerHost = "172.16.1.15";
            String dockerPort = "4243";
            String probeName = "dockerTestProbe";
            String dockerContainerID = "40a0d720d23c";
            String dockerContainerName = "testContainer@"+ dockerHost;

            System.out.println("Deploying DS on endpoint: " + endPoint);
            
            out = client.deployDS(endPoint, userName, dataConsumerAddress + "+" + 
                                                      dataConsumerPort + "+" +
                                                      controllerAddress + "+" +
                                                      remoteInfoPort + "+" +
                                                      localInfoPort + "+" +
                                                      localControlPort
                                 );
            
            System.out.println(out);
            System.in.read();

            String dsID = out.getString("ID");
            System.out.println("Creating probe on endpoint: " + endPoint + " - DS id: " + dsID);
            System.out.println("Dinamically loading probe class: " + probeClassName);
            
            //out = client.loadProbeOnDsByID(dsID, probeClassName, "myProbe+myAttribute+15");
            out = client.loadProbeOnDsByID(dsID, probeClassName, dockerHost + "+" +
                                                                 dockerPort + "+" +
                                                                 probeName + "+" +
                                                                 dockerContainerID + "+" +
                                                                 dockerContainerName
                                          );
            
            System.out.println(out);
            System.in.read();

            String probeID = out.getString("createdProbeID");
            System.out.println("Turning on probe " + probeID + " on endpoint " + endPoint  + " - DS id: " + dsID);
            client.turnProbeOn(probeID);
            System.in.read();

            System.out.println("Turning off probe " + probeID + " on endpoint " + endPoint + " - DS id: " + dsID);
            client.turnProbeOff(probeID);
            System.in.read();

            System.out.println("Unloading probe: " + probeID + " on endpoint " + endPoint + " - DS id: " + dsID);
            client.unloadProbe(probeID);
            System.in.read();

            System.out.println("Stopping DS on endpoint: "  + endPointAddress + " - DS id: " + dsID);
            out = client.stopDS(endPointAddress, userName);
            System.out.println(out);
            System.in.read();

            /* this should fail */
            System.out.println("Creating probe on endpoint: " + endPoint + " - DS id: " + dsID);
            System.out.println("Dinamically loading probe class: " + probeClassName);
            
            //out = client.loadProbeOnDsByID(dsID, probeClassName, "myProbe+myAttribute+15");
            out = client.loadProbeOnDsByID(dsID, probeClassName, dockerHost + "+" +
                                                                 dockerPort + "+" +
                                                                 probeName + "+" +
                                                                 dockerContainerID + "+" +
                                                                 dockerContainerName
                                          );
            
            System.out.println(out);
            System.in.read();
        }
        catch (Exception e) {
            System.out.println("Error! " + e.getMessage());
        }
    }
}

    