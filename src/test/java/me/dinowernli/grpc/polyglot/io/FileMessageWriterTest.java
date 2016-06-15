package me.dinowernli.grpc.polyglot.io;

import static com.google.common.truth.Truth.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;

import org.junit.Before;
import org.junit.Test;

import com.google.protobuf.util.JsonFormat;

import me.dinowernli.grpc.polyglot.io.FileMessageWriter;
import me.dinowernli.grpc.polyglot.io.testing.TestData;

/** Unit tests for {@link FileMessageWriter}. */
public class FileMessageWriterTest {
  private ByteArrayOutputStream outputStream;
  private FileMessageWriter writer;

  @Before
  public void setUp() {
    outputStream = new ByteArrayOutputStream();
    writer = new FileMessageWriter(JsonFormat.printer(), new PrintWriter(outputStream));
  }

  @Test
  public void writesSingleMessage() {
    writer.onNext(TestData.REQUEST);
    writer.onCompleted();
    String result = outputStream.toString();
    assertThat(result).isEqualTo(TestData.REQUEST_JSON);
  }

  @Test
  public void writesMultipleMessages() {
    writer.onNext(TestData.REQUEST);
    writer.onNext(TestData.REQUEST);
    writer.onNext(TestData.REQUEST);
    writer.onCompleted();
    String result = outputStream.toString();
    assertThat(result)
        .isEqualTo(TestData.REQUEST_JSON + TestData.REQUEST_JSON + TestData.REQUEST_JSON);
  }
}
