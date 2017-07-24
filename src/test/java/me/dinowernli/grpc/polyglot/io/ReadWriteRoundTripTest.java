package me.dinowernli.grpc.polyglot.io;

import static com.google.common.truth.Truth.assertThat;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;

import me.dinowernli.junit.TestClass;
import me.dinowernli.grpc.polyglot.io.testing.TestData;
import polyglot.test.TestProto.TestRequest;

/** Test for the read/write round trip through {@link MessageReader} and {@link MessageWriter}. */
@TestClass
public class ReadWriteRoundTripTest {
  private static final Descriptor DESCRIPTOR = TestRequest.getDescriptor();
  private Path responseFilePath;

  @Before
  public void setUp() throws Throwable {
    responseFilePath = Files.createTempFile("response", "pb.ascii");
  }

  @Test
  public void emptyStream() throws Throwable {
    MessageWriter<Message> writer = makeWriter();
    writer.onCompleted();

    ImmutableList<DynamicMessage> results = makeReader().read();
    assertThat(results).isEmpty();
  }

  @Test
  public void singleMessage() throws Throwable {
    MessageWriter<Message> writer = makeWriter();
    writer.onNext(TestData.REQUEST);
    writer.onCompleted();

    ImmutableList<DynamicMessage> results = makeReader().read();
    assertThat(results).containsExactly(TestData.REQUEST);
  }

  @Test
  public void multipleMessages() throws Throwable {
    makeWriter().writeAll(TestData.REQUESTS_MULTI);
    ImmutableList<DynamicMessage> results = makeReader().read();
    assertThat(results).containsExactlyElementsIn(TestData.REQUESTS_MULTI);
  }

  private MessageReader makeReader() {
    return MessageReader.forFile(responseFilePath, DESCRIPTOR);
  }

  private MessageWriter<Message> makeWriter() throws IOException {
    return new MessageWriter<Message>(
        JsonFormat.printer(),
        Output.forStream(new PrintStream(Files.newOutputStream(responseFilePath))));
  }
}
