package eu.fivegex.demo;

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
     * using defaults of localhost and port 8888
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

    
    public JSONObject loadProbe(String ID, String name, String args) throws JSONException {
        try {
            String uri = vimURI + "/datasource/" + ID + "/probe/?className=" + name + "&args=" + java.net.URLEncoder.encode(args, "UTF-8");
            
            // adding form data causes a POST
            JSONObject jsobj = rest.json(uri, form("")).toObject();

            return jsobj;

        } catch (IOException ioe) {
            throw new JSONException("loadProbe FAILED" + " IOException: " + ioe.getMessage());
        }
    }
    
    public static void main(String[] args) throws IOException, JSONException {
        RestClient client = new RestClient("localhost", 6666);
        
        Scanner keyboard = new Scanner(System.in);
        System.out.println("Enter the DS ID to load the probe on: ");
        String ID1 = keyboard.nextLine();
        
        //client.loadProbe(ID1, "eu.reservoir.demo.UserProcProbe", "ttys000");
        
        client.loadProbe(ID1, "eu.fivegex.demo.RandomProbe", "example+XXX+15");
    }
}

    