package me.dinowernli.grpc.polyglot.protobuf;

import com.google.protobuf.DescriptorProtos.FileDescriptorSet;
import me.dinowernli.junit.TestClass;
import org.junit.Before;
import org.junit.Test;
import polyglot.test.TestProto;
import polyglot.test.foo.FooProto;


/** Unit tests for {@link ServiceResolver}. */
@TestClass
public class ServiceResolverTest {
  private static FileDescriptorSet PROTO_FILE_DESCRIPTORS = FileDescriptorSet.newBuilder()
      .addFile(TestProto.getDescriptor().toProto())
      .addFile(FooProto.getDescriptor().toProto())
      .addAllFile(WellKnownTypes.descriptors())
      .build();

  private ServiceResolver serviceResolver;

  @Before
  public void setUp() {
    serviceResolver = ServiceResolver.fromFileDescriptorSet(PROTO_FILE_DESCRIPTORS);
  }

  @Test(expected = IllegalArgumentException.class)
  public void resolveMissingService() {
    ProtoMethodName method = ProtoMethodName.parseFullGrpcMethodName("asdf/doesnotexist");
    serviceResolver.resolveServiceMethod(method);
  }

  @Test(expected = IllegalArgumentException.class)
  public void resolveMissingMethod() {
    ProtoMethodName method = ProtoMethodName.parseFullGrpcMethodName("TestService/doesnotexist");
    serviceResolver.resolveServiceMethod(method);
  }

  @Test
  public void resolveHappyCase() {
    serviceResolver.resolveServiceMethod(
        ProtoMethodName.parseFullGrpcMethodName("polyglot.test.TestService/TestMethod"));
  }
}
