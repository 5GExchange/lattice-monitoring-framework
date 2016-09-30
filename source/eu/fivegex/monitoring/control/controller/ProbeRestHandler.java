/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.fivegex.monitoring.control.controller;

import eu.fivegex.monitoring.control.ProbeNotFoundException;
import cc.clayman.console.BasicRequestHandler;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Scanner;
import org.simpleframework.http.Path;
import org.simpleframework.http.Query;
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

        /*
        request.getQuery();
        
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
            if (method.equals("PUT")) {
                if (name == null && segments.length == 2)
                    probeOperation(request, response);   
                else
                    notFound(response, "PUT bad request");
            }
            
            else if (method.equals("DELETE")) {
                    if (name != null && segments.length == 2) {
                        // looks like a delete
                        deleteProbe(request, response);
                    } else
                        notFound(response, "DELETE bad request");  
            }
            
            else if (method.equals("GET")) {
                    if (name == null && segments.length == 2)
                        getProbesCatalogue(request, response);
                    else 
                        notFound(response, "GET bad request");  
            }
            
            else 
                badRequest(response, "Unknown method" + method);
            
            return true;
            
            } catch (IOException ex) {
                System.out.println("IOException" + ex.getMessage());
            } catch (JSONException jex) {
                System.out.println("JSONException" + jex.getMessage());
            } catch (ProbeNotFoundException idEx) {
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

    private void probeOperation(Request request, Response response) throws JSONException, IOException, ProbeNotFoundException {
        Scanner scanner;
        boolean success = true;
        String failMessage = null;
        JSONObject jsobj = new JSONObject();
        
        Query query = request.getQuery();
        Path path = request.getPath();
        String[] segments = path.getSegments(); 
        
        String probeID;
        
        scanner = new Scanner (segments[1]);
        if (scanner.hasNext("[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}")) {
            probeID = scanner.next();
            System.out.println("probeID " + probeID);
            scanner.close();
        }
        else {
            System.out.println("probeID is not valid");
            scanner.close();
            complain(response, "probe ID is not a valid UUID: " + segments[1]);
            return;
        }

        if (query.containsKey("serviceid")) {
            
            scanner = new Scanner(query.get("serviceid"));
            String serviceID;
            
            if (scanner.hasNext("[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}")) {
                serviceID = scanner.next();
                scanner.close();
            } else {
            	scanner.close();
                complain(response, "service ID is not a valid UUID");
                return;
            }   
            
        jsobj = controller_.setProbeServiceID(probeID, serviceID);    
        }
        
        else if (query.containsKey("sliceid")) {
            
            scanner = new Scanner(query.get("sliceid"));
            String sliceID;
            
            if (scanner.hasNext("[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}")) {
                sliceID = scanner.next();
                scanner.close();
            } else {
            	scanner.close();
                complain(response, "slice ID is not a valid UUID");
                return;
            }   
            
        jsobj = controller_.setProbeGroupID(probeID, sliceID);    
        }
        
        else if (query.containsKey("status")) {
            scanner = new Scanner(query.get("status"));
            
            String status;
            
            if (scanner.hasNext()) {
                status = scanner.next();
                scanner.close();
            } else {
            	scanner.close();
                complain(response, "status arg is empty");
                return;
            }
            
            switch (status) {
            case "off":
                jsobj = controller_.turnOffProbe(probeID);
                break;
            case "on":
                jsobj = controller_.turnOnProbe(probeID);
                break;
            default:
                complain(response, status + " is not a valid probe status");
                response.close();
                return;
            }
        }
        
        else if (query.containsKey("datarate")) {
            scanner = new Scanner(query.get("datarate"));
            
            String dataRate;
            
            if (scanner.hasNext()) {
                dataRate = scanner.next();
                scanner.close();
            } else {
            	scanner.close();
                complain(response, "datarate arg is empty");
                return;
            }
            
            jsobj = controller_.setProbeDataRate(probeID, dataRate);
        }
        
        
        else {
            complain(response, "no args have been specified");
        }

        if (jsobj.get("success").equals("false")) {
            failMessage = (String)jsobj.get("msg");
            System.out.println("ProbeRestHandler: failure detected: " + failMessage);
            success = false;
            
        }

        if (success) {
            PrintStream out = response.getPrintStream();       
            out.println(jsobj.toString());
        }
        
        else {
            response.setCode(302);
            PrintStream out = response.getPrintStream();       
            out.println(jsobj.toString());
        }
    }
    
    
    
    private void deleteProbe(Request request, Response response) throws JSONException, IOException, ProbeNotFoundException {
        boolean success = true;
        String failMessage = null;
        JSONObject jsobj = null;
        
        String name = request.getPath().getName();
        Scanner sc = new Scanner(name);

        if (sc.hasNext("[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}")) {
            String probeID = sc.next();
            sc.close();

            jsobj = controller_.unloadProbe(probeID);

            if (jsobj.get("success").equals("false")) {
                failMessage = (String)jsobj.get("msg");
                System.out.println("ProbeRestHandler: failure detected: " + failMessage);
                success = false;   
            }

            if (success) {
                PrintStream out = response.getPrintStream();       
                out.println(jsobj.toString());
            }

            else {
                response.setCode(302);
                PrintStream out = response.getPrintStream();       
                out.println(jsobj.toString());
            }
        
        }
        
        else {
            complain(response, "probe ID is not valid: " + name);
        }
    
    }
    
    private void getProbesCatalogue(Request request, Response response) throws JSONException, IOException {
        boolean success = true;
        String failMessage = null;
        JSONObject jsobj = null;
        
        Path path = request.getPath();
        String[] segments = path.getSegments();
        
        if (!segments[1].equals("catalogue")) {
            badRequest(response, segments[1] + "is not a valid path");
            response.close();
            return;
        }
        
        jsobj = controller_.getProbesCatalogue();

        if (jsobj.get("success").equals("false")) {
            failMessage = (String)jsobj.get("msg");
            System.out.println("getProbesCatalogue: failure detected: " + failMessage);
            success = false;   
        }

        if (success) {
            PrintStream out = response.getPrintStream();       
            out.println(jsobj.toString());
        }

        else {
            response.setCode(302);
            PrintStream out = response.getPrintStream();       
            out.println(jsobj.toString());
        }

    }
    
}