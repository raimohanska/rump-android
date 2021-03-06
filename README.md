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

Place the generated rump-android.jar on your project classpath. Then

~~~ .java

  private final RumpShaker rumpShaker = new RumpShaker("http://rump.demo.reaktor.fi/demo", new RumpCallback() {
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

Your responsibility then is to provide RUMP with user id and display name. 
You implement the connectedWith method to do whatever you wish with the information on the users that were found.
You should probably alter the server URL to be unique to your application to avoid clashing with other apps using
the same RUMP server. You are welcome to use http://rump.demo.reaktor.fi/whateveryourappnameis though, at least for the time being.

When used as above, Rump client will indicate progress and results with [Toast Notifications](http://developer.android.com/guide/topics/ui/notifiers/toasts.html).
You can also provide your own UI callback to get full control on the messages shown in the UI.

Using in Maven projects
=======================

~~~
        <dependency>
            <groupId>raimohanska</groupId>
            <artifactId>rump-android</artifactId>
            <version>1.0.1</version>
        </dependency>
~~~

and

~~~
    <repositories>
	    <repository>
	        <id>raimohanska-releases</id>
	        <url>https://github.com/raimohanska/raimohanska-mvn-repository/raw/master/releases</url>
	    </repository>
	</repositories>
~~~

Building
========

A simple Maven `pom.xml` is included for building. I deploy this jar to my own repository using `mvn deploy`.