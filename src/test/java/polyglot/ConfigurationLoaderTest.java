package polyglot;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;

import polyglot.ConfigProto.Configuration;
import polyglot.ConfigProto.ConfigurationSet;

/** Unit tests for {@link ConfigurationLoader}. */
public class ConfigurationLoaderTest {
  @Test
  public void loadsDefaultConfig() {
    assertThat(ConfigurationLoader.forDefaultFile().getDefaultConfiguration())
        .isEqualTo(Configuration.getDefaultInstance());
  }

  @Test(expected = IllegalStateException.class)
  public void throwsIfAskedToLoadNamedFromDefaultSet() {
    ConfigurationLoader.forDefaultFile().getNamedConfiguration("asdf");
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

  private static Configuration namedConfig(String name) {
    return Configuration.newBuilder()
        .setName(name)
        .build();
  }
}
