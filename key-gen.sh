#!/bin/sh

#constants
STORE_PASS="insecure"
KEY_PASS="insecure"
CA_CERTIFICATE_PASS="insecure"
D_NAME="CN=DistributedSystems,OU=DEI,O=IST,L=Lisbon,S=Lisbon,C=PT"
SUBJ="/CN=DistributedSystems/OU=DEI/O=IST/L=Lisbon/C=PT"
KEYS_VALIDITY=90

keytool -genkey -alias client -keyalg RSA -keystore keys/client1.jks -keysize 2048 -keypass $KEY_PASS -validity $KEYS_VALIDITY -storepass $STORE_PASS  -dname $D_NAME
keytool -genkey -alias client -keyalg RSA -keystore keys/client2.jks -keysize 2048 -keypass $KEY_PASS -validity $KEYS_VALIDITY -storepass $STORE_PASS  -dname $D_NAME


for server_name in $*
do
  server_folder=$server_name
  mkdir keys/$server_folder
  server_kerystore_file="keys/$server_folder/$server_name.jks"
  echo "Generating keypair of $server_name..."
  keytool -genkey -alias $server_name -keyalg RSA -keystore $server_kerystore_file -keysize 2048 -keypass $KEY_PASS -validity $KEYS_VALIDITY -storepass $STORE_PASS  -dname $D_NAME
  keytool -export -keystore $server_kerystore_file -alias $server_name -file "keys/$server_name.cer"
  keytool -import -file "keys/$server_name.cer" -alias $server_name -keystore keys/client1.jks
  keytool -import -file "keys/$server_name.cer" -alias $server_name -keystore keys/client2.jks

done

#keytool -genkey -alias $1 -keyalg RSA -keystore $1.jks -keysize 2048
#keytool -genkey -alias $2 -keyalg RSA -keystore $2.jks -keysize 2048
#keytool -export -keystore $1.jks -alias $1 -file $1.cer
#keytool -import -file $1.cer -alias $1 -keystore $2.jks

#2 client
#1 server