# TVManifest

Android app to grab a list of DVR recordings from a [TVHeadEnd](https://github.com/tvheadend/tvheadend) server. Created because I couldn't find a good TVHeadEnd Android client, Jellyfin wouldn't work, and I got tired of manually pasting links into mpv. 

Implements the following features:
  - Swipe to refresh
  - Opening DVR links (using M3U streams) in the [MPV](https://github.com/mpv-android/mpv-android) player
  - Deleting recordings
  - Changing authentication/server address

Known bugs/limitations
  - May break/crash if TVHeadEnd server is not on the same network/not available
  - Current implementation assumes HTTP (cleartext) connections and basic HTTP auth - this is NOT secure. Don't use this with anything other than a secure home network and local addresses. 

Future features?
  - Add support for opening live feeds/muxes
