for cmd in "$@"
do

if [ "$cmd" == "install" ]; then
	cd ../crypto
	mvn install
	cd ../common_classes
	mvn install
	cd ../PwServer
	mvn install
elif [ "$cmd" == "clean" ]; then
	cd ../crypto
	mvn clean
	cd ../common_classes
	mvn clean
	cd ../PwServer
	mvn clean
elif [ "$cmd" == "compile" ]; then
	cd ../crypto
	mvn compile
	cd ../common_classes
	mvn compile
	cd ../PwServer
	mvn compile
elif [ "$cmd" == "install-no-tests" ]; then
	cd ../crypto
	mvn install
	cd ../common_classes
	mvn install
	cd ../PwServer 
	mvn install -DskipTests
else
	open run.command
fi

done