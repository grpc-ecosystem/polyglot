package me.dinowernli.grpc.polyglot.protobuf;

import static com.google.common.truth.Truth.assertThat;

import me.dinowernli.junit.TestClass;

import org.junit.Test;

/** Unit tests for {@link ProtoMethodName}. */
@TestClass
public class ProtoMethodNameTest {
  @Test
  public void parsesCorrectly() {
    ProtoMethodName method = ProtoMethodName.parseFullGrpcMethodName("foo.bar.BazService/doStuff");
    assertThat(method.getMethodName()).isEqualTo("doStuff");
    assertThat(method.getPackageName()).isEqualTo("foo.bar");
    assertThat(method.getServiceName()).isEqualTo("BazService");
  }
}
