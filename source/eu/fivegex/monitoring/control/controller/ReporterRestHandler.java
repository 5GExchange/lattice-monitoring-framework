/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.fivegex.monitoring.control.controller;

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
class ReporterRestHandler extends BasicRequestHandler {

    Controller controller_;
    
    public ReporterRestHandler() {
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
        
        try {/*
            if (method.equals("PUT")) {
                if (name == null && segments.length == 2)
                    reporterOperation(request, response);   
                else
                    notFound(response, "PUT bad request");
            }
            
            else */if (method.equals("DELETE")) {
                    if (name != null && segments.length == 2) {
                        // looks like a delete
                        deleteReporter(request, response);
                    } else
                        notFound(response, "DELETE bad request");  
            }
            /*
            else if (method.equals("GET")) {
                    if (name == null && segments.length == 2)
                        getProbesCatalogue(request, response);
                    else 
                        notFound(response, "GET bad request");  
            }
            */
            else 
                badRequest(response, "Unknown method" + method);
            
            return true;
            
            } catch (IOException ex) {
                System.out.println("IOException" + ex.getMessage());
            } catch (JSONException jex) {
                System.out.println("JSONException" + jex.getMessage());
            } catch (ReporterNotFoundException idEx) {
                System.out.println("ReporterNotFoundException --- " + idEx.getMessage());
            } finally {
                        try {
                            response.close();
                            } catch (IOException ex) {
                                System.out.println("IOException" + ex.getMessage());
                              }
                      }
     return false;
    }

    private void reporterOperation(Request request, Response response) throws JSONException, IOException, ReporterNotFoundException {
        /*
        Scanner scanner;
        boolean success = true;
        String failMessage = null;
        JSONObject jsobj = new JSONObject();
        
        Query query = request.getQuery();
        Path path = request.getPath();
        String[] segments = path.getSegments(); 
        
        String reporterID;
        
        scanner = new Scanner (segments[1]);
        if (scanner.hasNext("[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}")) {
            reporterID = scanner.next();
            System.out.println("reporterID " + reporterID);
            scanner.close();
        }
        else {
            System.out.println("reporterID is not valid");
            scanner.close();
            complain(response, "reporter ID is not a valid UUID: " + segments[1]);
            return;
        }
        */
    }
    
    
    
    private void deleteReporter(Request request, Response response) throws JSONException, IOException, ReporterNotFoundException {
        boolean success = true;
        String failMessage = null;
        JSONObject jsobj = null;
        
        String name = request.getPath().getName();
        Scanner sc = new Scanner(name);

        if (sc.hasNext("[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}")) {
            String reporterID = sc.next();
            sc.close();

            jsobj = controller_.unloadReporter(reporterID);

            if (jsobj.get("success").equals("false")) {
                failMessage = (String)jsobj.get("msg");
                System.out.println("ReporterRestHandler: failure detected: " + failMessage);
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
            complain(response, "reporter ID is not valid: " + name);
        }
    
    }   
}