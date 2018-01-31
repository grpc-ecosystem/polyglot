package me.dinowernli.grpc.polyglot.config;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.collect.ImmutableMultimap;
import me.dinowernli.junit.TestClass;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

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
        String.format("--add_protoc_includes=%s,%s", tempFile1.toString(), tempFile2.toString())));
    assertThat(params.additionalProtocIncludes()).hasSize(2);
  }

  @Test
  public void parsesAdditionalIncludesMulti() {
    CommandLineArgs params = parseArgs(ImmutableList.of(
        String.format("--add_protoc_includes=%s", tempFile1.toString())));
    assertThat(params.additionalProtocIncludes()).hasSize(1);
  }

  @Test
  public void parseMetadata() {
    CommandLineArgs params = parseArgs(ImmutableList.of(
        String.format("--metadata=%s:%s,%s:%s,%s:%s",
            "key1", "value1", "key1", "value2", "key2", "value2")));

    ImmutableMultimap<Object, Object> expectedResult =
        ImmutableMultimap.of("key1", "value1", "key1", "value2", "key2", "value2");
    assertThat(params.metadata()).isEqualTo(Optional.of(expectedResult));
  }

  @Test(expected = IllegalArgumentException.class)
  public void parseMetadataWithKeyWithoutValue() {
    CommandLineArgs params = parseArgs(ImmutableList.of(
        String.format("--metadata=%s:%s,%s", "key1", "value1", "key2")));

    params.metadata();
  }

  @Test
  public void parseOutputFileEvenIfAbsent() {
    Path filePath = Paths.get(tempFolder.getRoot().getAbsolutePath(), "some-file.txt");
    CommandLineArgs params = parseArgs(ImmutableList.of(
        "--output_file_path=" + filePath.toAbsolutePath().toString()));
    assertThat(params.outputFilePath().isPresent()).isTrue();
  }

  private static CommandLineArgs parseArgs(ImmutableList<String> args) {
    ImmutableList<String> allArgs = ImmutableList.<String>builder()
        .addAll(args)
        .add("--endpoint=somehost:1234")
        .add("--full_method=some.package/Method")
        .add("--proto_discovery_root=.")
        .add("--use_reflection=true")
        .build();
    return CommandLineArgs.parse(allArgs.toArray(new String[0]));
  }
}
