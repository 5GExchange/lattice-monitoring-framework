/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.fivegex.demo;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import us.monoid.json.JSONObject;

/**
 *
 * @author uceeftu
 */
public class LatticeTestTest {
    
    public LatticeTestTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of getPort method, of class LatticeTest.
     * @throws java.lang.Exception
     */
    @Test
    public void testGetPort() throws Exception {
        System.out.println("getPort");
        LatticeTest instance = new LatticeTest();
        int expResult = 0;
        int result = instance.getPort();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of loadProbeOnDsByID method, of class LatticeTest.
     */
    @Test
    public void testLoadProbeOnDsByID() throws Exception {
        System.out.println("loadProbeOnDsByID");
        String ID = "";
        String name = "";
        String args = "";
        LatticeTest instance = new LatticeTest();
        JSONObject expResult = null;
        JSONObject result = instance.loadProbeOnDsByID(ID, name, args);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of loadProbeOnDsByName method, of class LatticeTest.
     */
    @Test
    public void testLoadProbeOnDsByName() throws Exception {
        System.out.println("loadProbeOnDsByName");
        String dsName = "";
        String name = "";
        String args = "";
        LatticeTest instance = new LatticeTest();
        JSONObject expResult = null;
        JSONObject result = instance.loadProbeOnDsByName(dsName, name, args);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of turnProbeOn method, of class LatticeTest.
     */
    @Test
    public void testTurnProbeOn() throws Exception {
        System.out.println("turnProbeOn");
        String probeID = "";
        LatticeTest instance = new LatticeTest();
        JSONObject expResult = null;
        JSONObject result = instance.turnProbeOn(probeID);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of turnProbeOff method, of class LatticeTest.
     */
    @Test
    public void testTurnProbeOff() throws Exception {
        System.out.println("turnProbeOff");
        String probeID = "";
        LatticeTest instance = new LatticeTest();
        JSONObject expResult = null;
        JSONObject result = instance.turnProbeOff(probeID);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setProbeServiceID method, of class LatticeTest.
     */
    @Test
    public void testSetProbeServiceID() throws Exception {
        System.out.println("setProbeServiceID");
        String probeID = "";
        String serviceID = "";
        LatticeTest instance = new LatticeTest();
        JSONObject expResult = null;
        JSONObject result = instance.setProbeServiceID(probeID, serviceID);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of unloadProbe method, of class LatticeTest.
     */
    @Test
    public void testUnloadProbe() throws Exception {
        System.out.println("unloadProbe");
        String probeID = "";
        LatticeTest instance = new LatticeTest();
        JSONObject expResult = null;
        JSONObject result = instance.unloadProbe(probeID);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getProbesCatalogue method, of class LatticeTest.
     */
    @Test
    public void testGetProbesCatalogue() throws Exception {
        System.out.println("getProbesCatalogue");
        LatticeTest instance = new LatticeTest();
        JSONObject expResult = null;
        JSONObject result = instance.getProbesCatalogue();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of deployDS method, of class LatticeTest.
     */
    @Test
    public void testDeployDS() throws Exception {
        System.out.println("deployDS");
        String endPoint = "";
        String userName = "";
        String args = "";
        LatticeTest instance = new LatticeTest();
        JSONObject expResult = null;
        JSONObject result = instance.deployDS(endPoint, userName, args);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of stopDS method, of class LatticeTest.
     */
    @Test
    public void testStopDS() throws Exception {
        System.out.println("stopDS");
        String endPoint = "";
        String userName = "";
        LatticeTest instance = new LatticeTest();
        JSONObject expResult = null;
        JSONObject result = instance.stopDS(endPoint, userName);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of main method, of class LatticeTest.
     */
    @Test
    public void testMain() {
        System.out.println("main");
        String[] args = null;
        LatticeTest.main(args);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}
