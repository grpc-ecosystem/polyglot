package polyglot.protobuf;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;

import polyglot.protobuf.ProtoMethodName;

/** Unit tests for {@link ProtoMethodName}. */
public class ProtoMethodNameTest {
  @Test
  public void parsesCorrectly() {
    ProtoMethodName method = ProtoMethodName.parseFullGrpcMethodName("foo.bar.BazService/doStuff");
    assertThat(method.getMethodName()).isEqualTo("doStuff");
    assertThat(method.getPackageName()).isEqualTo("foo.bar");
    assertThat(method.getServiceName()).isEqualTo("BazService");
  }
}
