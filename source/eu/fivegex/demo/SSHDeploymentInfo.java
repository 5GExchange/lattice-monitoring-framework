package eu.fivegex.demo;

import eu.reservoir.monitoring.core.ID;

public class SSHDeploymentInfo {
	private String endPoint;
	private ID DsId;
	private Integer DsPid;
	
	public String getEndPoint() {
		return endPoint;
	}
	public void setEndPoint(String endPoint) {
		this.endPoint = endPoint;
	}
	public ID getDsId() {
		return DsId;
	}
	public void setDsId(ID dsId) {
		DsId = dsId;
	}
	public Integer getDsPid() {
		return DsPid;
	}
	public void setDsPid(Integer dsPid) {
		DsPid = dsPid;
	}
	public String DsInfoToString(){
		String DsInfoString="";
		DsInfoString += "\n endPoint: "+this.endPoint;
		DsInfoString += "\n ID: "+this.DsId;
		DsInfoString += "\n PID: "+this.DsPid;
		return DsInfoString;
	}
}
