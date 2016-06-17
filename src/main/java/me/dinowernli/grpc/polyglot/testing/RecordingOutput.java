package me.dinowernli.grpc.polyglot.testing;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import me.dinowernli.grpc.polyglot.io.Output;

/** An implementation of {@link Output} which just records all the contents. */
public class RecordingOutput implements Output {
  private final List<String> contents;
  private boolean closed;

  public RecordingOutput() {
    this.closed = false;
    this.contents = new ArrayList<>();
  }

  @Override
  public void close() throws IOException {
    Preconditions.checkState(!closed, "Output already closed previously");
    this.closed = true;
  }

  @Override
  public void write(String content) {
    contents.add(content);
  }

  @Override
  public void newLine() {
    write("\n");
  }

  @Override
  public void writeLine(String line) {
    write(line + "\n");
  }

  public ImmutableList<String> getContents() {
    Preconditions.checkState(closed, "Output not yet closed, can't get contents");
    return ImmutableList.copyOf(contents);
  }

  public String getContentsAsString() {
    return Joiner.on("").join(getContents());
  }
}
