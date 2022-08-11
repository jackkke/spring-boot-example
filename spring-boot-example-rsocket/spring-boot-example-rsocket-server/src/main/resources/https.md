keytool -genkey -alias rsocket -keypass rsocketKey -keyalg RSA -keysize 1024 -validity 365 -keystore rsocket.keystore -storepass rsocketStore

keytool -genkey -alias rsocket -keypass rsocketKey -keyalg RSA -storetype PKCS12 -storepass rsocketStore -keystore client.p12



keytool -keystore rsocket.keystore -export -alias rsocket -file server.cer



keytool -genkey -alias rsocket -dname "CN=esafenet,OU=esafenet,O=esafenet,L=Beijing,ST=Beijing,C=CN" -storetype PKCS12 -keyalg RSA -keysize 2048 -keystore rsocket.p12 -validity 365  -storepass rsocketStore


keytool -keystore rsocket.p12 -export -alias rsocket -file server.cer

keytool -importcert -alias rsocket -file server.cer -storepass rsocketStore -noprompt

keytool -list -v -alias rsocket -storepass rsocketStore