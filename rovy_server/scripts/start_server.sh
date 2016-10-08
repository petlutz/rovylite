
#!/bin/sh

#init audio
# select analog out
amixer cset numid=3 1
# volume
amixer cset numid=1 -- 100%

# load raspberry camera v4l driver (needed for opencv) 
modprobe bcm2835-v4l2

export CLASSPATH=pi4j-core.jar:pi4j-core-javadoc.jar:pi4j-core-sources.jar:pi4j-device.jar:pi4j-device-javadoc.jar:pi4j-device-sources.jar:pi4j-example.jar:pi4j-example-javadoc.jar:pi4j-example-sources.jar:pi4j-gpio-extension.jar:pi4j-gpio-extension-javadoc.jar:pi4j-gpio-extension-sources.jar:pi4j-service.jar:pi4j-service-javadoc.jar:pi4j-service-sources.jar:pi4j-1.1-SNAPSHOT/lib/:rovy_opencvwrapper-0.0.1-SNAPSHOT.jar:rovy_api-0.0.1-SNAPSHOT.jar:rovy_server-0.0.1-SNAPSHOT.jar

killall rmiregistry
rmiregistry 1234 &
java -Djava.library.path=. -Djava.security.policy=./rovysecurity.policy -classpath $CLASSPATH de.gnox.rovy.server.Launcher
killall rmiregistry 
