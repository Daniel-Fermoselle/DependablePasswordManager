for cmd in $*
do

if [ $cmd == "install" ]; then
	cd ../crypto
	mvn install
	cd ../common_classes
	mvn install
	cd ../client_lib
	mvn install
	cd ../client
	mvn install
elif [ $cmd == "clean" ]; then
	cd ../crypto
	mvn clean
	cd ../common_classes
	mvn clean
	cd ../client_lib
	mvn clean
	cd ../client
	mvn clean
else
	mvn exec:java
fi

done