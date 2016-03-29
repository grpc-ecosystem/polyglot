bazel build src/main/java/polyglot && echo 'recipient: "Polyglot"' | ./bazel-bin/src/main/java/polyglot/polyglot \
  --use_tls=false \
  --full_method=polyglot.HelloService/SayHello \
  --endpoint=localhost:12345 \
  --proto_root=./src/main/proto \
  --protoc_proto_path=.
