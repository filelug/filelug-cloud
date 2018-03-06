#!/bin/sh
mvn clean compile package
zip -r -X target/classes.zip target/classes
scp -i ~/repository.pem target/classes.zip ubuntu@filelug.com:appserver-repository/webapps/crepo/WEB-INF
