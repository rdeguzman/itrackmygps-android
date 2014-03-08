itrackmygps-android
=========

![Splash Screen](screenshots/overview.jpg)

For more info, see <http://itrackmygps.com>

Want to become a tester?
[Google Plus Community "itrackmygps - testers"](https://plus.google.com/communities/100298062955164933715)

# Features

- Logs gps of your Android Device
- Saves gps coordinates to sqlite database on internal storage
- Sends gps coordinates to remote server


# Prerequisites
- IntelliJ Idea CE 133.124
- Android Platform 15
- Genymotion 1.3.1

# Instructions

- Clone the project `git clone git@github.com:rdeguzman/trackble-android.git`
- Get a google maps API Key.
- Copy `api_key.xml` to `res/values`

	```
	<?xml version="1.0" encoding="utf-8"?>
	<resources>
    	<string name="google_maps_key">Your-GoogleMaps-API-Key</string>
	</resources>	
	```
- Build	and Deploy