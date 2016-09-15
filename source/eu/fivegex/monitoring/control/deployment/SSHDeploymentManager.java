/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.fivegex.monitoring.control.deployment;

import eu.fivegex.monitoring.control.controller.DeploymentException;
import eu.fivegex.monitoring.control.controller.DeploymentDelegate;
import eu.reservoir.monitoring.core.ID;
import com.jcraft.jsch.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author uceeftu
 */
public class SSHDeploymentManager implements DeploymentDelegate {
    private final String localJarFilePath;
    private final String remoteJarFilePath;
    private final String jarFileName;
    private final String dsFileName;
    private final JSch jsch;
    
    private final Map<String, Boolean> deploymentMonitor;
    private final Map<String, Boolean> runMonitor;
    
    private final Map<String, Boolean> DSdeployedOnEndPoints;
    private final Map<String, SSHDeploymentInfo> DSrunningOnEndPoints;
    
    
    public SSHDeploymentManager (String localJarFilePath, String jarFileName, String remoteJarFilePath, String dsFileName) {
        this.localJarFilePath = localJarFilePath;
        this.remoteJarFilePath = remoteJarFilePath;
        this.jarFileName = jarFileName;
        this.dsFileName = dsFileName;
        this.jsch = new JSch();

        this.deploymentMonitor = new ConcurrentHashMap<>();
        this.runMonitor = new ConcurrentHashMap<>();
        
        this.DSdeployedOnEndPoints = new ConcurrentHashMap<>();
        this.DSrunningOnEndPoints = new ConcurrentHashMap<>();   
    }
    
    // if we do not use pkey authentication, we may use the same credentials for all the endpoints
    private Session connect(String endPoint, String userName) throws JSchException {
        Session session = this.jsch.getSession(userName, endPoint, 22);
        
        //this is just for testing: using the same password here regardless of the actual userName 
        session.setPassword("osboxes.org");
        session.setConfig("StrictHostKeyChecking", "no"); //ignore unknown hosts
        session.connect(3000);
        return session;
    }
    
    
    private Session connectWithKey(String endPoint, String userName) throws JSchException {
        String privateKeyFile = System.getProperty("user.home") + "/.ssh/id_rsa";
        System.out.println(privateKeyFile);        
        this.jsch.addIdentity(privateKeyFile);
        
        Session session = this.jsch.getSession(userName, endPoint, 22);
        session.setConfig("PreferredAuthentications", "publickey");
        session.setConfig("StrictHostKeyChecking", "no"); //ignore unknown hosts
        session.connect(3000);
        return session;
    }
    
    
    
    @Override
    public boolean deployDS(String endPoint, String userName) throws DeploymentException {
        String endPointAddress;
        try {
            endPointAddress = InetAddress.getByName(endPoint).getHostAddress();
        } catch (UnknownHostException e) {
                throw new DeploymentException("Error while resolving endPoint address " + e.getMessage());
          }
        
        deploymentMonitor.putIfAbsent(endPointAddress, true);
        
        synchronized (deploymentMonitor.get(endPointAddress)) {
                    System.out.println(Thread.currentThread().getName() + ": Deploying DS");

                    if (this.isDSdeployed(endPointAddress)) 
                        return false;
                    
                    Session session=null;
                    ChannelSftp c = null;
                    try {
                        session = this.connectWithKey(endPointAddress, userName);
                        Channel channel = session.openChannel("sftp");
                        channel.connect(3000);
                        c = (ChannelSftp) channel;

                        File jarFile = new File(this.localJarFilePath + "/" + this.jarFileName);
                        if (!jarFile.exists())
                            throw new DeploymentException("Error: file " + this.localJarFilePath + "/" + this.jarFileName + " does not exist");
                        
                        c.put(this.localJarFilePath + "/" + this.jarFileName, this.remoteJarFilePath + "/" + this.jarFileName, ChannelSftp.OVERWRITE);

                        System.out.println("Copying: " + this.localJarFilePath + "/" + this.jarFileName);
                        System.out.println("to: " + this.remoteJarFilePath + "/" + this.jarFileName);
                        
                        //adding info to the map
                        DSdeployedOnEndPoints.putIfAbsent(endPointAddress, true);

                    } catch (JSchException | SftpException e) {
                        throw new DeploymentException("Error while deploying DS on " + endPointAddress + ", " + e.getMessage());
                      }
                      finally {
                        if (c != null) {
                            c.disconnect();
                            session.disconnect();
                        }
                      }
        }
        deploymentMonitor.remove(endPointAddress);
        return true;  
    }
    
    
    @Override
    public ID startDS(String endPoint, String userName, String args) throws DeploymentException {
        String dsID = null;
        Integer pID = null;
        Session session=null;
        Channel channel=null;
        
        String endPointAddress;
        try {
            endPointAddress = InetAddress.getByName(endPoint).getHostAddress();
        } catch (UnknownHostException e) {
                throw new DeploymentException("Error while resolving endPoint address " + e.getMessage());
          }
        
        runMonitor.putIfAbsent(endPointAddress, true);
        synchronized (runMonitor.get(endPointAddress)) {
                    try {

                        if (this.isDSrunning(endPointAddress)) 
                            // if a DS is already running on the endpoint we just return its ID
                            return this.DSrunningOnEndPoints.get(endPointAddress).getDsId();

                        session = this.connectWithKey(endPointAddress, userName);

                        String jvm = "java"; //we assume the executable is in the PATH
                        
                        //args is a String object containing the args to be passed to the main of the DS being deployed
                        
                        //we could think of appending additional entries to the classpath to allow loading external probes
                        String command = jvm + " -cp " + this.remoteJarFilePath + "/" + this.jarFileName + " " + this.dsFileName + " " + args + "&";
                        
                        channel=session.openChannel("exec");
                        ((ChannelExec)channel).setCommand(command);

                        InputStream in=channel.getInputStream();
                        byte[] tmp=new byte[1024];
                        
                        channel.connect(3000);
                        
                        while (true) {
                                while (in.available() > 0) {
                                    int i=in.read(tmp, 0, 1024);

                                    if (i<0) break;

                                    String [] output = (new String(tmp, 0, i)).split("\\r?\\n"); // split the output in different lines
                                    // looking for the DS id (UUID) and PID
                                    for (String line: output) {
                                        if (line.matches("DataSource ID: [0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}")){
                                            dsID = line.substring("DataSource ID: ".length()); // getting the dsID as string removing "DataSource ID: "
                                        }

                                        else if (line.matches("Process ID: [0-9]+")){
                                                pID = Integer.parseInt(line.substring("Process ID: ".length())); // getting the PID"
                                        }
                                    }
                                }

                                if (channel.isClosed()) {
                                    if (in.available() > 0) continue;
                                    
                                    //TODO: it does not seem to be != 0 if the command is unsuccessful as the command is in background
                                    //we should consider starting the process in foreground and then using signal such as SIGSTOP and SIGCONT
                                    if (channel.getExitStatus() != 0) 
                                        throw new DeploymentException("Error while starting DS on " + endPointAddress + " exit-status: " + channel.getExitStatus());
                                    
                                    break;
                                }
                                
                                Thread.sleep(1000);       
                        }
                    } catch (JSchException | IOException e)
                        {
                        throw new DeploymentException("Error while starting DS on " + endPointAddress + ", " + e.getMessage());    
                        }
                      catch(InterruptedException ie) {/* we just swallow the exception as the thread shouldn't be interrupted */}

                      finally {
                        if (channel != null) {
                            channel.disconnect();
                            session.disconnect();
                        }
                      }

                    if (dsID != null && pID != null){
                        SSHDeploymentInfo dsInfo = new SSHDeploymentInfo();
                        dsInfo.setDsId(ID.fromString(dsID));
                        dsInfo.setDsPid(pID);
                        this.DSrunningOnEndPoints.putIfAbsent(endPointAddress, dsInfo);

                        runMonitor.remove(endPointAddress);
                        return ID.fromString(dsID);
                    }
                    else
                        throw new DeploymentException("Error: cannot get the DS ID, the endpoint " + endPointAddress + " may be unreachable");
        }  
    }
    

