/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.fivegex.monitoring.control.deployment;

import eu.reservoir.monitoring.core.ID;
import eu.reservoir.monitoring.core.plane.AbstractAnnounceMessage.EntityType;

/**
 *
 * @author uceeftu
 */
public class SSHDataSourcesDeploymentManager extends SSHDeploymentManager {
    
    
    public SSHDataSourcesDeploymentManager (String localJarFilePath, String jarFileName, String remoteJarFilePath, String dsFileName) {
        super(localJarFilePath, jarFileName, remoteJarFilePath, dsFileName, EntityType.DATASOURCE);
    }
    
    
    
    public static void main (String [] args) { 
        try {
            SSHDataSourcesDeploymentManager dm = new SSHDataSourcesDeploymentManager("/Users/uceeftu/Work/lattice-monitoring-framework/5Gex-Lattice/dist",
                                                               "5GEx-Lattice.jar",
                                                               "/tmp",
                                                               "eu.fivegex.demo.SimpleDataSourceDaemon"
                                                              );
            
            System.out.println(dm.deployEntity("192.168.56.101", "lattice"));
            //System.in.read();
            ID dataSourceID = dm.startEntity("192.168.56.101", "lattice", "not yet used");
            System.out.println("DataSouroceID: "+dataSourceID);
        } catch (DeploymentException  ex) {
            System.out.println(ex.getMessage());
          }
    }
}
