/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.reservoir.monitoring.core;

import eu.fivegex.monitoring.control.ProbeLoader;
import eu.fivegex.monitoring.utils.DockerDataSourceConfigurator;

/**
 *
 * @author uceeftu
 */
public interface ControllableDataSource extends DataSource, DataSourceDelegateInteracter {
    public ID addProbe(ProbeLoader p);
    
    public int getMyPID();
      
    public void setMyPID(int myPID);
    
    public DockerDataSourceConfigurator getDataSourceConfigurator();
    
    public void setDataSourceConfigurator(DockerDataSourceConfigurator dataSourceConfigurator);
}
