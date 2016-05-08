The required files for the tests are:
* ca.pem
* server.pem
* server.key

Generating these files requires only:
* openssl.cnf

In order to generate the three files above from "openssl.cnf", do the following:

1) openssl req -x509 -new -newkey rsa:1024 -nodes -out ca.pem -config openssl.cnf -days 3650 -extensions v3_req

all default, except common name: "localhost"

2) openssl genrsa -out server.key.rsa 1024
3) openssl pkcs8 -topk8 -in server.key.rsa -out server.key -nocrypt
4) openssl req -new -key server.key -out server.csr

all default, except common name: "localhost"

5) mv privkey.pem ca.key
6) touch index.txt
7) echo "01" > serial
8) openssl ca -in server.csr -out server.pem -keyfile ca.key -cert ca.pem -verbose -config openssl.cnf -days 3650 -updatedb
