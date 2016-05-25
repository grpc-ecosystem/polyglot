package polyglot.command;

import static com.google.common.truth.Truth.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.google.protobuf.DescriptorProtos.FileDescriptorSet;

import polyglot.test.TestProto;
import polyglot.test.foo.FooProto;

/** Unit tests for {@link ServiceList}. */
public class ServiceListTest {

  private static FileDescriptorSet PROTO_FILE_DESCRIPTORS = FileDescriptorSet.newBuilder()
      .addFile(TestProto.getDescriptor().toProto())
      .addFile(FooProto.getDescriptor().toProto())
      .build();
  
  @Before
  public void setUp() throws Throwable {
  }

  @After
  public void tearDown() throws Throwable {
  }

  @Test
  public void parsesAdditionalIncludesSingle() {
    Optional<String> serviceFilter = Optional.empty();
    Optional<String> methodFilter = Optional.empty();
    Optional<Boolean> withMessage = Optional.empty();
    
    ServiceList.listServices(
        PROTO_FILE_DESCRIPTORS, 
        "", 
        serviceFilter, 
        methodFilter, 
        withMessage);
  }  
}
