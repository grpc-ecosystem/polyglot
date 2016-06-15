package me.dinowernli.grpc.polyglot.oauth2;

import static com.google.common.truth.Truth.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.auth.oauth2.OAuth2Credentials;

import me.dinowernli.grpc.polyglot.oauth2.OauthCredentialsFactory;
import me.dinowernli.grpc.polyglot.oauth2.RefreshTokenCredentials;
import polyglot.ConfigProto.OauthConfiguration;
import polyglot.ConfigProto.OauthConfiguration.AccessTokenCredentials;

/** Unit tests for {@link OauthCredentialsFactory}. */
public class OauthCredentialsFactoryTest {
  private Path tempFile;

  @Before
  public void setUp() throws Throwable {
    tempFile = Files.createTempFile("token", "txt");
  }

  @After
  public void tearDown() throws Throwable {
    Files.delete(tempFile);
  }

  @Test
  public void producesRefreshTokenCredentials() {
    OauthCredentialsFactory factory = new OauthCredentialsFactory(OauthConfiguration.newBuilder()
        .setRefreshTokenCredentials(OauthConfiguration.RefreshTokenCredentials.newBuilder()
            .setRefreshTokenPath(tempFile.toString()))
        .build());
    assertThat(factory.getCredentials()).isInstanceOf(RefreshTokenCredentials.class);
  }

  @Test
  public void producesAccessTokenCredentials() {
    OauthCredentialsFactory factory = new OauthCredentialsFactory(OauthConfiguration.newBuilder()
        .setAccessTokenCredentials(AccessTokenCredentials.newBuilder()
            .setAccessTokenPath(tempFile.toString()))
        .build());
    assertThat(factory.getCredentials()).isInstanceOf(OAuth2Credentials.class);
  }
}