    @Override
    public boolean stopDS(String endPoint, String userName) throws DeploymentException {
        Session session=null;
        Channel channel=null;
        
        String endPointAddress;
        try {
            endPointAddress = InetAddress.getByName(endPoint).getHostAddress();
        } catch (UnknownHostException e) {
                throw new DeploymentException("Error while resolving endPoint address " + e.getMessage());
          }
        
        synchronized (this.DSrunningOnEndPoints.get(endPointAddress)) {
                    SSHDeploymentInfo endPointInfo = this.DSrunningOnEndPoints.get(endPointAddress);

                    if (endPointInfo == null)
                        return false;

                    try {
                        session = this.connectWithKey(endPointAddress, userName);
                        System.out.println(Thread.currentThread().getName() + ": Stopping DS");
                        
                        String command = "kill " + endPointInfo.getDsPid();

                        channel=session.openChannel("exec");
                        ((ChannelExec)channel).setCommand(command);

                        channel.connect(3000);

                        while (true) {
                            if (channel.isClosed()) {
                                if (channel.getExitStatus() == 0) {
                                    this.DSrunningOnEndPoints.remove(endPointAddress);
                                    return true;
                                }
                                else {
                                    // the process is likely to be already stopped removing from the map
                                    this.DSrunningOnEndPoints.remove(endPointAddress);
                                    throw new DeploymentException("Something went wrong while stopping DS");
                                }
                            }
                            Thread.sleep(500);
                        }
                    } catch (JSchException e) {
                        throw new DeploymentException("Error while stopping DS on " + endPointAddress + ", " + e.getMessage()); 
                      }
                      catch (InterruptedException ie) {/* we just swallow the exception as the thread shouldn't be interrupted*/}

                    return false;
        }
    }
     
        
    @Override
    public boolean isDSdeployed(String endPoint) {
        return DSdeployedOnEndPoints.containsKey(endPoint);
    }

    @Override
    public boolean isDSrunning(String endPoint) {
        return DSrunningOnEndPoints.containsKey(endPoint);
    }
   
    
    @Override
    public boolean deployProbeClass(String endpoint, String userName, String classFile) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    
    
    public static void main (String [] args) { 
        try {
            SSHDeploymentManager dm = new SSHDeploymentManager("/Users/uceeftu/Work/lattice-monitoring-framework/5Gex-Lattice/dist",
                                                               "5GEx-Lattice.jar",
                                                               "/tmp",
                                                               "eu.fivegex.demo.SimpleDataSourceDaemon"
                                                              );
            
            System.out.println(dm.deployDS("192.168.56.101", "lattice"));
            //System.in.read();
            ID dataSourceID = dm.startDS("192.168.56.101", "lattice", "not yet used");
            System.out.println("DataSouroceID: "+dataSourceID);
        } catch (DeploymentException  ex) {
            System.out.println(ex.getMessage());
          }
    }
}
