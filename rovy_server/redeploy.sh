#!/bin/sh
mvn package
scp target/rovy_server-0.0.1-SNAPSHOT.jar pi@192.168.0.17:/home/pi/rovy/server
