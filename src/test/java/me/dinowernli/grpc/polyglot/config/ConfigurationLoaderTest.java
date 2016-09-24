package me.dinowernli.grpc.polyglot.config;

import java.nio.file.Paths;
import java.util.Optional;

import com.google.common.collect.ImmutableList;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import polyglot.ConfigProto.Configuration;
import polyglot.ConfigProto.ConfigurationSet;
import polyglot.ConfigProto.OutputConfiguration.Destination;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.when;

/** Unit tests for {@link ConfigurationLoader}. */
public class ConfigurationLoaderTest {
  @Rule public TemporaryFolder tempDirectory = new TemporaryFolder();
  @Rule public MockitoRule mockitoJunitRule = MockitoJUnit.rule();
  @Mock private CommandLineArgs mockOverrides;

  private String storedHomeProperty;

  @Before
  public void setUp() {
    storedHomeProperty = System.getProperty("user.home");
    System.setProperty("user.home", tempDirectory.getRoot().getAbsolutePath());
  }

  @After
  public void tearDown() {
    System.setProperty("user.home", storedHomeProperty);
  }

  @Test
  public void loadsDefaultConfig() {
    Configuration defaultConfig =
        ConfigurationLoader.forDefaultConfigSet().getDefaultConfiguration();
    assertThat(defaultConfig).isEqualTo(Configuration.getDefaultInstance());

    assertThat(defaultConfig.getCallConfig().getUseTls()).isFalse();
    assertThat(defaultConfig.getOutputConfig().getDestination()).isEqualTo(Destination.STDOUT);
  }

  @Test(expected = IllegalStateException.class)
  public void throwsIfAskedToLoadNamedFromDefaultSet() {
    ConfigurationLoader.forDefaultConfigSet().getNamedConfiguration("asdf");
  }

  @Test(expected = IllegalArgumentException.class)
  public void throwsIfNamedConfigMissing() {
    ConfigurationLoader.forConfigSet(ConfigurationSet.getDefaultInstance())
        .getNamedConfiguration("asfd");
  }

  @Test
  public void loadsNamedConfig() {
    ConfigurationLoader loader = ConfigurationLoader.forConfigSet(ConfigurationSet.newBuilder()
        .addConfigurations(namedConfig("foo"))
        .addConfigurations(namedConfig("bar"))
        .build());
    assertThat(loader.getNamedConfiguration("foo").getName()).isEqualTo("foo");
  }

  @Test
  public void appliesOverrides() {
    when(mockOverrides.useTls()).thenReturn(Optional.of(true));
    when(mockOverrides.outputFilePath()).thenReturn(Optional.of(Paths.get("asdf")));
    when(mockOverrides.additionalProtocIncludes()).thenReturn(ImmutableList.of(Paths.get(".")));
    when(mockOverrides.protoDiscoveryRoot()).thenReturn(Optional.of(Paths.get(".")));
    when(mockOverrides.getRpcDeadlineMs()).thenReturn(Optional.of(25));
    when(mockOverrides.tlsCaCertPath()).thenReturn(Optional.of(Paths.get("asdf")));

    Configuration config = ConfigurationLoader
        .forDefaultConfigSet()
        .withOverrides(mockOverrides)
        .getDefaultConfiguration();

    assertThat(config.getCallConfig().getUseTls()).isTrue();
    assertThat(config.getOutputConfig().getDestination()).isEqualTo(Destination.FILE);
    assertThat(config.getCallConfig().getDeadlineMs()).isEqualTo(25);
    assertThat(config.getCallConfig().getTlsCaCertPath()).isNotEmpty();
  }

  private static Configuration namedConfig(String name) {
    return Configuration.newBuilder()
        .setName(name)
        .build();
  }
}
