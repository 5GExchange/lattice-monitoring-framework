/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.fivegex.monitoring.control.controller;

import eu.fivegex.monitoring.control.DSNotFoundException;
import cc.clayman.console.BasicRequestHandler;
import java.io.IOException;
import java.io.PrintStream;
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
class DataSourceRestHandler extends BasicRequestHandler {

    Controller controller_;
    
    public DataSourceRestHandler() {
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
                    createProbe(request, response);
                else if (name == null && segments.length == 1) {
                    deployDS(request,response);
                }
                else
                    notFound(response, "POST bad request");
            } else if (method.equals("DELETE")) {
                    if (name == null && segments.length == 1) {
                        stopDS(request,response);
                    }
              }
            
            return true;
            
            } catch (IOException ex) {
                System.out.println("IOException" + ex.getMessage());
            } catch (JSONException jex) {
                System.out.println("JSONException" + jex.getMessage());
            } catch (DSNotFoundException idEx) {
                System.out.println("DSNotFoundException --- " + idEx.getMessage());
            } finally {
                        try {
                            response.close();
                            } catch (IOException ex) {
                                System.out.println("IOException" + ex.getMessage());
                              }
                      }
     return false;
    }

    private void createProbe(Request request, Response response) throws JSONException, IOException, DSNotFoundException {
        Path path = request.getPath();
        String[] segments = path.getSegments(); 
        Query query = request.getQuery();
        
        String dsID;
        String dsName;
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
        
        if (segments[1].matches("[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}")) {
            dsID = segments[1];
            jsobj = controller_.loadProbe(dsID, className, rawArgs);
        }
        else {
            dsName = segments[1];
            System.out.println("dsName: " + dsName);
            dsID = controller_.getResolver().getDSIDFromName(dsName);
            System.out.println("dsID: " + dsID);
            jsobj = controller_.loadProbe(dsID, className, rawArgs);  
        }
        
        if (jsobj.get("success").equals("false")) {
            failMessage = (String)jsobj.get("msg");
            System.out.println("createProbe: failure detected: " + failMessage);
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
    
    
    private void deployDS(Request request, Response response) throws JSONException, IOException, DSNotFoundException {
        Path path = request.getPath();
        String[] segments = path.getSegments(); 
        Query query = request.getQuery();
        
        String endPoint;
        String userName;
        String rawArgs="";
        
        if (query.containsKey("endpoint"))
            endPoint = query.get("endpoint");
        else {
            badRequest(response, "missing endpoint arg");
            response.close();
            return;
        }
        
        if (query.containsKey("username"))
            userName = query.get("username");
        else {
            badRequest(response, "missing username args");
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
        
        jsobj = controller_.startDS(endPoint, userName, rawArgs);
        
        if (jsobj.get("success").equals("false")) {
            failMessage = (String)jsobj.get("msg");
            System.out.println("startDS: failure detected: " + failMessage);
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
    
    
    private void stopDS(Request request, Response response) throws JSONException, IOException, DSNotFoundException {
        Path path = request.getPath();
        String[] segments = path.getSegments(); 
        Query query = request.getQuery();
        
        String endPoint;
        String userName;
        
        if (query.containsKey("endpoint"))
            endPoint = query.get("endpoint");
        else {
            badRequest(response, "missing endpoint arg");
            response.close();
            return;
        }
        
        if (query.containsKey("username"))
            userName = query.get("username");
        else {
            badRequest(response, "missing username args");
            response.close();
            return;
        }
        
        boolean success = true;
        String failMessage = null;
        JSONObject jsobj = null;
        
        jsobj = controller_.stopDS(endPoint, userName);
        
        if (jsobj.get("success").equals("false")) {
            failMessage = (String)jsobj.get("msg");
            System.out.println("stopDS: failure detected: " + failMessage);
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