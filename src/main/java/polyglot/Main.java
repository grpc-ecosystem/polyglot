package polyglot;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.grpc.ServerBuilder;

public class Main {
  private static final Logger logger = LoggerFactory.getLogger(Main.class);
  private static final int SERVER_PORT = 12345;

  public static void main(String[] args) throws IOException {
    logger.info("Hello polyglot");
    ServerBuilder.forPort(SERVER_PORT)
        .build()
        .start();
  }
}
