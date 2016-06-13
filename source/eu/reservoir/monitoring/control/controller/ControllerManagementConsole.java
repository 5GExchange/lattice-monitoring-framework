/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.reservoir.monitoring.control.controller;

/**
 *
 * @author uceeftu
 */
public class ControllerManagementConsole extends RestConsole{
    public ControllerManagementConsole(Controller controller, int port) {

        setAssociated(controller);
        initialise(port);
    }

    @Override
    public void registerCommands() {
        defineRequestHandler("/probe/[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}/.*", new ProbeRestHandler());
        defineRequestHandler("/datasource/[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}/.*", new DataSourceRestHandler());
       }
    
}
