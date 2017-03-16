for path in $*
do

cd ../crypto
mvn install
cd ../common_classes
mvn install
cd ../PwServer
mvn clean install war:war
cp target/PwServer.war $path
catalina start

done