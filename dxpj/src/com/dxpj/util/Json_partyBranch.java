package com.dxpj.util;

public class Json_partyBranch {

	private String PartyBranchID;
	private String PartyBranchName;
	
	public Json_partyBranch (String PartyBranchID,String PartyBranchName) {
		this.PartyBranchID = PartyBranchID;
		this.PartyBranchName = PartyBranchName;
	}
	
	public String getPartyBranchID() {
		return PartyBranchID;
	}
	public void setPartyBranchID(String partyBranchID) {
		PartyBranchID = partyBranchID;
	}
	public String getPartyBranchName() {
		return PartyBranchName;
	}
	public void setPartyBranchName(String partyBranchName) {
		PartyBranchName = partyBranchName;
	}
}
