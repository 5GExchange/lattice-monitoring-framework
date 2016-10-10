package eu.fivegex.monitoring.control.deployment;

import eu.reservoir.monitoring.core.ID;

public class SSHDeploymentInfo {
	private ID entityID;
	private Integer entityPID;

	public ID getEntityID() {
		return entityID;
	}
	public void setEntityID(ID dsId) {
		this.entityID = dsId;
	}
	public Integer getEntityPID() {
		return entityPID;
	}
	public void setEntityPID(Integer dsPid) {
		this.entityPID = dsPid;
	}
        
        @Override
	public String toString(){
		String infoString="";
		infoString += "\n ID: "+this.entityID;
		infoString += "\n PID: "+this.entityPID;
		return infoString;
	}
}
