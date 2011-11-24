package com.rump;

import java.util.Set;

public interface RumpCallback {
	/**
	 * Username for identifying user
	 */
	String getUsername();

	/**
	 * Name for display purposes
	 */
	String getDisplayName();

	/**
	 * Callback method, called when a connection has been made.
	 * 
	 * @param uniqueUsers
	 *            Includes "self"
	 */
	void connectedWith(Set<RumpInfo> uniqueUsers);
}
