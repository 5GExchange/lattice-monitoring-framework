/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.fivegex.monitoring.control.deployment;

import eu.reservoir.monitoring.core.ID;
import eu.reservoir.monitoring.core.plane.AbstractAnnounceMessage.EntityType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author uceeftu
 */
public abstract class LatticeEntityInfo {
    EntityType entityType;
    String entityClassName;
    String arguments;
    List<String> argumentsAsList = new ArrayList<>();
    
    ID id;
    int pID;
    
    boolean running;
    
    public LatticeEntityInfo(EntityType t, String name, String args) {
        this.entityType = t;
        this.entityClassName = name;
        this.arguments = args;
        this.argumentsAsList.addAll(Arrays.asList(args.split("\\+")));
    }

    public ID getId() {
        return id;
    }

    public void setId(ID id) {
        this.id = id;
    }

    public int getpID() {
        return pID;
    }

    public void setpID(int pID) {
        this.pID = pID;
    }

    public String getEntityClassName() {
        return entityClassName;
    }

    public String getArguments() {
        return arguments;
    }

    public EntityType getEntityType() {
        return entityType;
    }
    
    
    public void setRunning() {
        this.running = true;
    }
    
    public boolean isRunning() {
        return this.running;
    }
    
    
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + Objects.hashCode(this.entityType);
        hash = 29 * hash + Objects.hashCode(this.entityClassName);
        hash = 29 * hash + Objects.hashCode(this.arguments);
        return hash;
    }
    
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof LatticeEntityInfo) {
            LatticeEntityInfo other = (LatticeEntityInfo) obj;
            return this.entityClassName.equals(other.getEntityClassName()) && 
                   this.arguments.equals(other.getArguments()) &&
                   this.entityType.equals(other.getEntityType());
        }
        else
            return false;
    }
    
    

    
}
