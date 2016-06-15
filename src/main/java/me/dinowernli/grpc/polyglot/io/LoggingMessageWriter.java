package me.dinowernli.grpc.polyglot.io;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.DynamicMessage;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;

import io.grpc.stub.StreamObserver;

/**
 * A {@link StreamObserver} which outputs the contents of the received response messages to the
 * standard logs.
 */
public class LoggingMessageWriter implements StreamObserver<DynamicMessage> {
  private static final Logger logger = LoggerFactory.getLogger(LoggingMessageWriter.class);

  private final JsonFormat.Printer jsonPrinter;

  public static LoggingMessageWriter create() {
    return new LoggingMessageWriter(JsonFormat.printer());
  }

  private LoggingMessageWriter(JsonFormat.Printer jsonPrinter) {
    this.jsonPrinter = jsonPrinter;
  }

  @Override
  public void onCompleted() {
    // Ignore.
  }

  @Override
  public void onError(Throwable t) {
    // Ignore.
  }

  @Override
  public void onNext(DynamicMessage message) {
    try {
      logger.info("Message: \n" + jsonPrinter.print(message) + "\n");
    } catch (InvalidProtocolBufferException e) {
      logger.error("Skipping invalid response message", e);
    }
  }
}
