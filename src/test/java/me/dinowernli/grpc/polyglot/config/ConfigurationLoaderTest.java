package me.dinowernli.grpc.polyglot.config;

import java.net.MalformedURLException;
import java.nio.file.Paths;
import java.util.Optional;
import java.net.URL;

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

import javax.swing.text.html.Option;

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
  public void appliesOverridesWithRefreshToken() {
    when(mockOverrides.useTls()).thenReturn(Optional.of(true));
    when(mockOverrides.outputFilePath()).thenReturn(Optional.of(Paths.get("asdf")));
    when(mockOverrides.additionalProtocIncludes()).thenReturn(ImmutableList.of(Paths.get(".")));
    when(mockOverrides.protoDiscoveryRoot()).thenReturn(Optional.of(Paths.get(".")));
    when(mockOverrides.getRpcDeadlineMs()).thenReturn(Optional.of(25));
    when(mockOverrides.tlsCaCertPath()).thenReturn(Optional.of(Paths.get("asdf")));
    when(mockOverrides.tlsClientCertPath()).thenReturn(Optional.of(Paths.get("client_cert")));
    when(mockOverrides.tlsClientKeyPath()).thenReturn(Optional.of(Paths.get("client_key")));
    when(mockOverrides.tlsClientOverrideAuthority()).thenReturn(Optional.of("override_authority"));
    when(mockOverrides.oauthRefreshTokenEndpointUrl()).thenReturn(Optional.of(getTestUrl("https://github.com/grpc-ecosystem/polyglot")));
    when(mockOverrides.oauthClientId()).thenReturn(Optional.of("id"));
    when(mockOverrides.oauthClientSecret()).thenReturn(Optional.of("secret"));
    when(mockOverrides.oauthRefreshTokenPath()).thenReturn(Optional.of(Paths.get("asdf")));
    when(mockOverrides.oauthAccessTokenPath()).thenReturn(Optional.empty());

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
    assertThat(callConfig.getDeadlineMs()).isEqualTo(25);
    assertThat(callConfig.getOauthConfig().getRefreshTokenCredentials().getTokenEndpointUrl())
            .isEqualTo("https://github.com/grpc-ecosystem/polyglot");
    assertThat(callConfig.getOauthConfig().getRefreshTokenCredentials().getClient().getId()).isEqualTo("id");
    assertThat(callConfig.getOauthConfig().getRefreshTokenCredentials().getClient().getSecret()).isEqualTo("secret");
    assertThat(callConfig.getOauthConfig().getRefreshTokenCredentials().getRefreshTokenPath()).isNotEmpty();
    assertThat(callConfig.getOauthConfig().getAccessTokenCredentials().getAccessTokenPath()).isEmpty();
  }

  @Test
  public void appliesOverridesWithAccessToken() {
    when(mockOverrides.useTls()).thenReturn(Optional.of(true));
    when(mockOverrides.outputFilePath()).thenReturn(Optional.of(Paths.get("asdf")));
    when(mockOverrides.additionalProtocIncludes()).thenReturn(ImmutableList.of(Paths.get(".")));
    when(mockOverrides.protoDiscoveryRoot()).thenReturn(Optional.of(Paths.get(".")));
    when(mockOverrides.getRpcDeadlineMs()).thenReturn(Optional.of(25));
    when(mockOverrides.tlsCaCertPath()).thenReturn(Optional.of(Paths.get("asdf")));
    when(mockOverrides.tlsClientCertPath()).thenReturn(Optional.of(Paths.get("client_cert")));
    when(mockOverrides.tlsClientKeyPath()).thenReturn(Optional.of(Paths.get("client_key")));
    when(mockOverrides.tlsClientOverrideAuthority()).thenReturn(Optional.of("override_authority"));
    when(mockOverrides.oauthRefreshTokenEndpointUrl()).thenReturn(Optional.of(getTestUrl("https://github.com/grpc-ecosystem/polyglot")));
    when(mockOverrides.oauthClientId()).thenReturn(Optional.of("id"));
    when(mockOverrides.oauthClientSecret()).thenReturn(Optional.of("secret"));
    when(mockOverrides.oauthRefreshTokenPath()).thenReturn(Optional.of(Paths.get("asdf")));
    when(mockOverrides.oauthAccessTokenPath()).thenReturn(Optional.of(Paths.get("asdf")));

    Configuration config = ConfigurationLoader
      .forDefaultConfigSet()
      .withOverrides(mockOverrides)
      .getDefaultConfiguration();

    CallConfiguration callConfig = config.getCallConfig();
    assertThat(callConfig.getUseTls()).isTrue();
    assertThat(config.getOutputConfig().getDestination()).isEqualTo(Destination.FILE);
    assertThat(callConfig.getDeadlineMs()).isEqualTo(25);
    assertThat(callConfig.getTlsCaCertPath()).isNotEmpty();
    assertThat(callConfig.getTlsClientCertPath()).isEqualTo("client_cert");
    assertThat(callConfig.getTlsClientKeyPath()).isEqualTo("client_key");
    assertThat(callConfig.getTlsClientOverrideAuthority()).isEqualTo("override_authority");
    assertThat(callConfig.getDeadlineMs()).isEqualTo(25);
    // Setting the access token path will unset all of the refresh token properties (due to the oneof semantics)
    assertThat(callConfig.getOauthConfig().getRefreshTokenCredentials().getTokenEndpointUrl()).isEmpty();
    assertThat(callConfig.getOauthConfig().getRefreshTokenCredentials().getClient().getId()).isEmpty();
    assertThat(callConfig.getOauthConfig().getRefreshTokenCredentials().getClient().getSecret()).isEmpty();
    assertThat(callConfig.getOauthConfig().getRefreshTokenCredentials().getRefreshTokenPath()).isEmpty();
    assertThat(callConfig.getOauthConfig().getAccessTokenCredentials().getAccessTokenPath()).isNotEmpty();
  }

  private static Configuration namedConfig(String name) {
    return Configuration.newBuilder()
      .setName(name)
      .build();
  }
  private static URL getTestUrl(String testUrl) {
    try {
      return new URL(testUrl);
    } catch (MalformedURLException mUrlE) {
      throw new RuntimeException();
    }
  }
}
