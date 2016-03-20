package polyglot;

import java.util.Arrays;

import com.google.common.collect.ImmutableList;
import com.google.protobuf.Descriptors.FileDescriptor;
import com.google.protobuf.Descriptors.MethodDescriptor;
import com.google.protobuf.Descriptors.ServiceDescriptor;

public class ServiceResolver {
  private final ImmutableList<FileDescriptor> fileDescriptors;

  public static ServiceResolver fromFileDescriptors(FileDescriptor... descriptors) {
    return new ServiceResolver(Arrays.asList(descriptors));
  }

  private ServiceResolver(Iterable<FileDescriptor> fileDescriptors) {
    this.fileDescriptors = ImmutableList.copyOf(fileDescriptors);
  }

  public MethodDescriptor resolveServiceMethod(String serviceName, String methodName) {
    ServiceDescriptor service = findService(serviceName);
    MethodDescriptor method = service.findMethodByName(methodName);
    if (method == null) {
      throw new IllegalArgumentException(
          "Unable to find method " + methodName + " in service " + serviceName);
    }
    return method;
  }

  private ServiceDescriptor findService(String serviceName) {
    for (FileDescriptor fileDescriptor : fileDescriptors) {
      ServiceDescriptor serviceDescriptor = fileDescriptor.findServiceByName(serviceName);
      if (serviceDescriptor != null) {
        return serviceDescriptor;
      }
    }
    throw new IllegalArgumentException("Unable to find service with name: " + serviceName);
  }
}
