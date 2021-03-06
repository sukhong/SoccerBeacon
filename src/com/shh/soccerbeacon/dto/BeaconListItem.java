package com.shh.soccerbeacon.dto;

public class BeaconListItem implements Comparable<BeaconListItem>
{
	private String beaconName;
	private int major;
	private int minor;
	private int RSSI;
	
	public BeaconListItem(String beaconId, int major, int minor, int RSSI)
	{
		this.beaconName = beaconId;
		this.major = major;
		this.minor = minor;
		this.RSSI = RSSI;
	}

	public String getBeaconName() {
		return beaconName;
	}

	public void setBeaconName(String beaconName) {
		this.beaconName = beaconName;
	}
	
	public int getRSSI() {
		return RSSI;
	}
	
	public void setRSSI(int RSSI) {
		this.RSSI = RSSI;
	}
	
	public int getMajor() {
		return major;
	}

	public void setMajor(int major) {
		this.major = major;
	}

	public int getMinor() {
		return minor;
	}

	public void setMinor(int minor) {
		this.minor = minor;
	}

	@Override
	public int compareTo(BeaconListItem target) {
		// sort by descending RSSI order
		if (this.RSSI < target.RSSI)
			return 1;
		else if (this.RSSI == target.RSSI)
			return 0;
		else
			return -1;
	}
}