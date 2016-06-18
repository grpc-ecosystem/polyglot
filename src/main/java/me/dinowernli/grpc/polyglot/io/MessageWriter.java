package me.dinowernli.grpc.polyglot.io;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;

import io.grpc.stub.StreamObserver;

/**
 * A {@link StreamObserver} which writes the contents of the received response messages to an
 * {@link OutputImpl}.
 */
public class MessageWriter implements StreamObserver<DynamicMessage> {
  private static final Logger logger = LoggerFactory.getLogger(MessageWriter.class);

  /** Used to separate the individual plaintext json proto messages. */
  private static final String MESSAGE_SEPARATOR = "\n\n";

  private final JsonFormat.Printer jsonPrinter;
  private final Output output;

  public static MessageWriter create(Output output) {
    return new MessageWriter(JsonFormat.printer(), output);
  }

  @VisibleForTesting
  MessageWriter(JsonFormat.Printer jsonPrinter, Output output) {
    this.jsonPrinter = jsonPrinter;
    this.output = output;
  }

  @Override
  public void onCompleted() {
    // Nothing to do.
  }

  @Override
  public void onError(Throwable t) {
    // Nothing to do.
  }

  @Override
  public void onNext(DynamicMessage message) {
    try {
      output.write(jsonPrinter.print(message) + MESSAGE_SEPARATOR);
    } catch (InvalidProtocolBufferException e) {
      logger.error("Skipping invalid response message", e);
    }
  }
}
