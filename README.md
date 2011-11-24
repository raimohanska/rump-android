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

  private final RumpShaker rumpShaker = new RumpShaker(this, new RumpCallback() {
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

  @Override
  protected void onResume() {
	rumpShaker.start(this);
  }

  protected void onPause() {
  	sumpShaker.stop();
  }
~~~