package polyglot;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.google.common.base.Joiner;
import com.google.protobuf.DescriptorProtos.FileDescriptorSet;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.MethodDescriptor;
import com.google.protobuf.Descriptors.ServiceDescriptor;

import polyglot.protobuf.ServiceResolver;

/** Simple utility to list the services, methods and message definitions for the known GRPC end-points */
public class ServiceList {

  /** Lists the set of GRPC services - optionally filtered by service name (contains) or method name (contains) */ 
  public static void listServices(FileDescriptorSet fileDescriptorSet, String protoDiscoveryRoot,
      Optional<String> serviceFilter, Optional<String> methodFilter, Optional<Boolean> withMessage) {

    ServiceResolver serviceResolver = ServiceResolver.fromFileDescriptorSet(fileDescriptorSet);

    // Add white-space before the rendered output
    System.out.println();

    for (ServiceDescriptor descriptor : serviceResolver.listServices()) {
      boolean matchingDescriptor = !serviceFilter.isPresent()
          || descriptor.getFullName().toLowerCase().contains(serviceFilter.get().toLowerCase());

      if (matchingDescriptor) {
        listMethods(protoDiscoveryRoot, descriptor, methodFilter, withMessage);
      }
    }
  }

  /**
   * Lists the methods on the service (the methodFilter will be applied if
   * non-empty)
   */
  private static void listMethods(String protoDiscoveryRoot, ServiceDescriptor descriptor,
      Optional<String> methodFilter, Optional<Boolean> withMessage) {

    boolean printedService = false;

    for (MethodDescriptor method : descriptor.getMethods()) {
      if (!methodFilter.isPresent() || method.getName().contains(methodFilter.get())) {
        if (!printedService) {
          System.out.println(descriptor.getFullName() + " -> "
              + new File(protoDiscoveryRoot, descriptor.getFile().getFullName()).getAbsolutePath());
          printedService = true;
        }

        System.out.println("  " + descriptor.getFullName() + "/" + method.getName());

        if (withMessage.isPresent() && withMessage.get()) {
          System.out.println(renderDescriptor(method.getInputType(), "  "));
          System.out.println();
        }
      }
    }

    if (printedService) {
      System.out.println();
    }
  }

  /** Creates a human-readable string to help the user build a message to send to an end-point */
  private static String renderDescriptor(Descriptor descriptor, String indent) {
    if (descriptor.getFields().size() == 0) {
      return indent + "<empty>";
    }

    List<String> fieldsAsStrings = 
        descriptor.getFields().stream()
        .map(field -> renderDescriptor(field, indent + "  "))
        .collect(Collectors.toList());

    return Joiner.on(System.lineSeparator()).join(fieldsAsStrings);
  }

  /** Creates a human-readable string of a field to help the user build a message to send to an end-point */
  private static String renderDescriptor(FieldDescriptor descriptor, String indent) {
    String isOptional = descriptor.isOptional() ? "<optional>" : "<required>";
    String isRepeated = descriptor.isRepeated() ? "<repeated>" : "<single>";
    String fieldPrefix = indent + descriptor.getJsonName() + "[" + isOptional + " " + isRepeated + "]";

    if (descriptor.getJavaType() == com.google.protobuf.Descriptors.FieldDescriptor.JavaType.MESSAGE) {
      return 
          fieldPrefix + " {" + System.lineSeparator() 
          + renderDescriptor(descriptor.getMessageType(), indent + "  ") 
          + System.lineSeparator() + indent + "}";

    } else if (descriptor.getJavaType() == com.google.protobuf.Descriptors.FieldDescriptor.JavaType.ENUM) {
      return fieldPrefix + ": " + descriptor.getEnumType().getValues();

    } else {
      return fieldPrefix + ": " + descriptor.getJavaType();
    }
  }
}
