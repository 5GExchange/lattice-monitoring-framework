/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.fivegex.monitoring.control.controller;

import eu.fivegex.monitoring.control.ControlInterface;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;

/**
 *
 * @author uceeftu
 */
public final class JSONControllerManagementConsole extends RestConsole{
    public JSONControllerManagementConsole(ControlInterface<JSONObject, JSONException> controller, int port) {

        setAssociated(controller);
        initialise(port);
    }

    @Override
    public void registerCommands() {
        // /probe/uuid/?<args>
        defineRequestHandler("/probe/.*", new ProbeRestHandler());
        
        // /datasource/uuid/probe/?<args> and /datasource/name/probe/?<args>
        defineRequestHandler("/datasource/.*", new DataSourceRestHandler());
        
        // /dataconsumer/uuid/?<args> 
        defineRequestHandler("/dataconsumer/.*", new DataConsumerRestHandler());
        
        // /dataconsumer/uuid/?<args> 
        defineRequestHandler("/reporter/.*", new ReporterRestHandler());
        
        register(new UnknownCommand());
       }
    
}
