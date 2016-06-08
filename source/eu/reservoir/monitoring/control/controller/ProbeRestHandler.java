/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.reservoir.monitoring.control.controller;

import cc.clayman.console.BasicRequestHandler;
import eu.reservoir.monitoring.core.ID;
import java.io.IOException;
import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.simpleframework.http.Path;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;

/**
 *
 * @author uceeftu
 */
class ProbeRestHandler extends BasicRequestHandler {

    Controller controller_;
    
    public ProbeRestHandler() {
    }
    
    @Override
    public boolean handle(Request request, Response response) {
        // get Controller
        controller_ = (Controller) getManagementConsole().getAssociated();
        
        System.out.println("REQUEST: " + request.getMethod() + " " +  request.getTarget());
        
        
        long time = System.currentTimeMillis();
        
        response.set("Content-Type", "application/json");
        response.set("Server", "LatticeController/1.0 (SimpleFramework 4.0)");
        response.setDate("Date", time);
        response.setDate("Last-Modified", time);

        // get the path
        Path path = request.getPath();
        path.getDirectory();
        String name = path.getName();
        String[] segments = path.getSegments();

        // Get the method
        String method = request.getMethod();

        //request.getQuery();
        /*
        System.out.println("method: " + request.getMethod());
        System.out.println("target: " + request.getTarget());
        System.out.println("path: " + request.getPath());
        System.out.println("directory: " + request.getPath().getDirectory());
        System.out.println("name: " + request.getPath().getName());
        System.out.println("segments: " + java.util.Arrays.asList(request.getPath().getSegments()));
        System.out.println("query: " + request.getQuery());
        System.out.println("keys: " + request.getQuery().keySet());
        */
        
        try {
            if (method.equals("POST")) {
                System.out.println("Received Post");
                if (name != null && segments.length == 3)
                    probeOperation(request, response);   
                else
                    notFound(response, "POST bad request");
            }
            
            return true;
            
            } catch (IOException ex) {
                System.out.println("IOException" + ex.getMessage());
            } catch (JSONException jex) {
                System.out.println("JSONException" + jex.getMessage());
            } catch (ProbeIDNotFoundException idEx) {
                System.out.println("ProbeIDNotFoundException --- " + idEx.getMessage());
            } finally {
                        try {
                            response.close();
                            } catch (IOException ex) {
                                System.out.println("IOException" + ex.getMessage());
                              }
                      }
     return false;
    }

    private void probeOperation(Request request, Response response) throws JSONException, IOException, ProbeIDNotFoundException {
        
        
        
        boolean success = true;
        String failMessage = null;
        JSONObject jsobj = null;
        
        Path path = request.getPath();
        String[] segments = path.getSegments(); 
        
        String probeID = segments[1];
        String operation = segments[2];
        
        
        if (operation.equals("off"))
            jsobj = controller_.turnOffProbe(probeID);
        else if (operation.equals("on"))
            jsobj = controller_.turnOnProbe(probeID);
        else {
                badRequest(response, " arg is not valid operation: " + operation);
                response.close();
                return;
             }

        if (jsobj.get("success").equals(false)) {
            failMessage = (String)jsobj.get("msg");
            System.out.println("ProbeRestHandler: failure detected: " + failMessage);
            success = false;
            
        }

        if (success) {
            // now lookup all the saved details
            // and send them back as the return value
            PrintStream out = response.getPrintStream();

            // WAS JSONObject jsobj = controller_.findRouterInfoAsJSON(rID);

            out.println(jsobj.toString());
        } else {
            complain(response, "Error while managing probe: " + failMessage);
        }
    }
}