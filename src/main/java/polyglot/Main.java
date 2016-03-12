package polyglot;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.grpc.ServerBuilder;
import polyglot.HelloProto.Hello;

public class Main {
  private static final Logger logger = LoggerFactory.getLogger(Main.class);
  private static final int SERVER_PORT = 12345;
  private static final Hello GREETING = Hello.newBuilder()
      .setMessage("Hello, polyglot")
      .build();

  public static void main(String[] args) throws IOException, InterruptedException {
    logger.info("Greeting: " + GREETING);

    logger.info("Starting grpc server on port: " + SERVER_PORT);
    ServerBuilder.forPort(SERVER_PORT)
        .addService(HelloServiceGrpc.bindService(new HelloServiceImpl()))
        .build()
        .start()
        .awaitTermination();
  }
}
