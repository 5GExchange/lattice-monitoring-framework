/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.fivegex.monitoring.control.deployment;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import eu.fivegex.monitoring.control.DCNotFoundException;
import eu.fivegex.monitoring.control.controller.InformationManager;
import eu.reservoir.monitoring.core.ID;
import eu.reservoir.monitoring.core.plane.AbstractAnnounceMessage.EntityType;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 *
 * @author uceeftu
 */
public class SSHDataConsumersDeploymentManager extends SSHDeploymentManager {
    
    
    public SSHDataConsumersDeploymentManager (String localJarFilePath, String jarFileName, String remoteJarFilePath, String dcFileName, InformationManager info) {
        super(localJarFilePath, jarFileName, remoteJarFilePath, dcFileName, EntityType.DATACONSUMER, info);
    }
    
    // we are supposing that only one DS can be running on a given resource identified by its address (endpoint)
    // TODO this may be changed to support multiple DS on the same resources listening on different ports
    @Override
    public ID startEntity(String endPoint, String userName, String args) throws DeploymentException {
        ID entityID = null;
        String entityIDAsString = null;
        Integer pID = null;
        Session session = null;
        Channel channel = null;
        String endPointAddress;
        try {
            endPointAddress = InetAddress.getByName(endPoint).getHostAddress();
        } catch (UnknownHostException e) {
            throw new DeploymentException("Error while resolving endPoint address " + e.getMessage());
        }
        runMonitor.putIfAbsent(endPointAddress, true);
        synchronized (runMonitor.get(endPointAddress)) {
            try {
                if (this.isEntityRunning(endPointAddress)) {
                    // if a DS is already running on the endpoint we just return its ID
                    return this.entityRunningOnEndPoint.get(endPointAddress).getEntityID();
                }
                this.deployEntity(endPoint, userName);
                
                session = this.connectWithKey(endPointAddress, userName);
                String jvm = "java"; //we assume the executable is in the PATH
                //args is a String object containing the args to be passed to the main of the entity being deployed
                //we could think of appending additional entries to the classpath to allow loading external probes
                
                // we are genereting here the entity ID and passing it as a parameter to the remote process
                entityID= ID.generate();
                entityIDAsString = entityID.toString();
                System.out.println("Generated entity ID: " + entityIDAsString);
                
                // this is only intended for testing the mongoDB reporter
                String externalJars = this.remoteJarFilePath + "/libs/mongo-java-driver-3.2.2.jar"; 
                
                String command = jvm + 
                                 " -cp " + this.remoteJarFilePath + "/" + this.jarFileName + ":" + externalJars + " " +
                                 this.entityFileName + " " + 
                                 entityIDAsString + " " +   
                                 args; 
                                 
                channel = session.openChannel("exec");
                ((ChannelExec) channel).setCommand(command);
                channel.connect(3000);
                
                // we are supposed to wait here until the announce message associated to the DS is received from the Announcelistener thread
                // or the timeout is reached (5 secs)
                informationManager.waitForDataConsumer(entityID, 7500);
                
                // if there is no Exception before we can now try to get the Data Source PID
                pID = informationManager.getDCPIDFromID(entityID);
                
            } catch (JSchException | DCNotFoundException e) {
                // we are here if there was an error while starting the remote Data Consumer
                String errorMessage = "Error while starting " + entityType + " on " + endPointAddress + "\n" + e.getMessage();
                if (channel != null) {
                    if (!channel.isClosed())
                        errorMessage += "\nthe SSH remote channel is still open - the DS may be up and running";
                    else
                        errorMessage += "\nremote process exit-status " + channel.getExitStatus();
                }
                
                // TODO we may now collect the error log file to report back the issue 
                throw new DeploymentException(errorMessage);
                
            } catch (InterruptedException ie) {
                System.out.println("Interrupted " + ie.getMessage());
            }
              finally {
                // as the command was started without a pty when we close the channel 
                // and session the remote command will continue to run
                if (channel != null && session != null) {
                    channel.disconnect();
                    session.disconnect();
                }
              }
            
            // now storing local information for Data Consumers deployment/starting management
            SSHDeploymentInfo entityInfo = new SSHDeploymentInfo();
            entityInfo.setEntityID(entityID);
            entityInfo.setEntityPID(pID);
            this.entityRunningOnEndPoint.putIfAbsent(endPointAddress, entityInfo);
            runMonitor.remove(endPointAddress);
            return entityID;
        }
    }
}
