# FeaVR

FeaVR is an Android VR Game, developed with Android Native and integrating a Unity project.

The game is meant to be played on a smartphone, in a VR headset, with a remote control to be able to move.

The smartphone can be connected to BLE Heart-Rate Monitor or a SmartWatch with the APP FeavrTracker.

The smartphone can be connected with a tablet that is also running FeaVR in Controller mode. The tablet will be a Control panel allowing to monitor the player.

## How to integrate Unity Project

Build your Unity project for Android using gradle builder, then copy the generated folders "assets" and "jniLibs" in "feavr_Android/src/main/".




