package me.dinowernli.grpc.polyglot.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
  private final Optional<CommandLineArgs> overrides;

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
  ConfigurationLoader(Optional<ConfigurationSet> configSet, Optional<CommandLineArgs> overrides) {
    this.configSet = configSet;
    this.overrides = overrides;
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
    if (!overrides.isPresent()) {
      return configuration;
    }

    Configuration.Builder resultBuilder = configuration.toBuilder();
    if (overrides.get().useTls().isPresent()) {
      resultBuilder.getCallConfigBuilder().setUseTls(overrides.get().useTls().get());
    }
    if (overrides.get().outputFilePath().isPresent()) {
      resultBuilder.getOutputConfigBuilder().setDestination(Destination.FILE);
      resultBuilder.getOutputConfigBuilder().setFilePath(
          overrides.get().outputFilePath().get().toString());
    }
    if (!overrides.get().additionalProtocIncludes().isEmpty()) {
      List<String> additionalIncludes = new ArrayList<>();
      for (Path path : overrides.get().additionalProtocIncludes()) {
        additionalIncludes.add(path.toString());
      }
      resultBuilder.getProtoConfigBuilder().addAllIncludePaths(additionalIncludes);
    }
    if (overrides.get().protoDiscoveryRoot().isPresent()) {
      resultBuilder.getProtoConfigBuilder().setProtoDiscoveryRoot(
          overrides.get().protoDiscoveryRoot().get().toString());
    }
    if (overrides.get().getRpcDeadlineMs().isPresent()) {
      resultBuilder.getCallConfigBuilder().setDeadlineMs(overrides.get().getRpcDeadlineMs().get());
    }
    if (overrides.get().tlsCaCertPath().isPresent()) {
      resultBuilder.getCallConfigBuilder().setTlsCaCertPath(
        overrides.get().tlsCaCertPath().get().toString());
    }
    if (overrides.get().tlsClientCertPath().isPresent()) {
      resultBuilder.getCallConfigBuilder().setTlsClientCertPath(
        overrides.get().tlsClientCertPath().get().toString());
    }
    if (overrides.get().tlsClientKeyPath().isPresent()) {
      resultBuilder.getCallConfigBuilder().setTlsClientKeyPath(
        overrides.get().tlsClientKeyPath().get().toString());
    }
    if (overrides.get().tlsClientOverrideAuthority().isPresent()) {
      resultBuilder.getCallConfigBuilder().setTlsClientOverrideAuthority(
          overrides.get().tlsClientOverrideAuthority().get());
    }
    if (overrides.get().oauthRefreshTokenEndpointUrl().isPresent()) {
      resultBuilder.getCallConfigBuilder().getOauthConfigBuilder().getRefreshTokenCredentialsBuilder()
              .setTokenEndpointUrl(overrides.get().oauthRefreshTokenEndpointUrl().get().toString());
    }
    if (overrides.get().oauthClientId().isPresent()) {
      resultBuilder.getCallConfigBuilder().getOauthConfigBuilder().getRefreshTokenCredentialsBuilder()
              .getClientBuilder().setId(overrides.get().oauthClientId().get());
    }
    if (overrides.get().oauthClientSecret().isPresent()) {
      resultBuilder.getCallConfigBuilder().getOauthConfigBuilder().getRefreshTokenCredentialsBuilder()
              .getClientBuilder().setSecret(overrides.get().oauthClientSecret().get());
    }
    if (overrides.get().oauthRefreshTokenPath().isPresent()) {
      resultBuilder.getCallConfigBuilder().getOauthConfigBuilder().getRefreshTokenCredentialsBuilder()
              .setRefreshTokenPath(overrides.get().oauthRefreshTokenPath().get().toString());
    }
    // Note the ordering of setting these fields is important. Oauth configuration has a oneof field, corresponding
    // to access or refresh tokens. We want access tokens to take precedence, setting this field last will ensure this
    // occurs. See https://developers.google.com/protocol-buffers/docs/proto#oneof
    if (overrides.get().oauthAccessTokenPath().isPresent()) {
      resultBuilder.getCallConfigBuilder().getOauthConfigBuilder().getAccessTokenCredentialsBuilder()
              .setAccessTokenPath(overrides.get().oauthAccessTokenPath().get().toString());
    }
    return resultBuilder.build();
  }

  /** Returns false iff this is backed by a real config set (rather than the special empty one). */
  private boolean isEmptyConfig() {
    return !configSet.isPresent();
  }
}
