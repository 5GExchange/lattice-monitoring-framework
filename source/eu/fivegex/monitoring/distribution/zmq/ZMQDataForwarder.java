/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.fivegex.monitoring.distribution.zmq;

import org.zeromq.ZMQ;

/**
 *
 * @author uceeftu
 */
public class ZMQDataForwarder extends Thread {
    ZMQ.Context context;
    ZMQ.Socket backend;
    ZMQ.Socket frontend;
    
    String internalURI = "inproc://dataplane";
    
    int localPort;
    
    public ZMQDataForwarder(int localPort) {
        this.localPort = localPort;
        
        context = ZMQ.context(1);
        backend = context.socket(ZMQ.XPUB);
        frontend = context.socket(ZMQ.XSUB);
    }

    public ZMQ.Context getContext() {
        return context;
    }
    
    public String getInternalURI() {
        return internalURI;
    }
    
    public boolean startProxy() {
        this.setName("zmq-data-forwarder");
        this.start();
        return true;
    }
    
    public boolean stopProxy() {
        frontend.close();
        backend.close();
        //context.term();
        return true;
    }
    
    @Override
    public void run() {
        frontend.bind("tcp://*:" + localPort);
        backend.bind("tcp://*:" + (localPort + 1));
        backend.bind(internalURI);
        ZMQ.proxy(frontend, backend, null);
    }
}
