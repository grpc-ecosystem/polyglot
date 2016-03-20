package org.polyglot;

import org.junit.Before;
import org.junit.Test;

import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.Descriptors.FileDescriptor;

import polyglot.ServiceResolver;
import polyglot.test.TestProto;


/** Unit tests for {@link ServiceResolver}. */
public class ServiceResolverTest {
  private static FileDescriptorProto FILE_DESCRIPTOR = TestProto.getDescriptor().toProto();

  private ServiceResolver serviceResolver;

  @Before
  public void setUp() throws Throwable {
    polyglot.test.TestProto p = null;

    FileDescriptor[] dependencies = new FileDescriptor[0];
    FileDescriptor fileDescriptor = FileDescriptor.buildFrom(FILE_DESCRIPTOR, dependencies);
    serviceResolver = ServiceResolver.fromFileDescriptors(fileDescriptor);
  }

  @Test(expected = IllegalArgumentException.class)
  public void resolveMissingService() {
    serviceResolver.resolveServiceMethod("does not", "exist");
  }

  @Test(expected = IllegalArgumentException.class)
  public void resolveMissingMethod() {
    serviceResolver.resolveServiceMethod("TestService", "some method");
  }

  @Test
  public void resolveHappyCase() {
    serviceResolver.resolveServiceMethod("TestService", "TestMethod");
  }
}
