/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.fivegex.monitoring.control.controller;

import eu.fivegex.monitoring.control.DCNotFoundException;
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
class DataConsumerRestHandler extends BasicRequestHandler {
    
    Controller controller_;

    public DataConsumerRestHandler() {
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
            if (method.equals("POST")) {
                if (name == null && segments.length == 3)
                    loadReporter(request, response);
                else if (name == null && segments.length == 1) {
                    //TODO: deployDC(request,response);
                }
                else
                    notFound(response, "POST bad request");
            }
            
            else if (method.equals("GET")) {
                    if (name == null && segments.length == 3) {
                        getDCMeasurementRate(request, response);
                    }
                    else
                        notFound(response, "GET bad request");
            } 
            
            return true;
            
            } catch (IOException ex) {
                System.out.println("IOException" + ex.getMessage());
            } catch (JSONException jex) {
                System.out.println("JSONException" + jex.getMessage());
            } catch (DCNotFoundException idEx) {
                System.out.println("DCNotFoundException --- " + idEx.getMessage());
            } finally {
                        try {
                            response.close();
                            } catch (IOException ex) {
                                System.out.println("IOException" + ex.getMessage());
                              }
                      }
     return false;
    }
   
   
    private void loadReporter(Request request, Response response) throws JSONException, IOException, DCNotFoundException {
        Path path = request.getPath();
        String[] segments = path.getSegments(); 
        Query query = request.getQuery();
        
        String dcID;
        String className;
        //String rawArgs="";
        String rawArgs=null;
        
        if (query.containsKey("className")) {
            className = query.get("className");
        } else {
            badRequest(response, "missing arg className");
            response.close();
            return;
        }
        
        if (query.containsKey("args")) {
            rawArgs = query.get("args");
            rawArgs = rawArgs.trim();
            rawArgs = rawArgs.replaceAll("\\+", " ");
        }

        boolean success = true;
        String failMessage = null;
        JSONObject jsobj = null;
        
        if (segments[1].matches("[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}") && segments[2].equals("reporter")) {
            dcID = segments[1];
            jsobj = controller_.loadReporter(dcID, className, rawArgs);
        }
        
        else {
            badRequest(response, "wrong path");
            response.close();
            return;
        }
        
        if (jsobj.get("success").equals("false")) {
            failMessage = (String)jsobj.get("msg");
            System.out.println("loadReporter: failure detected: " + failMessage);
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
    
    
    private void getDCMeasurementRate(Request request, Response response) throws JSONException, IOException, DCNotFoundException {
        Scanner scanner;
        boolean success = true;
        String failMessage = null;
        JSONObject jsobj = null;
        
        Path path = request.getPath();
        String[] segments = path.getSegments();
        
        String dcID;
        
        scanner = new Scanner (segments[1]);
        if (scanner.hasNext("[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}")) {
            dcID = scanner.next();
            System.out.println("dcID " + dcID);
            scanner.close();
        }
        else {
            System.out.println("DC ID is not valid");
            scanner.close();
            complain(response, "Data Consumer ID is not a valid UUID: " + segments[1]);
            return;
        }
        
        if (!segments[2].equals("rate")) {
            badRequest(response, segments[2] + "is not a valid path");
            response.close();
            return;
        }
        
        jsobj = controller_.getDataConsumerMeasurementRate(dcID);

        if (jsobj.get("success").equals("false")) {
            failMessage = (String)jsobj.get("msg");
            System.out.println("getDataConsumerMeasurementRate: failure detected: " + failMessage);
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
