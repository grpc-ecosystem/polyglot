package me.dinowernli.grpc.polyglot.io;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;

import io.grpc.stub.StreamObserver;

/**
 * A {@link StreamObserver} which writes the contents of the received messages to an
 * {@link Output}. The messages are writting in a newline-separated json format.
 */
public class MessageWriter<T extends Message> implements StreamObserver<T> {
  private static final Logger logger = LoggerFactory.getLogger(MessageWriter.class);

  /** Used to separate the individual plaintext json proto messages. */
  private final String messageSeparator;

  private final JsonFormat.Printer jsonPrinter;
  private final Output output;
  private boolean firstMessagePrinted = false;

  /**
   * Creates a new {@link MessageWriter} which writes the messages it sees to the supplied
   * {@link Output}.
   */
  public static <T extends Message> MessageWriter<T> create(Output output, String messageSeparator) {
    return new MessageWriter<T>(JsonFormat.printer(), output, messageSeparator);
  }

  /**
   * Returns the string representation of the stream of supplied messages. Each individual message
   * is represented as valid json, but not that the whole result is, itself, *not* valid json.
   */
  public static <M extends Message> String writeJsonStream(ImmutableList<M> messages) {
    return writeJsonStream(messages, "\n\n");
  }

  /**
   * Returns the string representation of the list of messages, separated by the {@param messageSeparator} string
   */
  public static <M extends Message> String writeJsonStream(ImmutableList<M> messages, String messageSeparator) {
    ByteArrayOutputStream resultStream = new ByteArrayOutputStream();
    MessageWriter<M> writer = MessageWriter.create(Output.forStream(new PrintStream(resultStream)), messageSeparator);
    writer.writeAll(messages);
    return resultStream.toString();
  }

  @VisibleForTesting
  MessageWriter(JsonFormat.Printer jsonPrinter, Output output, String messageSeparator) {
    this.jsonPrinter = jsonPrinter;
    this.output = output;
    this.messageSeparator = messageSeparator;
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
  public void onNext(T message) {
    try {
      String outputString = jsonPrinter.print(message);
      // Only separate messages if there are more than one
      if (!firstMessagePrinted) {
        firstMessagePrinted = true;
      } else {
        outputString = messageSeparator + outputString;
      }
      output.write(outputString);
    } catch (InvalidProtocolBufferException e) {
      logger.error("Skipping invalid response message", e);
    }
  }

  /** Writes all the supplied messages and closes the stream. */
  public void writeAll(ImmutableList<? extends T> messages) {
    messages.forEach(this::onNext);
    onCompleted();
  }
}
