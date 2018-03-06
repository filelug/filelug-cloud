#!/bin/sh

export MAVEN_OPTS="-Xmx512m"

#
# build without obfuscated
#
mvn -e clean compile && \
ant replace.before.package && \
mvn -e war:exploded prepare-package && \
ant war && \
cp -v target/crepo.war jetty-local-base/webapps

#
# build with obfuscated
#
#mvn -e clean compile && \
#ant replace.before.package && \
#mvn -e war:exploded prepare-package && \
#ant replace.obfuscated.classes war && \
#cp -v target/crepo.war jetty-local-base/webapps
