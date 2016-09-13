package eu.fivegex.monitoring.control.deployment;

import eu.reservoir.monitoring.core.ID;

public class SSHDeploymentInfo {
	private ID dsId;
	private Integer dsPid;

	public ID getDsId() {
		return dsId;
	}
	public void setDsId(ID dsId) {
		this.dsId = dsId;
	}
	public Integer getDsPid() {
		return dsPid;
	}
	public void setDsPid(Integer dsPid) {
		this.dsPid = dsPid;
	}
        
        @Override
	public String toString(){
		String dsInfoString="";
		dsInfoString += "\n dsID: "+this.dsId;
		dsInfoString += "\n PID: "+this.dsPid;
		return dsInfoString;
	}
}
