package polyglot;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorSet;
import com.google.protobuf.Descriptors.DescriptorValidationException;
import com.google.protobuf.Descriptors.FileDescriptor;
import com.google.protobuf.Descriptors.MethodDescriptor;
import com.google.protobuf.Descriptors.ServiceDescriptor;

/** A locator used to read proto file descriptors and extract method definitions. */
public class ServiceResolver {
  private static final Logger logger = LoggerFactory.getLogger(ServiceResolver.class);
  private final ImmutableList<FileDescriptor> fileDescriptors;

  /** Creates a resolver which searches the supplied file descriptors. */
  public static ServiceResolver fromFileDescriptors(FileDescriptor... descriptors) {
    return new ServiceResolver(Arrays.asList(descriptors));
  }

  /** Creates a resolver which searches the supplied {@link FileDescriptorSet}. */
  public static ServiceResolver fromFileDescriptorSet(FileDescriptorSet descriptorSet) {
    ImmutableMap<String, FileDescriptorProto> descriptorProtoIndex =
        computeDescriptorProtoIndex(descriptorSet);
    Map<String, FileDescriptor> descriptorCache = new HashMap<>();

    ImmutableList.Builder<FileDescriptor> result = ImmutableList.builder();
    for (FileDescriptorProto descriptorProto : descriptorSet.getFileList()) {
      try {
        result.add(descriptorFromProto(descriptorProto, descriptorProtoIndex, descriptorCache));
      } catch (DescriptorValidationException e) {
        logger.warn("Skipped descriptor " + descriptorProto.getName() + " due to error", e);
        continue;
      }
    }
    return new ServiceResolver(result.build());
  }

  private ServiceResolver(Iterable<FileDescriptor> fileDescriptors) {
    this.fileDescriptors = ImmutableList.copyOf(fileDescriptors);
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

  /**
   * Returns a map from descriptor proto name as found inside the descriptors to protos.
   */
  private static ImmutableMap<String, FileDescriptorProto> computeDescriptorProtoIndex(
      FileDescriptorSet fileDescriptorSet) {
    ImmutableMap.Builder<String, FileDescriptorProto> resultBuilder = ImmutableMap.builder();
    for (FileDescriptorProto descriptorProto : fileDescriptorSet.getFileList()) {
      resultBuilder.put(descriptorProto.getName(), descriptorProto);
    }
    return resultBuilder.build();
  }

  /**
   * Recursively constructs file descriptors for all dependencies of the supplied proto and returns
   * a {@link FileDescriptor} for the supplied proto itself. For maximal efficiency, reuse the
   * descriptorCache argument across calls.
   */
  private static FileDescriptor descriptorFromProto(
      FileDescriptorProto descriptorProto,
      ImmutableMap<String, FileDescriptorProto> descriptorProtoIndex,
      Map<String, FileDescriptor> descriptorCache) throws DescriptorValidationException {
    // First, check the cache.
    String descritorName = descriptorProto.getName();
    if (descriptorCache.containsKey(descritorName)) {
      return descriptorCache.get(descritorName);
    }

    // Then, fetch all the required dependencies recursively.
    ImmutableList.Builder<FileDescriptor> dependencies = ImmutableList.builder();
    for (String dependencyName : descriptorProto.getDependencyList()) {
      if (!descriptorProtoIndex.containsKey(dependencyName)) {
        throw new IllegalArgumentException("Could not find dependency: " + dependencyName);
      }
      FileDescriptorProto dependencyProto = descriptorProtoIndex.get(dependencyName);
      dependencies.add(descriptorFromProto(dependencyProto, descriptorProtoIndex, descriptorCache));
    }

    // Finally, construct the actual descriptor.
    FileDescriptor[] empty = new FileDescriptor[0];
    return FileDescriptor.buildFrom(descriptorProto, dependencies.build().toArray(empty));
  }
}
