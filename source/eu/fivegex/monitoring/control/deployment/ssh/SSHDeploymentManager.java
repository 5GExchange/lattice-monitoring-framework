/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.fivegex.monitoring.control.deployment.ssh;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import eu.fivegex.monitoring.control.deployment.DeploymentException;
import eu.fivegex.monitoring.control.deployment.EntityDeploymentDelegate;
import eu.fivegex.monitoring.im.delegate.InfoPlaneDelegate;
import eu.reservoir.monitoring.core.plane.AbstractAnnounceMessage.EntityType;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author uceeftu
 */
public abstract class SSHDeploymentManager implements EntityDeploymentDelegate {
    protected final EntityType entityType;
    protected final String entityFileName;
    protected final String localJarFilePath;
    protected final String remoteJarFilePath;
    protected final String jarFileName;
    protected final JSch jsch;
    protected final Map<String, Boolean> deploymentMonitor;
    protected final Map<String, Boolean> runMonitor;
    protected final Map<String, Long> entityDeployedOnEndPoint;
    protected final Map<String, SSHDeploymentInfo> entityRunningOnEndPoint;
    
    protected final InfoPlaneDelegate infoPlaneDelegate;
    
    Logger LOGGER = LoggerFactory.getLogger(SSHDeploymentManager.class);

    public SSHDeploymentManager(String localJarFilePath, String jarFileName, String remoteJarFilePath, String entityFileName, EntityType entityType, InfoPlaneDelegate info) {
        this.entityType = entityType;
        this.entityFileName = entityFileName;
        this.localJarFilePath = localJarFilePath;
        this.remoteJarFilePath = remoteJarFilePath;
        this.jarFileName = jarFileName;
        this.jsch = new JSch();

        this.deploymentMonitor = new ConcurrentHashMap<>();
        this.runMonitor = new ConcurrentHashMap<>();
        
        this.entityDeployedOnEndPoint = new ConcurrentHashMap<>();
        this.entityRunningOnEndPoint = new ConcurrentHashMap<>(); 
        
        this.infoPlaneDelegate = info;
    }

    /*
    protected Session connect(String endPoint, String userName) throws JSchException {
        Session session = this.jsch.getSession(userName, endPoint, 22);
        //this is just for testing: using the same password here regardless of the actual userName
        session.setPassword("osboxes.org");
        session.setConfig("StrictHostKeyChecking", "no"); //ignore unknown hosts
        session.connect(3000);
        return session;
    }
    */

    protected Session connectWithKey(String endPoint, String userName) throws JSchException {
        String privateKeyFile = System.getProperty("user.home") + "/.ssh/id_rsa";
        LOGGER.debug("Using identity from file: " + privateKeyFile);
        jsch.addIdentity(privateKeyFile);
        Session session = jsch.getSession(userName, endPoint, 22); // @TODO: port should be also a parameter
        session.setConfig("PreferredAuthentications", "publickey");
        session.setConfig("StrictHostKeyChecking", "no"); //ignore unknown hosts
        session.connect(3000);
        return session;
    }

    @Override
    public boolean deployEntity(String endPoint, String userName) throws DeploymentException {
        String endPointAddress;
        try {
            endPointAddress = InetAddress.getByName(endPoint).getHostAddress();
        } catch (UnknownHostException e) {
            throw new DeploymentException("Error while resolving endPoint address " + e.getMessage());
        }
        deploymentMonitor.putIfAbsent(endPointAddress, true);
        synchronized (deploymentMonitor.get(endPointAddress)) {
            LOGGER.debug("Deploying " + entityType);
            File jarFile = new File(this.localJarFilePath + "/" + this.jarFileName);
                if (!jarFile.exists()) {
                    throw new DeploymentException("Error: file " + this.localJarFilePath + "/" + this.jarFileName + " does not exist");
                }

            if (this.isEntityDeployed(endPointAddress) && jarFile.lastModified() <= getJarModificationDate(endPointAddress)) {
                return false;
            }
            Session session = null;
            ChannelSftp channelSftp = null;
            try {
                session = this.connectWithKey(endPointAddress, userName);
                Channel channel = session.openChannel("sftp");
                channel.connect(3000);
                channelSftp = (ChannelSftp) channel;
                channelSftp.put(this.localJarFilePath + "/" + this.jarFileName, this.remoteJarFilePath + "/" + this.jarFileName, ChannelSftp.OVERWRITE);
                LOGGER.debug("Copying: " + this.localJarFilePath + "/" + this.jarFileName 
                                         + "to: " + this.remoteJarFilePath + "/" + this.jarFileName);
                //adding info to the map
                entityDeployedOnEndPoint.putIfAbsent(endPointAddress, jarFile.lastModified());
            } catch (JSchException | SftpException e) {
                throw new DeploymentException("Error while deploying " + entityType + " on " + endPointAddress + ", " + e.getMessage());
            } finally {
                if (session != null && channelSftp != null) {
                    channelSftp.disconnect();
                    session.disconnect();
                }
            }
        }
        deploymentMonitor.remove(endPointAddress);
        return true;
    }

    

    @Override
    public boolean stopEntity(String endPoint, String userName) throws DeploymentException {
        Session session = null;
        Channel channel = null;
        String endPointAddress = endPoint;
        
        try {
            endPointAddress = InetAddress.getByName(endPoint).getHostAddress();
        
            synchronized (this.entityRunningOnEndPoint.get(endPointAddress)) {
                SSHDeploymentInfo endPointInfo = this.entityRunningOnEndPoint.get(endPointAddress);
                if (endPointInfo == null) {
                    return false;
                }

                session = this.connectWithKey(endPointAddress, userName);
                LOGGER.debug("Stopping " + entityType);
                String command = "kill " + endPointInfo.getEntityPID();
                channel = session.openChannel("exec");
                ((ChannelExec) channel).setCommand(command);
                channel.connect(3000);
                while (true) {
                    if (channel.isClosed()) {
                        if (channel.getExitStatus() == 0) {
                            this.entityRunningOnEndPoint.remove(endPointAddress);
                            break;
                        } else {
                            // the process is likely to be already stopped: removing from the map
                            this.entityRunningOnEndPoint.remove(endPointAddress);
                            throw new DeploymentException("exit-status: " + channel.getExitStatus());
                        }
                    }
                    Thread.sleep(500);
                }
            }
        } catch (JSchException | IOException | DeploymentException e) {
                throw new DeploymentException("Error while stopping " + entityType + " on " + endPointAddress + ", " + e.getMessage());
        } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
        } finally {
                if (session != null && channel != null) {
                    channel.disconnect();
                    session.disconnect();
                }
            }
        return true;
    }

    @Override
    public boolean isEntityDeployed(String endPoint) {
        return entityDeployedOnEndPoint.containsKey(endPoint);
    }

    @Override
    public boolean isEntityRunning(String endPoint) {
        return entityRunningOnEndPoint.containsKey(endPoint);
    }
    
    private Long getJarModificationDate(String endPoint) {
        return entityDeployedOnEndPoint.get(endPoint);
    }
    
}
