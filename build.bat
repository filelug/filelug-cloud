@ECHO OFF

SET MAVEN_OPTS="-Xmx512m"

REM
REM build without obfuscated
REM
mvn -e clean compile & ant replace.before.package & mvn -e war:exploded prepare-package & ant war & xcopy /f /y target\crepo.war jetty-local-base\webapps

REM
REM build with obfuscated
REM
REM mvn -e clean compile & ant replace.before.package & mvn -e war:exploded prepare-package & ant replace.obfuscated.classes war & xcopy /f /y target\crepo.war jetty-local-base\webapps
