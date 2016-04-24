package polyglot.protobuf;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

import polyglot.ConfigProto.ProtoConfiguration;
import polyglot.testing.TestUtils;

/** Unit tests for {@link ProtocInvoker}. */
public class ProtocInvokerTest {
  private static final Path TEST_PROTO_FILES =
      Paths.get(TestUtils.TESTING_PROTO_ROOT.toString(), "protobuf");

  @Test
  public void handlesStandaloneProtoFileWithoutImports() throws Throwable {
    ProtocInvoker invoker = ProtocInvoker.forConfig(ProtoConfiguration.newBuilder()
        .setProtoDiscoveryRoot(TEST_PROTO_FILES.toString())
        .build());
    invoker.invoke();
    // No crash.
  }
}
