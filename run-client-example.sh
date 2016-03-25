echo 'recipient: "Polyglot"' | ./run-client.sh --proto_class=polyglot.HelloProto --full_method=polyglot.HelloService/SayHello --endpoint=localhost:12345 --proto_root=./src/main/proto
