package com.rump;

import static com.google.common.collect.Sets.newHashSet;
import static java.lang.Math.sqrt;

import java.util.List;
import java.util.Set;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;

import com.google.common.base.Joiner;

public final class RumpShaker {
	private final Context context;
	private final RumpClient client;
	private final Vibrator vibrator;
	private final RumpCallback callback;
	private long previousRequest = 0;
	private GeoLocation location = new GeoLocation(0, 0);
	private final SensorEventListener sensorListener = new ShakeEventListener();
	private final LocationListener locationListener = new UserLocationListener();

	public RumpShaker(Context context, RumpCallback callback) {
		super();
		this.context = context;
		this.client = new RumpClient();
		this.callback = callback;
		this.vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
	}

	private void onShake() {
		long now = System.currentTimeMillis();
		if (now - previousRequest < 3000) {
			return;
		}
		previousRequest = now;
		findCompany();
	}

	private void findCompany() {
		vibrator.vibrate(100);
		final Toast progress = Toast.makeText(context, "Looking for company..", Toast.LENGTH_LONG);
		progress.show();
		final RumpInfo myInfo = new RumpInfo(callback.getUsername(), callback.getDisplayName(), location);
		client.rump(myInfo, new RumpResultHandler() {
			public void onResponse(List<RumpInfo> dudes) {
				progress.cancel();
				gotCompany(dudes, myInfo);
			}
		});
	}

	private void gotCompany(List<RumpInfo> dudes, RumpInfo me) {
		Set<RumpInfo> uniqueUsers = newHashSet(dudes);
		uniqueUsers.remove(me);
		if (uniqueUsers.isEmpty()) {
			Toast.makeText(context, "No match", Toast.LENGTH_SHORT).show();
		} else {
			int count = uniqueUsers.size();
			longVibrations(vibrator, count);
			Toast.makeText(context, "Found: " + Joiner.on(",").join(uniqueUsers), Toast.LENGTH_SHORT).show();
			uniqueUsers.add(me);
			callback.connectedWith(uniqueUsers);
		}
	}

	private static void longVibrations(final Vibrator v, int count) {
		long[] vibrations = new long[count * 2];
		// First element is 0 => no delay
		// Other elements are 300 => vibrate on/off every
		// 300 ms to indicate number of friends
		for (int i = 1; i < vibrations.length; i++) {
			vibrations[i] = 300l;
		}
		v.vibrate(vibrations, -1);
	}

	private class UserLocationListener implements LocationListener {
		public void onLocationChanged(Location androidLocation) {
			location = new GeoLocation(androidLocation);
			Log.i("RUMP Client", " got location " + location);
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
		}

		public void onProviderEnabled(String provider) {
		}

		public void onProviderDisabled(String provider) {
		}
	}

	private class ShakeEventListener implements SensorEventListener {
		private float[] prev;

		public void onSensorChanged(SensorEvent se) {
			float x = se.values[SensorManager.DATA_X];
			float y = se.values[SensorManager.DATA_Y];
			float z = se.values[SensorManager.DATA_Z];

			if (prev != null) {
				double diff = sqrt(sqr(x - prev[0]) + sqr(y - prev[1]) + sqr(z - prev[2]));
				if (diff > 20) {
					onShake();
				}
			}
			prev = new float[] { x, y, z };
		}

		public void onAccuracyChanged(Sensor sensor, int accuracy) {
		}

		private float sqr(float a) {
			return a * a;
		}
	}

	public SensorEventListener asSensorListener() {
		return sensorListener;
	}

	public LocationListener asLocationListener() {
		return locationListener;
	}
}