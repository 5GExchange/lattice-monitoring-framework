package eu.fivegex.monitoring.test;

import eu.reservoir.monitoring.appl.datarate.SamplesPerMinute;
import eu.reservoir.monitoring.core.Rational;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import us.monoid.json.JSONArray;

import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;


/**
 * Makes REST calls to the Lattice Controller through the REST API using Resty
 **/
public class DataRateController implements Runnable {
    LatticeTest restClient = null;
    String dataConsumerID;
    int maxRate = 8;
    int nProbes = 1;
    
    Boolean threadRunning;
    
    Map<String, Integer> probesMap;
    
    
    public DataRateController(Properties configuration) throws UnknownHostException, IOException {
        restClient = new LatticeTest(configuration);
        threadRunning = true;
        probesMap = new HashMap<>();
    }

    public String getDataConsumerID() {
        return dataConsumerID;
    }

    public void setDataConsumerID(String dataConsumerID) {
        this.dataConsumerID = dataConsumerID;
    }

    
    private void loadRandomProbe(String dsID, String probeName, String probeAttributeName, String value) throws JSONException {
        String probeClassName = "eu.fivegex.monitoring.appl.probes.RandomProbe";
        
        JSONObject out = restClient.loadProbe(dsID, probeClassName, probeName + "+" + probeAttributeName + "+" + value);
        String probeID = out.getString("createdProbeID");
        
        restClient.turnOnProbe(probeID);
    }
    
    
    private JSONArray getProbesOnDS(String dsID) throws JSONException {
        JSONObject out = restClient.getDataSourceInfo(dsID);
        return out.getJSONArray("probes");
    }
    
    
    private void adjustProbesRate() {
        Rational scalingFactor;
        Integer rateSum = 0;
        Integer rate;
        
        for (Integer probeRate : probesMap.values())
            rateSum += probeRate;
        
        scalingFactor = new Rational(maxRate * nProbes, rateSum);
        
        Rational newRate;
        for (String probeID : probesMap.keySet()) {
            newRate = scalingFactor.multiply(probesMap.get(probeID));
            System.out.println(newRate);
            try {
                restClient.setProbeDataRate(probeID, newRate.toString());
            } catch (JSONException e) {
                System.err.println("Error changing rate on probe " + probeID + " " + e.getMessage());
              }
        }
            
            
        
    }
    
    @Override
    public void run() {
        Integer currentRate;
        JSONObject out;
        
        while (threadRunning) {
            try {
                System.out.println("Checking rate");
                out = restClient.getDataConsumerMeasurementRate(dataConsumerID);
                currentRate = Integer.valueOf((String) out.get("rate"));
                
                System.out.println("Current rate: " + currentRate);
                if (currentRate > maxRate) {
                    // should change rate on probes
                    System.out.println("Changing probes rate");
                    adjustProbesRate();
                }
                
            } catch (JSONException e) {
                System.err.println(e.getMessage());
              }
            finally {
                try {
                    Thread.sleep(5000);
                    } catch (InterruptedException ie) {
                        return;
                        
                      } 
            }
        }
    }
   
    
    
    public static void main(String[] args) {
        DataRateController rateController = null;
        String dsID = null;
        String dcID = null;
        String reporterID = null;
        
        boolean errorStatus = false;
        
        try {
            Properties configuration = new Properties();
            InputStream input = null;
            String propertiesFile = null;
            
            if (args.length == 0)
                propertiesFile = System.getProperty("user.home") + "/rate.properties";
            else if (args.length == 1)
                propertiesFile = args[0];
            else {
                System.out.println("Please use: java LatticeTest [file.properties]");
                System.exit(1);
            }
            
            input = new FileInputStream(propertiesFile);
            configuration.load(input);
            
            rateController = new DataRateController(configuration);

            // instantiating a new DS on the endpoint as per configuration (field DSEndPointAddress)
            dsID = rateController.restClient.instantiateDS();
            
            dcID = rateController.restClient.instantiateDC();
            
            rateController.setDataConsumerID(dcID);
            
            for (Integer i=0; i < rateController.nProbes; i++)
                rateController.loadRandomProbe(dsID, "RandomProbe" + i, "RandomAttribute", i.toString());
            
            JSONArray probes = rateController.getProbesOnDS(dsID);
            
            
            
            Thread t = new Thread(rateController);
            t.start();
                        
            System.in.read();
            
            String probeID;
            int probeRate;
            JSONObject out;
            for (int i=0; i < probes.length(); i++) {
                probeID = (String) probes.get(i);
                out = rateController.restClient.getProbeDataRate(probeID);
                probeRate = out.getInt("rate");
                rateController.probesMap.put(probeID, probeRate);
                System.out.println("Probe: " + probeID + "- rate: " + probeRate);
            }
            
            System.in.read();
            rateController.threadRunning = false;
        }
        catch (Exception e) {
            System.out.println("*TEST FAILED*\n" + e.getMessage());
            errorStatus = true;
        }
        finally {
            // trying to stop the previous instantiated DS/DC anyway
            try {
                if (rateController.restClient != null) {
                    if (dsID != null)
                        rateController.restClient.unloadDS(dsID);
                    if (dcID != null)  {
                        if (reporterID != null) {
                            System.out.println("Unloading Reporter " + reporterID);
                            rateController.restClient.unloadReporter(reporterID);
                        }
                        rateController.restClient.unloadDC(dcID);
                    }
                }
            }
            catch (Exception e) { // the DS/DC was either already stopped or not running
            }
        }
    if (errorStatus)
        System.exit(1);
    }
}