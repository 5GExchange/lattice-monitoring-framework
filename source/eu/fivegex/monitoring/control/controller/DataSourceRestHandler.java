/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.fivegex.monitoring.control.controller;

import cc.clayman.console.BasicRequestHandler;
import eu.fivegex.monitoring.control.JSONControlInterface;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Scanner;
import org.simpleframework.http.Path;
import org.simpleframework.http.Query;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;

/**
 *
 * @author uceeftu
 */
class DataSourceRestHandler extends BasicRequestHandler {

    JSONControlInterface controllerInterface;
    private Logger LOGGER = LoggerFactory.getLogger(DataSourceRestHandler.class);
    
    public DataSourceRestHandler() {
    }
    
    
     @Override
    public boolean handle(Request request, Response response) {
        // get Controller
        controllerInterface = (JSONControlInterface) getManagementConsole().getAssociated();
        
        LOGGER.debug("-------- REQUEST RECEIVED --------\n" + request.getMethod() + " " +  request.getTarget());
        
        
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
            switch (method) {
                case "POST":
                    if (name == null && segments.length == 3)
                        createProbe(request, response);
                    else if (name == null && segments.length == 1) {
                        deployDS(request,response);
                    }
                    else
                        notFound(response, "POST bad request");
                    break;
                case "DELETE":
                    if (name == null && segments.length == 1) {
                        stopDS(request,response);
                    }
                    break;
                case "GET":
                    if (name == null && segments.length == 1)
                        getDataSources(request, response);
                    else
                        if (segments.length == 2 && name != null)
                            getDataSourceInfo(request, response);
                        else
                            notFound(response, "GET bad request");
                    break;   
                default:
                    badRequest(response, "Unknown method" + method);
                    return false;
            }
            
            
            return true;
            
            } catch (IOException ex) {
                LOGGER.error("IOException" + ex.getMessage());
            } catch (JSONException jex) {
                LOGGER.error("JSONException" + jex.getMessage());
            }
             finally {
                        try {
                            response.close();
                            } catch (IOException ex) {
                                LOGGER.error("IOException" + ex.getMessage());
                              }
                      }
     return false;
    }

    private void createProbe(Request request, Response response) throws JSONException, IOException {
        Path path = request.getPath();
        String[] segments = path.getSegments(); 
        Query query = request.getQuery();
        
        String dsID;
        //String dsName;
        String className;
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
            jsobj = controllerInterface.loadProbe(dsID, className, rawArgs);
        }
        
//      this was to deploy a DS by name
//        else {
//            dsName = segments[1];
//            System.out.println("dsName: " + dsName);
//            dsID = controllerInterface.getResolver().getDSIDFromName(dsName);
//            System.out.println("dsID: " + dsID);
//            jsobj = controllerInterface.loadProbe(dsID, className, rawArgs);  
//        }
        
        if (!jsobj.getBoolean("success")) {
            failMessage = (String)jsobj.get("msg");
            LOGGER.error("createProbe: failure detected: " + failMessage);
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
    
    
    private void deployDS(Request request, Response response) throws JSONException, IOException {
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
        
        jsobj = controllerInterface.startDS(endPoint, userName, rawArgs);
        
        if (!jsobj.getBoolean("success")) {
            failMessage = (String)jsobj.get("msg");
            LOGGER.error("startDS: failure detected: " + failMessage);
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
    
    
    private void stopDS(Request request, Response response) throws JSONException, IOException {
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
        
        jsobj = controllerInterface.stopDS(endPoint, userName);
        
        if (!jsobj.getBoolean("success")) {
            failMessage = (String)jsobj.get("msg");
            LOGGER.error("stopDS: failure detected: " + failMessage);
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
    
    private void getDataSources(Request request, Response response) throws JSONException, IOException {
        boolean success = true;
        String failMessage = null;
        JSONObject jsobj = null;
        
        jsobj = controllerInterface.getDataSources();

        if (!jsobj.getBoolean("success")) {
            failMessage = (String)jsobj.get("msg");
            LOGGER.error("getDataSources: failure detected: " + failMessage);
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

    private void getDataSourceInfo(Request request, Response response) throws IOException, JSONException {
        Path path = request.getPath();
        
        boolean success = true;
        String failMessage;
        JSONObject jsobj;
        
        String dsID;
        
        Scanner scanner = new Scanner(path.getName());
        
        if (scanner.hasNext("[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}")) {
            dsID = scanner.next();
            scanner.close();
        }
        else {
            LOGGER.error("dsID is not valid");
            scanner.close();
            complain(response, "data source ID is not a valid UUID: " + path.getName());
            return;
        }
        
        jsobj = controllerInterface.getDataSourceInfo(dsID);

        if (!jsobj.getBoolean("success")) {
            failMessage = (String)jsobj.get("msg");
            LOGGER.error("getDataSourceName failure: " + failMessage);
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