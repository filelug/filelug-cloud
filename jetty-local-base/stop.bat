@ECHO OFF

SET "JETTY_HOME=C:\jetty-distribution-9.3.14.v20161028"
SET "JETTY_BASE=C:\jetty-local-base"

java -jar %JETTY_HOME%/start.jar --stop STOP.PORT=28282 STOP.KEY=FILELUG
