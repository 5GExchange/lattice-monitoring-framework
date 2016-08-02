/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.fivegex.demo;

import eu.reservoir.monitoring.control.controller.DeploymentException;
import eu.reservoir.monitoring.control.controller.DeploymentDelegate;
import eu.reservoir.monitoring.core.ID;
import com.jcraft.jsch.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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
    
    private final Map<String, Boolean> syncDeployment;
    private final Map<String, Boolean> syncRunning;
    
    private final Map<String, Boolean> DSdeployedOnEndPoints;
    private final Map<String, SSHDeploymentInfo> DSrunningOnEndPoints;
    
    
    public SSHDeploymentManager (String localJarFilePath, String jarFileName, String remoteJarFilePath, String dsFileName) {
        this.localJarFilePath = localJarFilePath;
        this.remoteJarFilePath = remoteJarFilePath;
        this.jarFileName = jarFileName;
        this.dsFileName = dsFileName;
        this.jsch = new JSch();

        this.syncDeployment = new ConcurrentHashMap<>();
        this.syncRunning = new ConcurrentHashMap<>();
        
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
    
    
    @Override
    public ID startDS(String endPoint, String userName, String args) throws DeploymentException {
        String dsID="";
        Integer pID = null;
        Session session=null;
        Channel channel=null;
        
        syncRunning.putIfAbsent(endPoint, true);
        synchronized (syncRunning.get(endPoint)) {
                    System.out.println(Thread.currentThread().getName() + ": Starting DS - " + System.currentTimeMillis());
                    try {

                        if (this.isDSrunning(endPoint)) 
                            return null;

                        session = this.connect(endPoint, userName);
                        System.out.println(Thread.currentThread().getName() + ": Starting DS Session created - " + System.currentTimeMillis());
                        // we are starting the DS here without providing paramaters, this should be done either using a DS conf file
                        // or passing the paramaters as an array

                        String jvm = "java"; //we assume the executable is in the PATH
                        
                        //we could think of appending additional entries to the classpath to allow loading external probes
                        String command = jvm + " -cp " + this.remoteJarFilePath + "/" + this.jarFileName + " " + this.dsFileName + " " + args + "&";
                        System.out.println(command);
                        channel=session.openChannel("exec");
                        ((ChannelExec)channel).setCommand(command);

                        channel.setInputStream(null);
                        ((ChannelExec)channel).setErrStream(System.err);

                        InputStream in=channel.getInputStream();

                        channel.connect(3000);

                        System.out.println(Thread.currentThread().getName() + ": Starting DS command executed - " + System.currentTimeMillis());
                        byte[] tmp=new byte[1024];
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
                                    System.out.println("exit-status: " + channel.getExitStatus());
                                    if (channel.getExitStatus() != 0) // it does not seem to be != 0 if the command is unsuccessful
                                        throw new DeploymentException("Error while starting DS on " + endPoint + " exit-status: " + channel.getExitStatus());
                                    break;
                                }
                                Thread.sleep(100);       
                        }

                    } catch (JSchException | IOException e)
                        {
                        throw new DeploymentException("Error while starting DS on " + endPoint + ", " + e.getMessage());    
                        }
                      catch(InterruptedException ie) {/* we just swallow the exception as the thread shouldn't be interrupted*/}

                      finally {
                        if (channel != null) {
                            channel.disconnect();
                            session.disconnect();
                        }
                      }

                    System.out.println(Thread.currentThread().getName() + ": Starting DS parsed output - " + System.currentTimeMillis());

                    if (dsID != null && pID!=null){
                        SSHDeploymentInfo dsInfo = new SSHDeploymentInfo();
                        dsInfo.setDsId(ID.fromString(dsID));
                        dsInfo.setDsPid(pID);
                        this.DSrunningOnEndPoints.putIfAbsent(endPoint, dsInfo);

                        System.out.println(Thread.currentThread().getName() + ": Starting DS before returning - " + System.currentTimeMillis());
                        syncRunning.remove(endPoint);
                        return ID.fromString(dsID);
                    }
                    else
                        throw new DeploymentException("Error: cannot get the DS ID, the endpoint " + endPoint + " may be unreachable");
        }  
    }
    
    

    @Override
    public boolean stopDS(String endpoint) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    
    @Override
    public boolean deployDS(String endPoint, String userName) throws DeploymentException {
        syncDeployment.putIfAbsent(endPoint, true);
        
        synchronized (syncDeployment.get(endPoint)) {
                    System.out.println(Thread.currentThread().getName() + ": Deploying DS - " + System.currentTimeMillis());

                    if (this.isDSdeployed(endPoint)) 
                        return false;

                    System.out.println(Thread.currentThread().getName() + ": Starting Session - " + System.currentTimeMillis());
                    
                    Session session=null;
                    ChannelSftp c = null;
                    try {
                        session = this.connect(endPoint, userName);
                        Channel channel = session.openChannel("sftp");
                        channel.connect(3000);
                        c = (ChannelSftp) channel;

                        File jarFile = new File(this.localJarFilePath + "/" + this.jarFileName);
                        if (!jarFile.exists())
                            throw new DeploymentException("Error: file " + this.localJarFilePath + "/" + this.jarFileName + " does not exist");

                        System.out.println("Copying: " + this.localJarFilePath + "/" + this.jarFileName);
                        System.out.println("to: " + this.remoteJarFilePath + "/" + this.jarFileName);

                        c.put(this.localJarFilePath + "/" + this.jarFileName, this.remoteJarFilePath + "/" + this.jarFileName);

                        //adding info to the map
                        DSdeployedOnEndPoints.putIfAbsent(endPoint, true);

                        System.out.println(Thread.currentThread().getName() + ": After put - " + System.currentTimeMillis());


                    } catch (JSchException | SftpException e) {
                        throw new DeploymentException("Error while deploying DS on " + endPoint + ", " + e.getMessage());
                      }
                      finally {
                        if (c != null) {
                            c.disconnect();
                            session.disconnect();
                        }
                      }
                    System.out.println(Thread.currentThread().getName() + ": before end of synchronized block - " + System.currentTimeMillis());

        }
        syncDeployment.remove(endPoint);
        return true;  
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
            
            System.out.println(dm.deployDS("192.168.56.101", "osboxes"));
            System.in.read();
            ID dataSourceID = dm.startDS("192.168.56.101", "osboxes", "not yet used");
            System.out.println("DataSouroceID: "+dataSourceID);
        } catch (DeploymentException | IOException ex) {
            System.out.println(ex.getMessage());
          }
    }
}
