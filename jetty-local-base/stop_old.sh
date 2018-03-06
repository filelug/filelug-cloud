#!/usr/bin/env bash
export JETTY_HOME=/Users/masonhsieh/projects/Servers/jetty-distribution-9.3.14.v20161028
export JETTY_BASE=/Users/masonhsieh/projects/clopuccino/codebase/clopuccino-repository/clopuccino-repository/jetty-local-base

java -jar ${JETTY_HOME}/start.jar --stop STOP.PORT=28282 STOP.KEY=FILELUG
