package me.dinowernli.grpc.polyglot.command;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.DescriptorProtos.FileDescriptorSet;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.MethodDescriptor;
import com.google.protobuf.Descriptors.ServiceDescriptor;
import com.google.protobuf.util.JsonFormat;

import me.dinowernli.grpc.polyglot.io.MessageWriter;
import me.dinowernli.grpc.polyglot.io.Output;
import me.dinowernli.grpc.polyglot.protobuf.ServiceResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import polyglot.OutputProto.ListServicesJsonOutput;

/** Utility to list the services, methods and message definitions for the known GRPC end-points */
public class ServiceList {
  private static final Logger logger = LoggerFactory.getLogger(MessageWriter.class);

  /** Lists the GRPC services - filtered by service name (contains) or method name (contains) */
  public static void listServices(Output output, FileDescriptorSet fileDescriptorSet, String protoDiscoveryRoot,
      Optional<String> serviceFilter, Optional<String> methodFilter, Optional<Boolean> withMessage,
      Optional<String> listOutputFormat) {

    ServiceResolver serviceResolver = ServiceResolver.fromFileDescriptorSet(fileDescriptorSet);

    if (listOutputFormat.isPresent() && listOutputFormat.get().equals("json")) {
      printJsonOutput(output, serviceResolver, serviceFilter, methodFilter);
    } else {

      // Add white-space before the rendered output
      output.newLine();

      for (ServiceDescriptor descriptor : serviceResolver.listServices()) {
        boolean matchingDescriptor = !serviceFilter.isPresent()
            || descriptor.getFullName().toLowerCase().contains(serviceFilter.get().toLowerCase());

        if (matchingDescriptor) {
          listMethods(output, protoDiscoveryRoot, descriptor, methodFilter, withMessage);
        }
      }
    }
  }

  /** Lists the methods on the service (the methodFilter will be applied if non-empty)  */
  private static void listMethods(Output output, String protoDiscoveryRoot, ServiceDescriptor descriptor,
      Optional<String> methodFilter, Optional<Boolean> withMessage) {

    boolean printedService = false;

    // Due to the way the protos are discovered, the leaf directly of the  protoDiscoveryRoot
    // is the same as the root directory as the proto file
    File protoDiscoveryDir = new File(protoDiscoveryRoot).getParentFile();

    for (MethodDescriptor method : descriptor.getMethods()) {
      if (!methodFilter.isPresent() || method.getName().toLowerCase().contains(methodFilter.get().toLowerCase())) {

        // Only print the service name once - and only if a method is going to be printed
        if (!printedService) {
          File pFile = new File(protoDiscoveryDir, descriptor.getFile().getName());
          output.writeLine(descriptor.getFullName() + " -> " + pFile.getAbsolutePath());
          printedService = true;
        }

        output.writeLine("  " + descriptor.getFullName() + "/" + method.getName());

        // If requested, add the message definition
        if (withMessage.isPresent() && withMessage.get()) {
          output.writeLine(renderDescriptor(method.getInputType(), "  "));
          output.newLine();
        }
      }
    }

    if (printedService) {
      output.newLine();
    }
  }

  /** Creates a human-readable string to help the user build a message to send to an end-point */
  private static String renderDescriptor(Descriptor descriptor, String indent) {
    if (descriptor.getFields().size() == 0) {
      return indent + "<empty>";
    }

    List<String> fieldsAsStrings = descriptor.getFields().stream().map(field -> renderDescriptor(field, indent + "  "))
        .collect(Collectors.toList());

    return Joiner.on(System.lineSeparator()).join(fieldsAsStrings);
  }

  /** Create a readable string from the field to help the user build a message  */
  private static String renderDescriptor(FieldDescriptor descriptor, String indent) {
    String isOpt = descriptor.isOptional() ? "<optional>" : "<required>";
    String isRep = descriptor.isRepeated() ? "<repeated>" : "<single>";
    String fieldPrefix = indent + descriptor.getJsonName() + "[" + isOpt + " " + isRep + "]";

    if (descriptor.getJavaType() == FieldDescriptor.JavaType.MESSAGE) {
      return fieldPrefix + " {" + System.lineSeparator() + renderDescriptor(descriptor.getMessageType(), indent + "  ")
          + System.lineSeparator() + indent + "}";

    } else if (descriptor.getJavaType() == FieldDescriptor.JavaType.ENUM) {
      return fieldPrefix + ": " + descriptor.getEnumType().getValues();

    } else {
      return fieldPrefix + ": " + descriptor.getJavaType();
    }
  }

  private static void printJsonOutput(Output output, ServiceResolver serviceResolver, Optional<String> serviceFilter,
      Optional<String> methodFilter) {
    ListServicesJsonOutput.Builder listServicesJsonOutputBuilder = ListServicesJsonOutput.newBuilder();

    // Filter service descriptors (case insensitive)
    ImmutableList<ServiceDescriptor> serviceDescriptors = ImmutableList
      .copyOf(Lists.newArrayList(serviceResolver.listServices()).stream()
        .filter(serviceDescriptor -> !serviceFilter.isPresent()
            || serviceDescriptor.getFullName().toLowerCase().contains(serviceFilter.get().toLowerCase()))
        .collect(Collectors.toList()));

    ImmutableList<DescriptorProtos.ServiceDescriptorProto> serviceDescriptorProtos = ImmutableList
      .copyOf(serviceDescriptors.stream().map(serviceDescriptor -> serviceDescriptor.toProto().toBuilder()
        // filtering methods by clearing the methods then readding back only the filtered ones
        .clearMethod()
        .addAllMethod(serviceDescriptor.getMethods().stream()
          .filter(methodDescriptor -> !methodFilter.isPresent()
              || methodDescriptor.getName().toLowerCase().contains(methodFilter.get().toLowerCase()))
          .map(methodDescriptor -> methodDescriptor.toProto()).collect(Collectors.toList()))
        .build()).collect(Collectors.toList()));

    listServicesJsonOutputBuilder.addAllServices(serviceDescriptorProtos);

    Set<DescriptorProtos.FileDescriptorProto> fileProtosSet = new HashSet<>();

    serviceDescriptors.forEach(serviceDescriptor -> {
      fileProtosSet.add(serviceDescriptor.getFile().toProto());
      serviceDescriptor.getFile().getDependencies().stream()
        .forEach(fileDescriptor -> fileProtosSet.add(fileDescriptor.toProto()));
    });

    listServicesJsonOutputBuilder.addAllDependencies(fileProtosSet);

    try {
      String jsonOut = JsonFormat.printer().print(listServicesJsonOutputBuilder.build());
      output.writeLine(jsonOut);
    } catch (com.google.protobuf.InvalidProtocolBufferException e) {
      logger.error("Error printing JSON output.", e);
    }
  }
}