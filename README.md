RUMP client for Android
=======================

Client library for [RUMP](https://github.com/raimohanska/rump) on Android.

Building
========

- Create a symlink to your local android.jar as in

~~~ .bash
  ln -s /usr/local/Cellar/android-sdk/r12/platforms/android-8/android.jar lib/android.jar
~~~

- sbt package

A jar will come.

Using in your Android project
=============================

~~~ .java

  RumpShaker rumpShaker;	

  @Override
  public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		rumpShaker = new RumpShaker(this, new RumpCallback() {
    	public void connectedWith(Set<RumpInfo> dudes) {
				// TODO: handle incoming connection
			}

			public String getDisplayName() {
				// TODO: return display name
			}

			public String getUsername() {
				// TODO: return user id
			}
		});	
		sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
	}
  
  @Override
	protected void onResume() {
		super.onResume();
  	sensorManager.registerListener(rumpShaker.asSensorListener(), sensorManager.getDefaultSensor(TYPE_ACCELEROMETER), SENSOR_DELAY_UI);
		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, rumpShaker.asLocationListener());
	}

	protected void onPause() {
		super.onPause();
  	locationManager.removeUpdates(rumpShaker.asLocationListener());
		sensorManager.unregisterListener(rumpShaker.asSensorListener());		
	}
~~~