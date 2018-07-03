/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.fivegex.monitoring.appl.probes.rtt.unidirectional;

import eu.fivegex.monitoring.test.LatticeTest;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;

/**
 *
 * @author uceeftu
 */
public final class RTTTest {
    
    String username;
    Integer port;
    
    String controllerAddress;
    Integer controllerAPIPort;
    Integer controllerControlPort;
    Integer controllerInfoPort;
    
    String dcAddress;
    Integer dcPort;
    
    String sourceProbeMgmAddress;
    Integer sourceProbeMgmPort;
    String sourceProbeDataAddress;
    Integer sourceProbeDataPort;
    
    String destProbeMgmAddress;
    Integer destProbeMgmPort;
    String destProbeDataAddress;
    Integer destProbeDataPort;

    LatticeTest rest;
    
    Map<String, String> uuids;
    
    JSONObject configuration;
    
    
    public RTTTest(String filename) throws IOException {
        loadConfiguration(filename);
        parseConfiguration();
        init();
    }
   
    
    private void init() throws IOException {
        rest = new LatticeTest(controllerAddress, controllerAPIPort);
        uuids = new HashMap<>();
    }
    
    private void loadConfiguration(String filename) throws IOException {
        BufferedReader reader = null;
        
        try {
            reader = new BufferedReader(new FileReader(filename));
            
            String line;
            StringBuilder content = new StringBuilder();
        
            while ((line = reader.readLine()) != null) {
                content.append(line);
            }
            configuration = new JSONObject(content.toString());
            
        } catch (FileNotFoundException e) {
            throw e;
        } catch (JSONException je) {
            throw new IOException(je);
        }
        finally {
            if (reader != null)
                reader.close();
        }
    }
    
    
    private void parseConfiguration() throws IOException {
        try {
            username = configuration.getJSONObject("ssh").getString("username");
            port = configuration.getJSONObject("ssh").getInt("port");

            JSONObject latticeConf = configuration.getJSONObject("lattice");

            controllerAddress = latticeConf.getJSONObject("controller").getString("address");
            controllerAPIPort = latticeConf.getJSONObject("controller").getInt("apiPort");
            controllerControlPort = latticeConf.getJSONObject("controller").getInt("controlPort");
            controllerInfoPort = latticeConf.getJSONObject("controller").getInt("infoPort");

            dcAddress = latticeConf.getJSONObject("dataConsumer").getString("address");
            dcPort = latticeConf.getJSONObject("dataConsumer").getInt("port");

            JSONObject probes = latticeConf.getJSONObject("probes");

            sourceProbeMgmAddress = probes.getJSONObject("source").getString("mgmAddress");
            sourceProbeMgmPort = probes.getJSONObject("source").getInt("mgmPort");
            sourceProbeDataAddress = probes.getJSONObject("source").getString("dataAddress");
            sourceProbeDataPort = probes.getJSONObject("source").getInt("dataPort");

            destProbeMgmAddress = probes.getJSONObject("destination").getString("mgmAddress");
            destProbeMgmPort = probes.getJSONObject("destination").getInt("mgmPort");
            destProbeDataAddress = probes.getJSONObject("destination").getString("dataAddress");
            destProbeDataPort = probes.getJSONObject("destination").getInt("dataPort");
        } catch (JSONException je) {
            throw new IOException("Error while parsing configuration file:" + je.getMessage());
        }
    }
    
    
    public void deployComponents() throws JSONException {
        System.out.println("Deploying Lattice Components");
        String dcArgs = dcPort + "+" + controllerAddress + "+" + controllerInfoPort + "+" + controllerControlPort;
        String dsArgs = dcAddress + "+" + dcPort + "+" + controllerAddress + "+" + controllerInfoPort + "+" + controllerControlPort;
  
        uuids.put("dc", rest.startDC(dcAddress, "22", "lattice", dcArgs).getString("ID"));
        uuids.put("dssource", rest.startDS(sourceProbeMgmAddress, "22", "lattice", dsArgs).getString("ID"));
        uuids.put("dsdest", rest.startDS(destProbeMgmAddress, "22", "lattice", dsArgs).getString("ID"));
    }
    
    
    public void undeployComponents() throws JSONException {
        System.out.println("Undeploying Lattice Components");

        rest.stopDS(uuids.get("dssource"));
        rest.stopDS(uuids.get("dsdest"));
        rest.stopDC(uuids.get("dc"));
    }
    
    
    
    public void  loadProbes() throws JSONException {
        System.out.println("Loading monitoring probes");
        String sourceProbeArgs = "RTTSource" + "+" + sourceProbeMgmAddress + "+" + sourceProbeMgmPort + "+" + sourceProbeDataAddress + "+" + sourceProbeDataPort + "+" + destProbeDataAddress + "+" + destProbeDataPort; 
        String destProbeArgs = "RTTDestination" + "+" + destProbeMgmAddress + "+" + destProbeMgmPort + "+" + destProbeDataAddress + "+" + destProbeDataPort + "+" + sourceProbeMgmAddress + "+" + sourceProbeMgmPort;

        uuids.put("sourceprobe", rest.loadProbe(uuids.get("dssource"), "eu.fivegex.monitoring.appl.probes.rtt.unidirectional.RTTSourceProbe", sourceProbeArgs).getString("createdProbeID"));
        uuids.put("destprobe", rest.loadProbe(uuids.get("dsdest"), "eu.fivegex.monitoring.appl.probes.rtt.unidirectional.RTTDestProbe", destProbeArgs).getString("createdProbeID"));
    }
    
    
    public void  unloadProbes() throws JSONException {
        System.out.println("Unloading monitoring probes");
        rest.unloadProbe(uuids.get("sourceprobe"));
        rest.unloadProbe(uuids.get("destprobe"));
    }
    
    
    public void loadReporters() throws JSONException {
        System.out.println("Loading reporters");
        uuids.put("loggerreporter", rest.loadReporter(uuids.get("dc"), "eu.fivegex.monitoring.appl.reporters.LoggerReporter", "logger-reporter").getString("createdReporterID"));
    }
    
    
    public void unloadReporters() throws JSONException {
        System.out.println("Unloading reporters");
        rest.unloadReporter(uuids.get("loggerreporter"));
    }
    
    
    public void activateProbes() throws JSONException {
        System.out.println("Activating monitoring probes");
        rest.turnOnProbe(uuids.get("destprobe"));
        rest.turnOnProbe(uuids.get("sourceprobe"));
    }
    
    
    
    
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Error: configuration file must be provided at startup");
            System.exit(1);
        }
        
        String filename = args[0];
        
        try {
            RTTTest rttTest = new RTTTest(filename);
            
            rttTest.deployComponents();
            rttTest.loadReporters();
            rttTest.loadProbes();
            rttTest.activateProbes();
            
            System.in.read();
            
            rttTest.unloadProbes();
            rttTest.unloadReporters();
            rttTest.undeployComponents();
        } catch (IOException ioe) {
            System.err.println("Error while initializing the environment: " + ioe.getMessage());
        } catch (JSONException je) {
            System.err.println("Error while performing the test: " + je.getMessage());
        }
    }  
    
}
