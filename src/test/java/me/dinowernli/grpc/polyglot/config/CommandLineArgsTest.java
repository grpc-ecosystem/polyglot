package me.dinowernli.grpc.polyglot.config;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.collect.ImmutableMultimap;
import me.dinowernli.junit.TestClass;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.google.common.collect.ImmutableList;
import org.junit.rules.TemporaryFolder;

/** Unit tests for {@link CommandLineArgs}. */
@TestClass
public class CommandLineArgsTest {
  @Rule public TemporaryFolder tempFolder = new TemporaryFolder();
  private Path tempFile1;
  private Path tempFile2;

  @Before
  public void setUp() throws Throwable {
    tempFile1 = Paths.get(tempFolder.newFile().toURI());
    tempFile2 = Paths.get(tempFolder.newFile().toURI());
  }

  @Test
  public void parsesAdditionalIncludesSingle() {
    CommandLineArgs params = parseArgs(ImmutableList.of(
        makeArg("add_protoc_includes", String.format("%s,%s", tempFile1.toString(), tempFile2.toString()))),
      Collections.emptyList());
    assertThat(params.additionalProtocIncludes()).hasSize(2);
  }

  @Test
  public void parsesAdditionalIncludesSingle_usingSpaceSeparator() {
    CommandLineArgs params = parseArgs(ImmutableList.of(
        makeArg("add_protoc_includes", ' ', String.format("%s,%s", tempFile1.toString(), tempFile2.toString()))),
      Collections.emptyList(), ' ');
    assertThat(params.additionalProtocIncludes()).hasSize(2);
  }

  @Test
  public void parsesAdditionalIncludesSingle_usingMixedSeparators() {
    CommandLineArgs params = parseArgs(ImmutableList.of(
        makeArg("add_protoc_includes", '=', String.format("%s,%s", tempFile1.toString(), tempFile2.toString()))),
      Collections.emptyList(), ' ');
    assertThat(params.additionalProtocIncludes()).hasSize(2);
  }

  @Test
  public void parsesAdditionalIncludesMulti() {
    CommandLineArgs params = parseArgs(ImmutableList.of(
        makeArg("add_protoc_includes", tempFile1.toString())),
      Collections.emptyList());
    assertThat(params.additionalProtocIncludes()).hasSize(1);
  }

  @Test
  public void parseMetadata() {
    CommandLineArgs params = parseArgs(Collections.emptyList(), ImmutableList.of(
        makeArg("metadata", String.format("%s:%s,%s:%s,%s:%s",
            "key1", "value1", "key1", "value2", "key2", "value2"))));

    ImmutableMultimap<Object, Object> expectedResult =
      ImmutableMultimap.of("key1", "value1", "key1", "value2", "key2", "value2");
    assertThat(params.metadata()).isEqualTo(Optional.of(expectedResult));
  }

  @Test
  public void parseMetadata_usingSpaceSeparator() {
    CommandLineArgs params = parseArgs(Collections.emptyList(), ImmutableList.of(
        makeArg("metadata", ' ', String.format("%s:%s,%s:%s,%s:%s",
            "key1", "value1", "key1", "value2", "key2", "value2"))), ' ');

    ImmutableMultimap<Object, Object> expectedResult =
      ImmutableMultimap.of("key1", "value1", "key1", "value2", "key2", "value2");
    assertThat(params.metadata()).isEqualTo(Optional.of(expectedResult));
  }

  @Test
  public void parseMetadataWithSpaces() {
    CommandLineArgs params = parseArgs(Collections.emptyList(), ImmutableList.of(
        makeArg("metadata", String.format("%s:%s,%s:%s,%s:%s",
            "key1", "value1 ", "key2", " value2", "key3", "value 3"))));

    ImmutableMultimap<Object, Object> expectedResult =
      ImmutableMultimap.of("key1", "value1 ", "key2", " value2", "key3", "value 3");
    assertThat(params.metadata()).isEqualTo(Optional.of(expectedResult));
  }

  @Test
  public void parseMetadataWithSpaces_usingSpaceSeparator() {
    CommandLineArgs params = parseArgs(Collections.emptyList(), ImmutableList.of(
        makeArg("metadata", ' ', String.format("%s:%s,%s:%s,%s:%s",
            "key1", "value1 ", "key2", " value2", "key3", "value 3"))), ' ');

    ImmutableMultimap<Object, Object> expectedResult =
      ImmutableMultimap.of("key1", "value1 ", "key2", " value2", "key3", "value 3");
    assertThat(params.metadata()).isEqualTo(Optional.of(expectedResult));
  }

  @Test(expected = IllegalArgumentException.class)
  public void parseMetadataWithKeyWithoutValue() {
    CommandLineArgs params = parseArgs(Collections.emptyList(), ImmutableList.of(
        makeArg("metadata", String.format("%s:%s,%s", "key1", "value1", "key2"))));

    params.metadata();
  }

  @Test
  public void parseMetadataWithColons() {
    CommandLineArgs params = parseArgs(Collections.emptyList(), ImmutableList.of(
        makeArg("metadata", String.format("%s:%s,%s:%s,%s:%s",
            "key1", "value:1", "key2", "value:2", "key3", "value:3"))));

    ImmutableMultimap<Object, Object> expectedResult =
      ImmutableMultimap.of("key1", "value:1", "key2", "value:2", "key3", "value:3");
    assertThat(params.metadata()).isEqualTo(Optional.of(expectedResult));

  }

  @Test
  public void parseOutputFileEvenIfAbsent() {
    Path filePath = Paths.get(tempFolder.getRoot().getAbsolutePath(), "some-file.txt");
    CommandLineArgs params = parseArgs(ImmutableList.of(
        makeArg("output_file_path", filePath.toAbsolutePath().toString())),
      Collections.emptyList());
    assertThat(params.outputFilePath().isPresent()).isTrue();
  }

  @Test(expected = IllegalArgumentException.class)
  public void parseOptionWithoutCommand() {
    CommandLineArgs.parse(new String[]{makeArg("endpoint", "somehost:1234")});
  }

  @Test
  public void usage() {
    assertThat(CommandLineArgs.getUsage()).startsWith("Usage: java -jar polyglot.jar [options] [command] [command options]");
  }

  @Test
  public void help() {
    assertThat(CommandLineArgs.parse(new String[]{"--help"}).isHelp()).isTrue();
  }

  private static CommandLineArgs parseArgs(List<String> args, List<String> callArgs) {
    return parseArgs(args, callArgs, '=');
  }

  private static CommandLineArgs parseArgs(List<String> args, List<String> callArgs, char separator) {
    ImmutableList<String> allArgs = ImmutableList.<String>builder()
      .addAll(args)
      .add(makeArg("proto_discovery_root", separator, "."))
      .add(makeArg("use_reflection", separator, "true"))
      .add("call")
      .addAll(callArgs)
      .add(makeArg("endpoint", separator, "somehost:1234"))
      .add(makeArg("full_method", separator, "some.package/Method"))
      .build();
    return CommandLineArgs.parse(allArgs.toArray(new String[0]));
  }

  private static String makeArg(String option, String value) {
    return makeArg(option, '=', value);
  }

  private static String makeArg(String option, char separator, String value) {
    return String.format("--%s%c%s", option, separator, value);
  }

}
