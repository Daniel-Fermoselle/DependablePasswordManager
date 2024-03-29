echo "Full path to server:"
read text1
echo "Full path to client"
read text2
echo "Path to keystore - ks/client1.jks (for example)"
read text3
echo "Rank of this client - 100 (for example)"
read text4
server2="cd $text1\n mvn exec:java -Dexec.args=\\\"localhost:6666\\\""
server3="cd $text1\n mvn exec:java -Dexec.args=\\\"localhost:7777\\\""
server4="cd $text1\n mvn exec:java -Dexec.args=\\\"localhost:5444\\\""
client="cd $text2\n sleep 25\n ./client.sh install-no-tests \\\"metadata.in $text3 $text4\\\""
run2="tell application \"Terminal\" to do script \"$server2\""
run3="tell application \"Terminal\" to do script \"$server3\""
run4="tell application \"Terminal\" to do script \"$server4\""
run5="tell application \"Terminal\" to do script \"$client\""
cd $text1
osascript -e "$run2"
osascript -e "$run3"
osascript -e "$run4"
osascript -e "$run5"
mvn exec:java -Dexec.args="localhost:5555"