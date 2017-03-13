for path in $*
do

mvn clean install war:war
cp target/PwServer.war $path
catalina start

done