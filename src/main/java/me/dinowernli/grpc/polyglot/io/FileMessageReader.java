package me.dinowernli.grpc.polyglot.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.util.JsonFormat;

/** A utility class which knows how to read proto files written using {@link FileMessageWriter}. */
public class FileMessageReader {
  private final JsonFormat.Parser jsonParser;
  private final Descriptor descriptor;
  private final BufferedReader bufferedReader;

  public static FileMessageReader create(Path path, Descriptor descriptor) {
    try {
      return new FileMessageReader(JsonFormat.parser(), descriptor, Files.newBufferedReader(path));
    } catch (IOException e) {
      throw new IllegalArgumentException("Unable to read file: " + path.toString(), e);
    }
  }

  @VisibleForTesting
  FileMessageReader(
      JsonFormat.Parser jsonParser,
      Descriptor descriptor,
      BufferedReader bufferedReader) {
    this.jsonParser = jsonParser;
    this.descriptor = descriptor;
    this.bufferedReader = bufferedReader;
  }

  /** Parses all the messages and returns them in a list. */
  public ImmutableList<DynamicMessage> read() {
    ImmutableList.Builder<DynamicMessage> resultBuilder = ImmutableList.builder();

    try {
      StringBuilder stringBuilder = new StringBuilder();
      String line;
      while((line = bufferedReader.readLine()) != null) {
        if (!line.isEmpty()) {
          // Part of the current message. Accumulate.
          stringBuilder.append(line);
          continue;
        }

        // Done with the current message. Parse it and add to result;
        DynamicMessage.Builder nextMessage = DynamicMessage.newBuilder(descriptor);
        jsonParser.merge(stringBuilder.toString(), nextMessage);
        resultBuilder.add(nextMessage.build());
        stringBuilder = new StringBuilder();
      }
    } catch (IOException e) {
      throw new RuntimeException("Unable to read file", e);
    }

    return resultBuilder.build();
  }
}
