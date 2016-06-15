package me.dinowernli.grpc.polyglot.io;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;

import io.grpc.stub.StreamObserver;

/**
 * A {@link StreamObserver} which outputs the contents of the received response messages to a file.
 */
public class FileMessageWriter implements StreamObserver<DynamicMessage> {
  private static final Logger logger = LoggerFactory.getLogger(FileMessageWriter.class);

  /** Used to separate the individual plaintext json proto messages. */
  private static final String MESSAGE_SEPARATOR = "\n\n";

  private final JsonFormat.Printer jsonPrinter;
  private final PrintWriter printWriter;

  /** Returns a {@link FileMessageWriter} which writes the observed messages to a file. */
  public static FileMessageWriter forFile(Path path) {
    PrintWriter printWriter;
    try {
      printWriter = new PrintWriter(path.toFile());
    } catch (FileNotFoundException e) {
      throw new RuntimeException("Unable to create writer for file: " + path.toString(), e);
    }

    return new FileMessageWriter(JsonFormat.printer(), printWriter);
  }

  @VisibleForTesting
  FileMessageWriter(JsonFormat.Printer jsonPrinter, PrintWriter printWriter) {
    this.jsonPrinter = jsonPrinter;
    this.printWriter = printWriter;
  }

  @Override
  public void onCompleted() {
    finalizeOutput();
  }

  @Override
  public void onError(Throwable t) {
    finalizeOutput();
  }

  @Override
  public void onNext(DynamicMessage message) {
    try {
      printWriter.write(jsonPrinter.print(message) + MESSAGE_SEPARATOR);
    } catch (InvalidProtocolBufferException e) {
      logger.error("Skipping invalid response message", e);
    }
  }

  private void finalizeOutput() {
    printWriter.close();
  }
}
