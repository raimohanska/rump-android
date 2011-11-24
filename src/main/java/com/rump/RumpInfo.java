package com.rump;

public class RumpInfo {
	/**
	 * Username for identifying user
	 */
	public final String userId;
	/**
	 * Name for display purposes
	 */
	public final String displayName;

	public final GeoLocation location;

	public RumpInfo(String userId, String displayName, GeoLocation location) {
		super();
		this.userId = userId;
		this.displayName = displayName;
		this.location = location;
	}

	@Override
	public String toString() {
		return displayName;
	}

	@Override
	public boolean equals(Object o) {
		return ((RumpInfo) o).userId.equals(userId);
	}

	@Override
	public int hashCode() {
		return userId.hashCode();
	}
}
