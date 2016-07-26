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
    //private Session session;
    
    // we should also create a data structure to keep track of what is deployed where
    // we should also create a data structure to keep track of what is running where
    
    
    public SSHDeploymentManager (String localJarFilePath, String jarFileName, String remoteJarFilePath, String dsFileName) {
        this.localJarFilePath = localJarFilePath;
        this.remoteJarFilePath = remoteJarFilePath;
        this.jarFileName = jarFileName;
        this.dsFileName = dsFileName;
        this.jsch = new JSch();
    }
    
    // if we do not use pkey authentication, we may use the same credentials for all the endpoints
    private Session connect(String endPoint, String userName) throws JSchException {
        Session session = this.jsch.getSession(userName, endPoint, 22);
        
        //this is just for testing: using the same password here regardless of the actual userName 
        session.setPassword("osboxes.org");
        session.setConfig("StrictHostKeyChecking", "no"); //ignore unknown hosts
        session.connect(5000);
        return session;
    }
    
    
    @Override
    public ID startDS(String endPoint, String userName, String confFile) throws DeploymentException {
        String dsID="";
        Session session=null;
        Channel channel=null;
        try {
            session = this.connect(endPoint, userName);
            
            // we are starting the DS here without providing paramaters, this should be done either using a DS conf file
            // or passing the paramaters as an array
            
            //we could think of appending additional entries to the classpath to allow loading external probes
            String command="java -cp " + this.remoteJarFilePath + "/" + this.jarFileName + " " + this.dsFileName + " <&- &";
            
            channel=session.openChannel("exec");
            ((ChannelExec)channel).setCommand(command);

            channel.setInputStream(null);
            ((ChannelExec)channel).setErrStream(System.err);

            InputStream in=channel.getInputStream();

            channel.connect(5000);
            
            byte[] tmp=new byte[1024];
            
            while (true) {
                    while (in.available() > 0) {
                        int i=in.read(tmp, 0, 1024);
                        if (i<0) break;
                        
                        String [] output = (new String(tmp, 0, i)).split("\\r?\\n"); // split the output in different lines
                        
                        // looking for the DS id (UUID)
                        for (String line: output) {
                            if (line.matches("DataSource ID: [0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}"))
                                dsID = line.substring("DataSource ID: ".length()); // getting the dsID as string removing "DataSource ID: "
                        }
                        //System.out.println(dsID);   
                    }
                    
                    if (channel.isClosed()) {
                        if (in.available() > 0) continue;
                        System.out.println("exit-status: " + channel.getExitStatus());
                        if (channel.getExitStatus() != 0) // it does not seem to be != 0 if the command is unsuccessful
                            throw new DeploymentException("Error while starting DS on " + endPoint + " exit-status: " + channel.getExitStatus());
                        break;
                    }
                    
                    try { Thread.sleep(1000);
                        } catch(Exception ee) {}
            }
        
        } catch (JSchException | IOException e)
            {
            throw new DeploymentException("Error while starting DS on " + endPoint + ", " + e.getMessage());    
            }
          finally {
            if (channel != null) {
                channel.disconnect();
                session.disconnect();
            }
          }
        
    if (dsID != null)
        return ID.fromString(dsID);
    else
        throw new DeploymentException("Error: cannot get the DS ID, the endpoint" + endPoint + "may be unreachable");
    }
    
    

    @Override
    public boolean stopDS(String endpoint) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    
    @Override
    public boolean deployDS(String endPoint, String userName) throws DeploymentException {
        Session session=null;
        ChannelSftp c = null;
        try {
            session = this.connect(endPoint, userName);
            Channel channel = session.openChannel("sftp");
            channel.connect(5000);
            c = (ChannelSftp) channel;
            
            File jarFile = new File(this.localJarFilePath + "/" + this.jarFileName);
            if (!jarFile.exists())
                throw new DeploymentException("Error: file " + this.localJarFilePath + "/" + this.jarFileName + " does not exist");
            
            System.out.println("Copying: " + this.localJarFilePath + "/" + this.jarFileName);
            System.out.println("to: " + this.remoteJarFilePath + "/" + this.jarFileName);
            
            c.put(this.localJarFilePath + "/" + this.jarFileName, this.remoteJarFilePath + "/" + this.jarFileName);
        } catch (JSchException | SftpException e) {
            throw new DeploymentException("Error while deploying DS on " + endPoint + ", " + e.getMessage());
          }
          finally {
            if (c != null) {
                c.disconnect();
                session.disconnect();
            }
          }
        return true;
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
            
            System.out.println(dm.startDS("192.168.56.101", "osboxes", "not yet used"));
        } catch (DeploymentException | IOException ex) {
            System.out.println(ex.getMessage());
          }
    }
}
