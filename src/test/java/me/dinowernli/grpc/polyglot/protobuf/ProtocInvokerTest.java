package me.dinowernli.grpc.polyglot.protobuf;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

import me.dinowernli.junit.TestClass;
import me.dinowernli.grpc.polyglot.testing.TestUtils;
import polyglot.ConfigProto.ProtoConfiguration;

/** Unit tests for {@link ProtocInvoker}. */
@TestClass
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
