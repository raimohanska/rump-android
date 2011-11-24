package com.rump;

import android.location.Location;

public class GeoLocation {
	public final double latitude;
	public final double longitude;

	public GeoLocation(Location loc) {
		this(loc.getLatitude(), loc.getLongitude());
	}

	public GeoLocation(double latitude, double longitude) {
		super();
		this.latitude = latitude;
		this.longitude = longitude;
	}

	@Override
	public String toString() {
		return "LAT=" + latitude + " LONG=" + longitude;
	}
}
