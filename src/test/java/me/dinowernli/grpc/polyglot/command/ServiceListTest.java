package me.dinowernli.grpc.polyglot.command;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.protobuf.DescriptorProtos.FileDescriptorSet;
import me.dinowernli.grpc.polyglot.testing.RecordingOutput;
import me.dinowernli.junit.TestClass;
import org.junit.Before;
import org.junit.Test;
import polyglot.test.TestProto;
import polyglot.test.foo.FooProto;
import sun.misc.IOUtils;

import static com.google.common.truth.Truth.assertThat;

/** Unit tests for {@link ServiceList}. */
@TestClass
public class ServiceListTest {
  private static FileDescriptorSet PROTO_FILE_DESCRIPTORS = FileDescriptorSet.newBuilder()
    .addFile(TestProto.getDescriptor().toProto())
    .addFile(FooProto.getDescriptor().toProto())
    .build();

  private static final String EXPECTED_SERVICE = "polyglot.test.TestService";
  private static final ImmutableList<String> EXPECTED_METHOD_NAMES = ImmutableList.of(
    "TestMethod", "TestMethodStream", "TestMethodClientStream", "TestMethodBidi");

  private RecordingOutput recordingOutput;

  @Before
  public void setUp() throws Throwable {
    recordingOutput = new RecordingOutput();
  }

  @Test
  public void testServiceListOutput() throws Throwable {
    ServiceList.listServices(
      recordingOutput,
      PROTO_FILE_DESCRIPTORS,
      "",
      Optional.empty(),
      Optional.empty(),
      Optional.empty(),
      Optional.empty());
    recordingOutput.close();

    validateOutput(recordingOutput.getContentsAsString(), EXPECTED_SERVICE, EXPECTED_METHOD_NAMES);
  }

  @Test
  public void testServiceListOutputWithServiceFilter() throws Throwable {
    ServiceList.listServices(
      recordingOutput,
      PROTO_FILE_DESCRIPTORS,
      "",
      Optional.of("TestService"),
      Optional.empty(),
      Optional.empty(),
      Optional.empty());
    recordingOutput.close();

    validateOutput(recordingOutput.getContentsAsString(), EXPECTED_SERVICE, EXPECTED_METHOD_NAMES);
  }

  @Test
  public void testServiceListOutputWithMethodFilter() throws Throwable {
    ServiceList.listServices(
      recordingOutput,
      PROTO_FILE_DESCRIPTORS,
      "",
      Optional.of("TestService"),
      Optional.of("TestMethodStream"),
      Optional.empty(),
      Optional.empty());
    recordingOutput.close();

    validateOutput(
      recordingOutput.getContentsAsString(),
      EXPECTED_SERVICE,
      ImmutableList.of("TestMethodStream"));
  }

  @Test
  public void testServiceListOutputWithMessageDetail() throws Throwable {
    ServiceList.listServices(
      recordingOutput,
      PROTO_FILE_DESCRIPTORS,
      "",
      Optional.of("TestService"),
      Optional.of("TestMethodStream"),
      Optional.of(true),
      Optional.empty());
    recordingOutput.close();

    validateMessageOutput(recordingOutput.getContentsAsString());
  }

  @Test
  public void testServiceListOutputWithMessageDetailJsonFormatted() throws Throwable {
    ServiceList.listServices(
            recordingOutput,
            PROTO_FILE_DESCRIPTORS,
            "",
            Optional.of("TestService"),
            Optional.of("TestMethodStream"),
            Optional.of(true),
            Optional.of("json"));
    recordingOutput.close();

    validateJsonMessageOutput(recordingOutput.getContentsAsString());
  }

  /** Compares the actual output with the expected output format */
  private void validateOutput(
      String output, String serviceName, ImmutableList<String> methodNames) {
    // Assuming no filters, we expect output of the form (note that [tmp_path]
    // is a placeholder):
    //
    // polyglot.test.TestService ->
    // [tmp_path]/src/main/proto/testing/test_service.proto
    // polyglot.test.TestService/TestMethod
    // polyglot.test.TestService/TestMethodStream
    // polyglot.test.TestService/TestMethodBidi

    String[] lines = output.trim().split("\n");
    assertThat(lines.length).isEqualTo(methodNames.size() + 1);

    // Parse the first line (always [ServiceName] -> [FileName]
    assertThat(lines[0]).startsWith(serviceName + " -> ");

    // Parse the subsequent lines (always [ServiceName]/[MethodName])
    for (int i = 0; i < methodNames.size(); i++) {
      assertThat(lines[i + 1].trim()).isEqualTo(serviceName + "/" + methodNames.get(i));
    }
  }

  /** Ensures that the message-rendering logic is correct */
  private void validateMessageOutput(String output) {
    // Assuming the filter is for TestService/TestMethodStream, then the message
    // should render as:
    //
    // polyglot.test.TestService ->
    // [tmp_path]/src/main/proto/testing/test_service.proto
    // polyglot.test.TestService/TestMethodStream
    // message[<optional> <single>]: STRING
    // foo[<optional> <single>] {
    // message[<optional> <single>]: STRING
    // }

    String[] lines = output.trim().split("\n");

    // Parse the first line (always [ServiceName] -> [FileName]
    assertThat(lines[0]).startsWith("polyglot.test.TestService -> ");

    ImmutableList<String> expectedLines = ImmutableList.of(
      "polyglot.test.TestService/TestMethodStream",
      "message[<optional> <single>]: STRING",
      "foo[<optional> <single>] {",
      "message[<optional> <single>]: STRING",
      "}");

    for (int i = 0; i < expectedLines.size(); i++) {
      assertThat(lines[i + 1].trim()).isEqualTo(expectedLines.get(i));
    }
  }

