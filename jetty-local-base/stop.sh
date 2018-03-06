#!/usr/bin/env bash

#Check if Jetty started before shut it down

status=$(curl -o /dev/null --silent --head --write-out '%{http_code}\n' http://127.0.0.1:8080/crepo/index.jsp)

if [ $status -eq 200 ]
then
	echo "Stopping Filelug Cloud Server..."

	export JETTY_HOME=/Users/masonhsieh/projects/Servers/jetty-distribution-9.3.14.v20161028
	export JETTY_BASE=/Users/masonhsieh/projects/clopuccino/codebase/clopuccino-repository/clopuccino-repository/jetty-local-base

	java -jar ${JETTY_HOME}/start.jar --stop STOP.PORT=28282 STOP.KEY=FILELUG STOP.WAIT=30
else
  echo "Filelug Cloud Server not started and don't have to stop it."

  exit 0
fi