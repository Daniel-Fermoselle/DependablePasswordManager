echo "Path to server:"
read text1
echo "Path to client"
read text2
server2="cd $text1\n sleep 25\n ./server.sh \\\"localhost:6666\\\""
server3="cd $text1\n sleep 25\n ./server.sh \\\"localhost:7777\\\""
server4="cd $text1\n sleep 25\n ./server.sh \\\"localhost:5444\\\""
client="cd $text2\n sleep 45\n ./client.sh install-no-tests \\\"metadata.in\\\""
run2="tell application \"Terminal\" to do script \"$server2\""
run3="tell application \"Terminal\" to do script \"$server3\""
run4="tell application \"Terminal\" to do script \"$server4\""
run5="tell application \"Terminal\" to do script \"$client\""
cd $text1
osascript -e "$run2"
osascript -e "$run3"
osascript -e "$run4"
osascript -e "$run5"
./server.sh install "localhost:5555"