TSL Communication
==================

# Configuration

* Create file server
```
keytool -genkey -keypass password \
  -storepass password \
  -keystore server.jks
```
* Create file client
```
keytool -genkey -keypass password \
  -storepass password \
  -keystore server.jks
```

* Export server file as a file which need to be import as trusted file for client

```
keytool -export -storepass password \
  -file server.cer \
  -keystore server.jks
```
* Import file server and create trusted client file
```
keytool -import -v -trustcacerts \
  -file server.cer \
  -keypass password \
  -storepass password \
  -keystore trusted.jks
```
