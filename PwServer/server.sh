for cmd in $*
do

if [ $cmd == "install" ]; then
	cd ../crypto
	mvn install
	cd ../common_classes
	mvn install
	cd ../PwServer
	mvn install
elif [ $cmd == "clean" ]; then
	cd ../crypto
	mvn clean
	cd ../common_classes
	mvn clean
	cd ../PwServer
	mvn clean
else
	mvn exec:java -Dexec.args="$cmd"
fi

done