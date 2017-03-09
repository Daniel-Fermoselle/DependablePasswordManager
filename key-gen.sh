keytool -genkey -alias $1 -keyalg RSA -keystore $1.jks -keysize 2048
keytool -genkey -alias $2 -keyalg RSA -keystore $2.jks -keysize 2048
keytool -export -keystore $1.jks -alias $1 -file $1.cer
keytool -import -file $1.cer -alias $1 -keystore $2.jks
