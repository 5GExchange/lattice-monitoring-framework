/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.reservoir.monitoring.core;

import eu.fivegex.monitoring.utils.DockerDataSourceConfigurator;

/**
 *
 * @author uceeftu
 */
public interface DockerDataSource {
    public DockerDataSourceConfigurator getDataSourceConfigurator();
    
    public void setDataSourceConfigurator(DockerDataSourceConfigurator dataSourceConfigurator);
}
