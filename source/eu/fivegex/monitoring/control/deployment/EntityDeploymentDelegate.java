/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.fivegex.monitoring.control.deployment;

import eu.reservoir.monitoring.core.ID;

/**
 *
 * @author uceeftu
 */
public interface EntityDeploymentDelegate {
    public ID startEntity(String endPoint, String userName, String params) throws DeploymentException;
    
    public boolean stopEntity(String endPoint, String userName) throws DeploymentException;
    
    public boolean deployEntity(String endPoint, String userName) throws DeploymentException;

    boolean isEntityDeployed(String endPoint);
    
    boolean isEntityRunning(String endPoint);    
}
