/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.reservoir.monitoring.control.controller;

import java.util.Hashtable;

import eu.fivegex.demo.SSHDeploymentInfo;
import eu.reservoir.monitoring.core.ID;

/**
 *
 * @author uceeftu
 */
public interface DeploymentDelegate {
    public ID startDS(String endPoint, String userName, String confFile) throws DeploymentException;
    
    public boolean stopDS(String endPoint);
    
    public boolean deployDS(String endPoint, String userName) throws DeploymentException;
    
    public boolean deployProbeClass(String endPoint, String userName, String classFile);

    boolean isDSdeployed(String endPoint);
    
    boolean isDSrunning(String endPoint);
    
    /*
	public Hashtable<String, SSHDeploymentInfo> getHashDS();

	public void setHashDS(Hashtable<String, SSHDeploymentInfo> hashDS);

	public void putHashDS(String endPoint, SSHDeploymentInfo dsInfo);
    */
    
}
