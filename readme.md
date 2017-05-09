# Description

This library fetch utc time from ntp one time and cache on local along with elapse time of device.
Auto retry/reconnect to server when network connected to get correct utc.

# Setup
![Preview](app/src/main/res/mipmap-hdpi/ic_launcher.png)
[![](https://jitpack.io/v/nhancv/nc-android-nutc.svg)](https://jitpack.io/#nhancv/nc-android-nutc)

Add it in your root build.gradle at the end of repositories:

	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}

Add the dependency

	dependencies {
	        compile 'com.github.nhancv:nc-android-nutc:${version}'
	}

# Usage

Initialize one time

```java
NUtc.build(getApplicationContext());
```

And get time

```java
NUtc.getUtcNow()
```

