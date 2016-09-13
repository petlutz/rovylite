#!/bin/sh
cd rasp_rover
mvn package
mvn install
scp target/rovy_api-0.0.1-SNAPSHOT.jar pi@192.168.0.17:/home/pi/rovy/server/
