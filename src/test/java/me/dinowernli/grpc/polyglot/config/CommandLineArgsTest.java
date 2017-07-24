package me.dinowernli.grpc.polyglot.config;

import static com.google.common.truth.Truth.assertThat;

import me.dinowernli.junit.TestClass;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableList;

/** Unit tests for {@link CommandLineArgs}. */
@TestClass
public class CommandLineArgsTest {
  private Path tempFile1;
  private Path tempFile2;

  @Before
  public void setUp() throws Throwable {
    tempFile1 = Files.createTempFile("a", ".txt");
    tempFile2 = Files.createTempFile("b", ".txt");
  }

  @After
  public void tearDown() throws Throwable {
    Files.delete(tempFile1);
    Files.delete(tempFile2);
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

  private static CommandLineArgs parseArgs(ImmutableList<String> args) {
    ImmutableList<String> allArgs = ImmutableList.<String>builder()
        .addAll(args)
        .add("--endpoint=somehost:1234")
        .add("--full_method=some.package/Method")
        .add("--proto_discovery_root=.")
        .build();
    return CommandLineArgs.parse(allArgs.toArray(new String[0]));
  }
}
