package polyglot;

import java.util.Arrays;
import java.util.Optional;

import com.google.common.collect.ImmutableList;
import com.google.protobuf.Descriptors.FileDescriptor;
import com.google.protobuf.Descriptors.MethodDescriptor;
import com.google.protobuf.Descriptors.ServiceDescriptor;

/** A locator used to read proto file descriptors and extract method definitions. */
public class ServiceResolver {
  private final ImmutableList<FileDescriptor> fileDescriptors;

  /** Creates a resolver which searches the supplied file descriptors. */
  public static ServiceResolver fromFileDescriptors(FileDescriptor... descriptors) {
    return new ServiceResolver(Arrays.asList(descriptors));
  }

  private ServiceResolver(Iterable<FileDescriptor> fileDescriptors) {
    this.fileDescriptors = ImmutableList.copyOf(fileDescriptors);
  }

  /**
   * Returns the descriptor of a protobuf method with the supplied service and method name. If the
   * method cannot be found, this throws {@link IllegalArgumentException}.
   */
  public MethodDescriptor resolveServiceMethod(String serviceName, String methodName) {
    return resolveServiceMethod(serviceName, methodName, Optional.empty() /* packageName */);
  }

  /**
   * Returns the descriptor of a protobuf method with the supplied grpc method name. If the method
   * cannot be found, this throws {@link IllegalArgumentException}.
   */
  public MethodDescriptor resolveServiceMethod(ProtoMethodName method) {
    return resolveServiceMethod(
        method.getServiceName(),
        method.getMethodName(),
        Optional.of(method.getPackageName()));
  }

  private MethodDescriptor resolveServiceMethod(
      String serviceName, String methodName, Optional<String> packageName) {
    ServiceDescriptor service = findService(serviceName, packageName);
    MethodDescriptor method = service.findMethodByName(methodName);
    if (method == null) {
      throw new IllegalArgumentException(
          "Unable to find method " + methodName + " in service " + serviceName);
    }
    return method;
  }

  private ServiceDescriptor findService(String serviceName, Optional<String> packageName) {
    // TODO(dino): Consider creating an index.
    for (FileDescriptor fileDescriptor : fileDescriptors) {
      if (packageName.isPresent() && !fileDescriptor.getPackage().equals(packageName.get())) {
        // Package does not match this file, ignore.
        continue;
      }

      ServiceDescriptor serviceDescriptor = fileDescriptor.findServiceByName(serviceName);
      if (serviceDescriptor != null) {
        return serviceDescriptor;
      }
    }
    throw new IllegalArgumentException("Unable to find service with name: " + serviceName);
  }
}
