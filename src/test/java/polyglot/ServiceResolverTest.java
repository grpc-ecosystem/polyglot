package polyglot;

import org.junit.Before;
import org.junit.Test;

import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.Descriptors.FileDescriptor;

import polyglot.ProtoMethodName;
import polyglot.ServiceResolver;
import polyglot.test.TestProto;


/** Unit tests for {@link ServiceResolver}. */
public class ServiceResolverTest {
  private static FileDescriptorProto FILE_DESCRIPTOR = TestProto.getDescriptor().toProto();

  private ServiceResolver serviceResolver;

  @Before
  public void setUp() throws Throwable {
    FileDescriptor[] dependencies = new FileDescriptor[0];
    FileDescriptor fileDescriptor = FileDescriptor.buildFrom(FILE_DESCRIPTOR, dependencies);
    serviceResolver = ServiceResolver.fromFileDescriptors(fileDescriptor);
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
