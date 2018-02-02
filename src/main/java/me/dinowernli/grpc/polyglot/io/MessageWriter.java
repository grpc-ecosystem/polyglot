package me.dinowernli.grpc.polyglot.io;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import com.google.protobuf.util.JsonFormat.TypeRegistry;
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
  private static final String MESSAGE_SEPARATOR = "\n\n";

  private final JsonFormat.Printer jsonPrinter;
  private final Output output;

  /**
   * Creates a new {@link MessageWriter} which writes the messages it sees to the supplied
   * {@link Output}.
   */
  public static <T extends Message> MessageWriter<T> create(Output output, TypeRegistry registry) {
    return new MessageWriter<>(JsonFormat.printer().usingTypeRegistry(registry), output);
  }

  /**
   * Returns the string representation of the stream of supplied messages. Each individual message
   * is represented as valid json, but not that the whole result is, itself, *not* valid json.
   */
  public static <M extends Message> String writeJsonStream(ImmutableList<M> messages) {
    return writeJsonStream(messages, TypeRegistry.getEmptyTypeRegistry());
  }

  /**
   * Returns the string representation of the stream of supplied messages. Each individual message
   * is represented as valid json, but not that the whole result is, itself, *not* valid json.
   */
  public static <M extends Message> String writeJsonStream(
      ImmutableList<M> messages, TypeRegistry registry) {
    ByteArrayOutputStream resultStream = new ByteArrayOutputStream();
    MessageWriter<M> writer =
        MessageWriter.create(Output.forStream(new PrintStream(resultStream)), registry);
    writer.writeAll(messages);
    return resultStream.toString();
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
  public void onNext(T message) {
    try {
      output.write(jsonPrinter.print(message) + MESSAGE_SEPARATOR);
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
