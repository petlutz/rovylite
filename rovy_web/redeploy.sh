#!/bin/sh
mvn compile war:war
scp target/rovy_web-0.0.1-SNAPSHOT.war pi@192.168.0.17:/var/lib/tomcat8/webapps
