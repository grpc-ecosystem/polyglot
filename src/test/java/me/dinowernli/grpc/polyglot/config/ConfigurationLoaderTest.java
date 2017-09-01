package me.dinowernli.grpc.polyglot.config;

import java.nio.file.Paths;
import java.util.Optional;

import com.google.common.collect.ImmutableList;
import me.dinowernli.junit.TestClass;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import polyglot.ConfigProto.CallConfiguration;
import polyglot.ConfigProto.Configuration;
import polyglot.ConfigProto.ConfigurationSet;
import polyglot.ConfigProto.OutputConfiguration.Destination;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.when;

/** Unit tests for {@link ConfigurationLoader}. */
@TestClass
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
    when(mockOverrides.tlsClientCertPath()).thenReturn(Optional.of(Paths.get("client_cert")));
    when(mockOverrides.tlsClientKeyPath()).thenReturn(Optional.of(Paths.get("client_key")));
    when(mockOverrides.tlsClientOverrideAuthority()).thenReturn(Optional.of("override_authority"));

    Configuration config = ConfigurationLoader
        .forDefaultConfigSet()
        .withOverrides(mockOverrides)
        .getDefaultConfiguration();

    assertThat(config.getOutputConfig().getDestination()).isEqualTo(Destination.FILE);

    CallConfiguration callConfig = config.getCallConfig();
    assertThat(callConfig.getUseTls()).isTrue();
    assertThat(callConfig.getDeadlineMs()).isEqualTo(25);
    assertThat(callConfig.getTlsCaCertPath()).isNotEmpty();
    assertThat(callConfig.getTlsClientCertPath()).isEqualTo("client_cert");
    assertThat(callConfig.getTlsClientKeyPath()).isEqualTo("client_key");
    assertThat(callConfig.getTlsClientOverrideAuthority()).isEqualTo("override_authority");
  }

  private static Configuration namedConfig(String name) {
    return Configuration.newBuilder()
        .setName(name)
        .build();
  }
}
