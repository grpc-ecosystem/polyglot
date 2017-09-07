package me.dinowernli.grpc.polyglot.command;

import java.io.IOException;
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
  private static final String TMP_DIR = System.getProperty("user.dir");

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

//  @Test
//  public void testServiceListOutputWithMessageDetailJsonFormatted() throws Throwable {
//    ServiceList.listServices(
//            recordingOutput,
//            PROTO_FILE_DESCRIPTORS,
//            "",
//            Optional.of("TestService"),
//            Optional.of("TestMethodStream"),
//            Optional.of(true),
//            Optional.of("json"));
//    recordingOutput.close();
//
//    validateJsonMessageOutput(recordingOutput.getContentsAsString());
//  }

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
    Path jsonOutputGoldPath = Paths.get("./jsonOutputGold.txt");

    try {
      String expectedJSON = Files.readAllLines(jsonOutputGoldPath).stream().collect(Collectors.joining(""));
      JsonElement parsedExpectedJson = new JsonParser().parse(expectedJSON);
      assertThat(parsedExpectedJson.toString()).isEqualTo(parsedJSONOutput.toString());
    } catch (IOException e) {
      throw new RuntimeException("Unable to find gold output file: " + jsonOutputGoldPath.toString(), e);
    }
  }
}
