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
import static us.monoid.web.Resty.data;
import static us.monoid.web.Resty.form;
import static us.monoid.web.Resty.content;
import static us.monoid.web.Resty.data;
import static us.monoid.web.Resty.form;

/**
 * Makes REST calls to VIM / GlobalController using Resty
 */
public class RestClient {
    // A URI for a VIM / GlobalController to interact with
    String vimURI;
    Resty rest;
    int port;

    /**
     * Construct a VimClient
     * using defaults of localhost and port 6666
     */
    public RestClient() throws UnknownHostException, IOException {
        this("localhost", 6666);
    }

    /**
     * Constructor for a VimClient
     * to the ManagementConsole of a VIM / GlobalController.
     * @param addr the name of the host
     * @param port the port the server is listening on
     */
    public RestClient(String addr, int port) throws UnknownHostException, IOException  {
        initialize(InetAddress.getByName(addr), port);
    }

    /**
     * Constructor for a VimClient
     * to the ManagementConsole of a VIM / GlobalController.
     * @param addr the InetAddress of the host
     * @param port the port the server is listening on
     */
    public RestClient(InetAddress addr, int port) throws UnknownHostException, IOException  {
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
            
            // adding form data causes a POST
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
    
    
    public static void main(String[] args) throws IOException, JSONException {
        RestClient client = new RestClient("localhost", 6666);
        
        Scanner keyboard = new Scanner(System.in);
        System.out.println("Enter the DS name to load the probe on: ");
        String dsName = keyboard.nextLine();
        
        //creating a random probe on DS dsName
        JSONObject out;
        System.out.println("Creating probe");
        out = client.loadProbeOnDsByName(dsName, "eu.fivegex.demo.RandomProbe", "myProbe+myAttribute+15");
        System.in.read();
        
        //getting created probe ID
        String probeID = (String) out.get("createdProbeID");
        
        //setting (random generated) service ID to the created probe
        System.out.println("Setting service ID on probe: " + probeID);
        client.setProbeServiceID(probeID, ID.generate().toString());
        System.in.read();
        
        //turning probe on
        System.out.println("turning on probe: " + probeID);
        client.turnProbeOn(probeID);
        System.in.read();
        
        //turing probe off
        System.out.println("turning off probe: " + probeID);
        client.turnProbeOff(probeID);
        System.in.read();
        
        //unloading the probe
        System.out.println("unloading probe: " + probeID);
        client.unloadProbe(probeID);
        System.in.read();
        }
}

    