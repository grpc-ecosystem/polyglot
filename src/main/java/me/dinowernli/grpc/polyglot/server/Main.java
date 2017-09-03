package me.dinowernli.grpc.polyglot.server;

import java.io.IOException;

import io.grpc.ServerBuilder;
import io.grpc.protobuf.services.ProtoReflectionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A binary which starts a simple gRPC server. This is used to test the client code.
 */
public class Main {
  private static final Logger logger = LoggerFactory.getLogger(Main.class);
  private static final int SERVER_PORT = 12345;

  public static void main(String[] args) {
    logger.info("Starting grpc server on port: " + SERVER_PORT);
    try {
      ServerBuilder.forPort(SERVER_PORT)
          .addService(new HelloServiceImpl())
          .addService(ProtoReflectionService.newInstance())
          .build()
          .start()
          .awaitTermination();
    } catch (InterruptedException | IOException e) {
      logger.info("Caught exception, shutting down", e);
      System.exit(0);
    }
  }
}
