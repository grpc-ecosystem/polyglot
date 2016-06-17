package me.dinowernli.grpc.polyglot.io;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.LoggerFactory;

import me.dinowernli.grpc.polyglot.io.OutputImpl.LogWriter;
import me.dinowernli.grpc.polyglot.io.OutputImpl.PrintStreamWriter;
import polyglot.ConfigProto.OutputConfiguration;
import polyglot.ConfigProto.OutputConfiguration.Destination;

/**
 * A one-stop-shop for output of the binary. Supports writing to logs, to streams, to files, etc.
 */
public interface Output extends AutoCloseable {
  /** Writes a single string of output. */
  void write(String content);

  /** Writes a line of content. */
  void writeLine(String line);

  /** Writes a blank line. */
  void newLine();

  /**
   * Creates a new {@link OutputImpl} instance for the supplied config. The retruned instance must ]
   * be closed after use or written content could go missing.
   */
  public static OutputImpl forConfiguration(OutputConfiguration outputConfig) {
    Destination destination = outputConfig.getDestination();
    switch(destination) {
      case STDOUT:
        return new OutputImpl(PrintStreamWriter.forStdout());
      case FILE:
        Path filePath = Paths.get(outputConfig.getFilePath());
        return new OutputImpl(PrintStreamWriter.forFile(filePath));
      case LOG:
        return new OutputImpl(new LogWriter(LoggerFactory.getLogger("Output")));
      default:
        throw new IllegalArgumentException("Unrecognized output destination " + destination);
    }
  }
}
