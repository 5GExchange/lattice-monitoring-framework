/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.reservoir.monitoring.core;

/**
 *
 * @author uceeftu
 */
public abstract class AbstractReporter implements ControllableReporter {
    /**
    * The Reporter ID
    */
    ID myId;
    
    /**
    * The Data Consumer ID the reporter is bound to
    */
    ID dcId;
        
    
    public AbstractReporter(String name) {
        myId = ID.generate();
        System.out.println("Reporter ID = " + myId);
        this.name = name;
    }
    
    
    @Override
    public ID getId() {
        return myId;
    }

    @Override
    public void setId(ID id) {
        this.myId = id;
    }
    String name;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }
  
    
    @Override
    public ID getDcId() {
        return dcId;
    }

    @Override
    public void setDcId(ID dcId) {
        this.dcId = dcId;
    }

    @Override
    public abstract void report(Measurement m);
    
}
