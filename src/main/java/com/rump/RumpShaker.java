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
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;

import com.google.common.base.Joiner;

public final class RumpShaker {
	private Context context;
	private final RumpClient client;
	private Vibrator vibrator;
	private final RumpCallback callback;
	private final RumpUICallback uiCallback;
	private long previousRequest = 0;
	private GeoLocation location = new GeoLocation(0, 0);
	private SensorManager sensorManager;
	private LocationManager locationManager;
	private final SensorEventListener sensorListener = new ShakeEventListener();
	private final LocationListener locationListener = new UserLocationListener();

	public RumpShaker(String serverUrl, RumpCallback callback) {
		this(serverUrl, callback, new DefaultUICallback());
	}

	public RumpShaker(String serverUrl, RumpCallback callback, RumpUICallback uiCallback) {
		this.client = new RumpClient(serverUrl);
		this.callback = callback;
		this.uiCallback = uiCallback;
	}

	public void start(Context context) {
		this.sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
		this.locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		this.context = context;
		this.vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
		sensorManager.registerListener(sensorListener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_UI);
		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
	}

	public void stop() {
		locationManager.removeUpdates(locationListener);
		sensorManager.unregisterListener(sensorListener);
		client.discardBackgroundTasks();
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
		uiCallback.onRumpStart(context);
		final RumpInfo myInfo = new RumpInfo(callback.getUsername(), callback.getDisplayName(), location);
		client.rump(myInfo, new RumpResultHandler() {
			public void onResponse(List<RumpInfo> dudes) {
				uiCallback.onRumpEnd(context);
				gotCompany(dudes, myInfo);
			}
		});
	}

	private void gotCompany(List<RumpInfo> dudes, RumpInfo me) {
		Set<RumpInfo> uniqueUsers = newHashSet(dudes);
		uniqueUsers.remove(me);
		if (uniqueUsers.isEmpty()) {
			uiCallback.onNoMatch(context);
		} else {
			int count = uniqueUsers.size();
			longVibrations(vibrator, count);
			uiCallback.onConnect(context, uniqueUsers);
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
}

class DefaultUICallback implements RumpUICallback {
	private Toast progressToast;

	public void onRumpStart(Context context) {
		progressToast = Toast.makeText(context, "Looking for company..", Toast.LENGTH_LONG);
		progressToast.show();
	}

	public void onRumpEnd(Context context) {
		progressToast.cancel();
	}

	public void onNoMatch(Context context) {
		Toast.makeText(context, "No match", Toast.LENGTH_SHORT).show();
	}

	public void onConnect(Context context, Set<RumpInfo> uniqueUsers) {
		Toast.makeText(context, "Found: " + Joiner.on(",").join(uniqueUsers), Toast.LENGTH_SHORT).show();
	}
}