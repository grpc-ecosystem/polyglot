package polyglot;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import com.google.api.client.util.Joiner;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.protobuf.util.JsonFormat;

import polyglot.ConfigProto.Configuration;
import polyglot.ConfigProto.ConfigurationSet;

/** A utility which manipulating and reading a single {@link ConfigurationSet}. */
public class ConfigurationLoader {
  private static final String DEFAULT_FILE_NAME = "config.pb.json";
  private static final String DEFAULT_LOCATION = ".polyglot";

  /** Empty iff we are loading the empty config. */
  private final Optional<ConfigurationSet> configSet;

  /**
   * Returns a {@link ConfigurationLoader} which points to a default location, and falls back to an
   * empty configuration set if that location doesn't exist.
   */
  public static ConfigurationLoader forDefaultFile() {
    String homeDirectory = System.getProperty("user.home");
    Path defaultLocation = Paths.get(homeDirectory, DEFAULT_LOCATION, DEFAULT_FILE_NAME);
    if (Files.exists(defaultLocation)) {
      return ConfigurationLoader.forFile(defaultLocation);
    } else {
      return new ConfigurationLoader(Optional.empty());
    }
  }

  /** Constructs a {@link ConfigurationLoader} from an explicit {@ConfigurationSet}. */
  public static ConfigurationLoader forConfigSet(ConfigurationSet configSet) {
    return new ConfigurationLoader(Optional.of(configSet));
  }

  /** Returns a loader which attempts to parser the supplied file as Json. */
  public static ConfigurationLoader forFile(Path configFile) {
    try {
      ConfigurationSet.Builder configSetBuilder = ConfigurationSet.newBuilder();
      String fileContent = Joiner.on('\n').join(Files.readAllLines(configFile));
      JsonFormat.parser().merge(fileContent, configSetBuilder);
      return new ConfigurationLoader(Optional.of(configSetBuilder.build()));
    } catch (IOException e) {
      throw new RuntimeException("Unable to read config file: " + configFile.toString(), e);
    }
  }

  @VisibleForTesting
  ConfigurationLoader(Optional<ConfigurationSet> configSet) {
    this.configSet = configSet;
  }

  /** Returns the default configuration from the loaded configuration set. */
  public Configuration getDefaultConfiguration() {
    if (isEmptyConfig()) {
      return Configuration.getDefaultInstance();
    }
    if (configSet.get().getConfigurationsList().isEmpty()) {
      throw new IllegalStateException("No configs present in config set");
    }
    return configSet.get().getConfigurations(0);
  }

  /** Returns a config with the supplied name and throws if no such config is found. */
  public Configuration getNamedConfiguration(String name) {
    Preconditions.checkState(!isEmptyConfig(), "Cannot load named config with a config set");
    return configSet.get().getConfigurationsList().stream()
        .filter(config -> config.getName().equals(name))
        .findAny()
        .orElseThrow(() -> new IllegalArgumentException("Could not find named config: " + name));
  }

  /** Returns true the config set is backed by a real file. */
  private boolean isEmptyConfig() {
    return !configSet.isPresent();
  }
}
