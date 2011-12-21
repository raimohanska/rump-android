package com.rump;

import java.util.Set;

import android.content.Context;

public interface RumpUICallback {
	/**
	 * Callback method, called when shake request is sent to server
	 */
	void onRumpStart(Context context);
	
	/**
	 * Callback method, called when shake request returns from server
	 */
	void onRumpEnd(Context context);
	
	/**
	 * Callback method, called when no matching users are found
	 */
	void onNoMatch(Context context);

	/**
	 * Callback method, called when a connection has been made.
	 *
	 * @param context
	 * @param uniqueUsers
	 *            Excludes "self"
	 */
	void onConnect(Context context, Set<RumpInfo> uniqueUsers);
}
