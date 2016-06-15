bazel build src/main/java/me/dinowernli/grpc/polyglot && echo '{"recipient": "Polyglot"}' | ./bazel-bin/src/main/java/me/dinowernli/grpc/polyglot/polyglot \
  --full_method=polyglot.HelloService/SayHello \
  --endpoint=localhost:12345 \
  --proto_discovery_root=./src/main/proto \
  --add_protoc_includes=. \
  --config_set_path=config.pb.json \
  --deadline_ms=3000
