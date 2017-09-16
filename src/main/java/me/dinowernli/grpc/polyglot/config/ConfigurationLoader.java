package me.dinowernli.grpc.polyglot.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.stream.Collectors;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.protobuf.util.JsonFormat;

import polyglot.ConfigProto.Configuration;
import polyglot.ConfigProto.ConfigurationSet;
import polyglot.ConfigProto.OutputConfiguration.Destination;

/** A utility which manipulating and reading a single {@link ConfigurationSet}. */
public class ConfigurationLoader {
  private static final String DEFAULT_FILE_NAME = "config.pb.json";
  private static final String DEFAULT_LOCATION = ".polyglot";

  /** If this is absent, we hand out default instances of configs. */
  private final Optional<ConfigurationSet> configSet;

  /** Optional overrides for the configuration. */
  private final Optional<CommandLineArgs> maybeOverrides;

  /**
   * Returns a {@link ConfigurationLoader} backed by a {@link ConfigurationSet} in the current
   * user's home directory. If no such file exists, it falls back to a default config.
   */
  public static ConfigurationLoader forDefaultConfigSet() {
    String homeDirectory = System.getProperty("user.home");
    Path defaultLocation = Paths.get(homeDirectory, DEFAULT_LOCATION, DEFAULT_FILE_NAME);
    if (Files.exists(defaultLocation)) {
      return ConfigurationLoader.forFile(defaultLocation);
    } else {
      return new ConfigurationLoader(Optional.empty(), Optional.empty());
    }
  }

  /** Constructs a {@link ConfigurationLoader} from an explicit {@link ConfigurationSet}. */
  public static ConfigurationLoader forConfigSet(ConfigurationSet configSet) {
    return new ConfigurationLoader(Optional.of(configSet), Optional.empty() /* overrides */);
  }

  /** Returns a loader backed by config set obtained from the supplied file. */
  public static ConfigurationLoader forFile(Path configFile) {
    try {
      ConfigurationSet.Builder configSetBuilder = ConfigurationSet.newBuilder();
      String fileContent = Joiner.on('\n').join(Files.readAllLines(configFile));
      JsonFormat.parser().merge(fileContent, configSetBuilder);
      return ConfigurationLoader.forConfigSet(configSetBuilder.build());
    } catch (IOException e) {
      throw new RuntimeException("Unable to read config file: " + configFile.toString(), e);
    }
  }

  @VisibleForTesting
  ConfigurationLoader(Optional<ConfigurationSet> configSet, Optional<CommandLineArgs> maybeOverrides) {
    this.configSet = configSet;
    this.maybeOverrides = maybeOverrides;
  }

  /** Returns a new instance of {@link ConfigurationLoader} with the supplied overrides. */
  public ConfigurationLoader withOverrides(CommandLineArgs overrides) {
    return new ConfigurationLoader(configSet, Optional.of(overrides));
  }

  /** Returns the default configuration from the loaded configuration set. */
  public Configuration getDefaultConfiguration() {
    return applyOverrides(getDefaultConfigurationInternal());
  }

  /** Returns a config with the supplied name and throws if no such config is found. */
  public Configuration getNamedConfiguration(String name) {
    return applyOverrides(getNamedConfigurationInternal(name));
  }

  private Configuration getDefaultConfigurationInternal() {
    if (isEmptyConfig()) {
      return Configuration.getDefaultInstance();
    }
    if (configSet.get().getConfigurationsList().isEmpty()) {
      throw new IllegalStateException("No configs present in config set");
    }
    return configSet.get().getConfigurations(0);
  }

  private Configuration getNamedConfigurationInternal(String name) {
    Preconditions.checkState(!isEmptyConfig(), "Cannot load named config with a config set");
    return configSet.get().getConfigurationsList().stream()
        .filter(config -> config.getName().equals(name))
        .findAny()
        .orElseThrow(() -> new IllegalArgumentException("Could not find named config: " + name));
  }

  /** Returns the {@link Configuration} with overrides, if any, applied to it. */
  private Configuration applyOverrides(Configuration configuration) {
    if (!maybeOverrides.isPresent()) {
      return configuration;
    }

    CommandLineArgs overrides = maybeOverrides.get();
    Configuration.Builder resultBuilder = configuration.toBuilder();

    resultBuilder.getProtoConfigBuilder().setUseReflection(overrides.useReflection());

    overrides.useTls().ifPresent(resultBuilder.getCallConfigBuilder()::setUseTls);
    overrides.outputFilePath().ifPresent(path -> {
      resultBuilder.getOutputConfigBuilder().setDestination(Destination.FILE);
      resultBuilder.getOutputConfigBuilder().setFilePath(path.toString());
    });

    resultBuilder.getProtoConfigBuilder().addAllIncludePaths(
        overrides.additionalProtocIncludes().stream()
            .map(Path::toString)
            .collect(Collectors.toList()));

    overrides.protoDiscoveryRoot().ifPresent(
        root -> resultBuilder.getProtoConfigBuilder().setProtoDiscoveryRoot(root.toString()));

    overrides.getRpcDeadlineMs().ifPresent(
        resultBuilder.getCallConfigBuilder()::setDeadlineMs);

    overrides.tlsCaCertPath().ifPresent(
        path -> resultBuilder.getCallConfigBuilder().setTlsCaCertPath(path.toString()));

    overrides.tlsClientCertPath().ifPresent(
        path -> resultBuilder.getCallConfigBuilder().setTlsClientCertPath(path.toString()));

    overrides.tlsClientKeyPath().ifPresent(
        path -> resultBuilder.getCallConfigBuilder().setTlsClientKeyPath(path.toString()));

    overrides.tlsClientOverrideAuthority()
        .ifPresent(resultBuilder.getCallConfigBuilder()::setTlsClientOverrideAuthority);

    return resultBuilder.build();
  }

  /** Returns false iff this is backed by a real config set (rather than the special empty one). */
  private boolean isEmptyConfig() {
    return !configSet.isPresent();
  }
}
