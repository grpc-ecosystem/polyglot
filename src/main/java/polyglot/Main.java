package polyglot;

import polyglot.HelloProto.Hello;
import polyglot.HelloServiceGrpc;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.grpc.ServerBuilder;

public class Main {
  private static final Logger logger = LoggerFactory.getLogger(Main.class);
  private static final int SERVER_PORT = 12345;
  private static final Hello GREETING = Hello.newBuilder()
      .setMessage("Hello, polyglot")
      .build();

  public static void main(String[] args) throws IOException {
    logger.info("Greeting: " + GREETING);
    ServerBuilder.forPort(SERVER_PORT)
        .addService(HelloServiceGrpc.bindService(null))  // TODO(dino); Add proper implementation.
        .build()
        .start();
  }
}
