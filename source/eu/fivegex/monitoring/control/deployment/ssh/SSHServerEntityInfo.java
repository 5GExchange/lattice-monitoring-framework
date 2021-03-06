/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.fivegex.monitoring.control.deployment.ssh;

import eu.fivegex.monitoring.control.deployment.DeploymentException;
import eu.fivegex.monitoring.control.deployment.MonitorableEntityInfo;
import java.util.Objects;

/**
 *
 * @author uceeftu
 */
public class SSHServerEntityInfo extends MonitorableEntityInfo {
    String username;
    
    
    
    public SSHServerEntityInfo(String address, int port, String username) throws DeploymentException {
        super(address, port);
        this.username = username;
    }

    @Override
    public String getCredentials() {
        return username;
    }
    

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SSHServerEntityInfo) {
            return this.address.equals(((SSHServerEntityInfo) obj).getAddress());   
        }
        else
            return false;     
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 41 * hash + Objects.hashCode(this.address);
        return hash;
    }
 

    
    
}
