package me.dinowernli.grpc.polyglot.io;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.google.protobuf.util.JsonFormat;

import me.dinowernli.grpc.polyglot.io.testing.TestData;
import me.dinowernli.grpc.polyglot.testing.RecordingOutput;

/** Unit tests for {@link MessageWriter}. */
public class FileMessageWriterTest {
  private MessageWriter writer;
  private RecordingOutput recordingOutput;

  @Before
  public void setUp() {
    recordingOutput = new RecordingOutput();
    writer = new MessageWriter(JsonFormat.printer(), recordingOutput);
  }

  @Test
  public void writesSingleMessage() throws Throwable {
    writer.onNext(TestData.REQUEST);
    writer.onCompleted();

    recordingOutput.close();

    assertThat(recordingOutput.getContentsAsString()).isEqualTo(TestData.REQUEST_JSON);
  }

  @Test
  public void writesMultipleMessages() throws Throwable {
    writer.onNext(TestData.REQUEST);
    writer.onNext(TestData.REQUEST);
    writer.onNext(TestData.REQUEST);
    writer.onCompleted();

    recordingOutput.close();

    assertThat(recordingOutput.getContentsAsString())
        .isEqualTo(TestData.REQUEST_JSON + TestData.REQUEST_JSON + TestData.REQUEST_JSON);
  }
}
