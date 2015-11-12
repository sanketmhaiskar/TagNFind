package com.vexit.tagnfind.objects;

public class TagResults {
	String TagName = null, Location = null, Latitude = null, Longitude = null,
			Address = null;

	public String getLocation() {
		return Location;
	}

	public void setLocation(String location) {
		this.Location = location;
	}

	public String getLatitude() {
		return Latitude;
	}

	public void setLatitude(String latitude) {
		this.Latitude = latitude;
	}

	public String getLongitude() {
		return Longitude;
	}

	public void setLongitude(String longitude) {
		this.Longitude = longitude;
	}

	public String getAddress() {
		return Address;
	}

	public void setAddress(String address) {
		this.Address = address;
	}

	public String getTagName() {
		return TagName;
	}

	public void setTagName(String tagName) {
		this.TagName = tagName;
	}
}
