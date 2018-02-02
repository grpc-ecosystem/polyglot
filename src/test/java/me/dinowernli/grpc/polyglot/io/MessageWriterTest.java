package me.dinowernli.grpc.polyglot.io;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.google.protobuf.Any;
import com.google.protobuf.Duration;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;
import com.google.protobuf.util.JsonFormat.TypeRegistry;
import me.dinowernli.junit.TestClass;
import me.dinowernli.grpc.polyglot.io.testing.TestData;
import me.dinowernli.grpc.polyglot.testing.RecordingOutput;
import me.dinowernli.grpc.polyglot.testing.TestUtils;
import org.junit.Before;
import org.junit.Test;
import polyglot.test.TestProto.TestResponse;
import polyglot.test.TestProto.TunnelMessage;

import static com.google.common.truth.Truth.assertThat;

/** Unit tests for {@link MessageWriter}. */
@TestClass
public class MessageWriterTest {
  private static final String TESTDATA_ROOT = Paths.get(TestUtils.getWorkspaceRoot().toString(),
      "src", "test", "java", "me", "dinowernli", "grpc", "polyglot", "io", "testdata").toString();
  private static final TypeRegistry REGISTRY = TypeRegistry.newBuilder()
      .add(TunnelMessage.getDescriptor())
      .build();

  private MessageWriter<Message> writer;
  private RecordingOutput recordingOutput;

  @Before
  public void setUp() {
    recordingOutput = new RecordingOutput();
    writer = new MessageWriter<>(JsonFormat.printer().usingTypeRegistry(REGISTRY), recordingOutput);
  }

  @Test
  public void writesSingleMessage() throws Throwable {
    writer.onNext(TestData.REQUEST);
    writer.onCompleted();

    recordingOutput.close();

    assertThat(recordingOutput.getContentsAsString()).isEqualTo(loadTestFile("request.pb.ascii"));
  }

  @Test
  public void writesMultipleMessages() throws Throwable {
    TestData.REQUESTS_MULTI.forEach(writer::onNext);
    writer.onCompleted();

    recordingOutput.close();

    assertThat(recordingOutput.getContentsAsString())
        .isEqualTo(loadTestFile("requests_multi.pb.ascii"));
  }

  @Test
  public void writesMessageWithAny() throws Throwable {
    TestResponse proto = TestResponse.newBuilder()
        .setDuration(Duration.newBuilder()
            .setSeconds(5678)
            .setNanos(1234))
        .setAny(Any.pack(TunnelMessage.newBuilder()
            .setNumber(42)
            .build()))
        .build();

    writer.onNext(DynamicMessage.newBuilder(proto).build());
    writer.onCompleted();
    recordingOutput.close();

    assertThat(recordingOutput.getContentsAsString())
        .isEqualTo(loadTestFile("response_any.pb.ascii"));
  }

  private static String loadTestFile(String filename) {
    Path filePath = Paths.get(TESTDATA_ROOT, filename);
    try {
      return new String(Files.readAllBytes(filePath));
    } catch (IOException e) {
      throw new RuntimeException("Could not load file: " + filePath.toString());
    }
  }
}