  /** Ensures that the message-rendering logic is correct */
  private void validateJsonMessageOutput(String output) {
    JsonElement parsedJSONOutput = new JsonParser().parse(output);
    JsonElement parsedExpectedJson = new JsonParser().parse(JSON_GOLD);
    assertThat(parsedExpectedJson.toString()).isEqualTo(parsedJSONOutput.toString());
  }

  private static final String JSON_GOLD = "{" +
          "\"services\": ["+
          "{"+
          "\"name\": \"TestService\","+
          "\"method\": ["+
          "{"+
          "\"name\": \"TestMethod\","+
          "\"inputType\": \".polyglot.test.TestRequest\","+
          "\"outputType\": \".polyglot.test.TestResponse\","+
          "\"options\": {}"+
          "},"+
          "{"+
          "\"name\": \"TestMethodStream\","+
          "\"inputType\": \".polyglot.test.TestRequest\","+
          "\"outputType\": \".polyglot.test.TestResponse\","+
          "\"options\": {},"+
          "\"serverStreaming\": true"+
          "},"+
          "{"+
          "\"name\": \"TestMethodClientStream\","+
          "\"inputType\": \".polyglot.test.TestRequest\","+
          "\"outputType\": \".polyglot.test.TestResponse\","+
          "\"options\": {},"+
          "\"clientStreaming\": true"+
          "},"+
          "{"+
          "\"name\": \"TestMethodBidi\","+
          "\"inputType\": \".polyglot.test.TestRequest\","+
          "\"outputType\": \".polyglot.test.TestResponse\","+
          "\"options\": {},"+
          "\"clientStreaming\": true,"+
          "\"serverStreaming\": true"+
          "}"+
          "]"+
          "}"+
          "],"+
          "\"dependencies\": ["+
          "{"+
          "\"name\": \"src/main/proto/testing/test_service.proto\","+
          "\"package\": \"polyglot.test\","+
          "\"dependency\": ["+
          "\"src/main/proto/testing/foo/foo.proto\""+
          "],"+
          "\"messageType\": ["+
          "{"+
          "\"name\": \"TestRequest\","+
          "\"field\": ["+
          "{"+
          "\"name\": \"message\","+
          "\"number\": 1,"+
          "\"label\": \"LABEL_OPTIONAL\","+
          "\"type\": \"TYPE_STRING\""+
          "},"+
          "{"+
          "\"name\": \"foo\","+
          "\"number\": 2,"+
          "\"label\": \"LABEL_OPTIONAL\","+
          "\"type\": \"TYPE_MESSAGE\","+
          "\"typeName\": \".polyglot.test.foo.Foo\""+
          "},"+
          "{"+
          "\"name\": \"number\","+
          "\"number\": 3,"+
          "\"label\": \"LABEL_OPTIONAL\","+
          "\"type\": \"TYPE_INT32\""+
          "}"+
          "]"+
          "},"+
          "{"+
          "\"name\": \"TestResponse\","+
          "\"field\": ["+
          "{"+
          "\"name\": \"message\","+
          "\"number\": 1,"+
          "\"label\": \"LABEL_OPTIONAL\","+
          "\"type\": \"TYPE_STRING\""+
          "}"+
          "]"+
          "}"+
          "],"+
          "\"service\": ["+
          "{"+
          "\"name\": \"TestService\","+
          "\"method\": ["+
          "{"+
          "\"name\": \"TestMethod\","+
          "\"inputType\": \".polyglot.test.TestRequest\","+
          "\"outputType\": \".polyglot.test.TestResponse\","+
          "\"options\": {}"+
          "},"+
          "{"+
          "\"name\": \"TestMethodStream\","+
          "\"inputType\": \".polyglot.test.TestRequest\","+
          "\"outputType\": \".polyglot.test.TestResponse\","+
          "\"options\": {},"+
          "\"serverStreaming\": true"+
          "},"+
          "{"+
          "\"name\": \"TestMethodClientStream\","+
          "\"inputType\": \".polyglot.test.TestRequest\","+
          "\"outputType\": \".polyglot.test.TestResponse\","+
          "\"options\": {},"+
          "\"clientStreaming\": true"+
          "},"+
          "{"+
          "\"name\": \"TestMethodBidi\","+
          "\"inputType\": \".polyglot.test.TestRequest\","+
          "\"outputType\": \".polyglot.test.TestResponse\","+
          "\"options\": {},"+
          "\"clientStreaming\": true,"+
          "\"serverStreaming\": true"+
          "}"+
          "]"+
          "}"+
          "],"+
          "\"options\": {"+
          "\"javaOuterClassname\": \"TestProto\""+
          "},"+
          "\"syntax\": \"proto3\""+
          "},"+
          "{"+
          "\"name\": \"src/main/proto/testing/foo/foo.proto\","+
          "\"package\": \"polyglot.test.foo\","+
          "\"messageType\": ["+
          "{"+
          "\"name\": \"Foo\","+
          "\"field\": ["+
          "{"+
          "\"name\": \"message\","+
          "\"number\": 1,"+
          "\"label\": \"LABEL_OPTIONAL\","+
          "\"type\": \"TYPE_STRING\""+
          "}"+
          "]"+
          "}"+
          "],"+
          "\"options\": {"+
          "\"javaOuterClassname\": \"FooProto\""+
          "},"+
          "\"syntax\": \"proto3\""+
          "}"+
          "]"+
          "}";
}
