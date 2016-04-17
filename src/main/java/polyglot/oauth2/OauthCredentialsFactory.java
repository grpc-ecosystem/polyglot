package polyglot.oauth2;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.auth.Credentials;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.OAuth2Credentials;
import com.google.common.base.Joiner;

import polyglot.ConfigProto.OauthConfiguration;
import polyglot.ConfigProto.OauthConfiguration.AccessTokenCredentials;
import polyglot.ConfigProto.OauthConfiguration.CredentialsCase;
import polyglot.ConfigProto.OauthConfiguration.OauthClient;

/** A utility class used to create credentials from a {@link OauthConfiguration}. */
public class OauthCredentialsFactory {
  private static final Logger logger = LoggerFactory.getLogger(OauthCredentialsFactory.class);
  private final OauthConfiguration oauthConfig;

  public OauthCredentialsFactory(OauthConfiguration oauthConfig) {
    this.oauthConfig = oauthConfig;
  }

  /** Returns a set of {@link Credentials} which can be used to authenticate requests. */
  public Credentials getCredentials() {
    if (oauthConfig.getCredentialsCase() == CredentialsCase.ACCESS_TOKEN_CREDENTIALS) {
      return createAccessTokenCredentials(oauthConfig.getAccessTokenCredentials());
    } else if (oauthConfig.getCredentialsCase() == CredentialsCase.REFRESH_TOKEN_CREDENTIALS) {
      return createRefreshTokenCredentials(oauthConfig.getRefreshTokenCredentials());
    } else {
      throw new IllegalArgumentException(
          "Unknown oauth credential type: " + oauthConfig.getCredentialsCase());
    }
  }

  private Credentials createAccessTokenCredentials(AccessTokenCredentials accessTokenCreds) {
    AccessToken accessToken = new AccessToken(
        readFile(Paths.get(accessTokenCreds.getAccessTokenPath())), null);

    logger.info("Using access token credentials");
    return new OAuth2Credentials(accessToken);
  }

  private Credentials createRefreshTokenCredentials(
      OauthConfiguration.RefreshTokenCredentials refreshTokenCreds) {
    String exchangeUrl = oauthConfig.getRefreshTokenCredentials().getTokenEndpointUrl();
    String refreshToken = readFile(
        Paths.get(oauthConfig.getRefreshTokenCredentials().getRefreshTokenPath()));
    OauthClient oauthClient = oauthConfig.getRefreshTokenCredentials().getClient();

    logger.info("Using refresh token credentials");
    return RefreshTokenCredentials.create(oauthClient, refreshToken, exchangeUrl);
  }

  private static String readFile(Path path) {
    try {
      return Joiner.on('\n').join(Files.readAllLines(path));
    } catch (IOException e) {
      throw new RuntimeException("Unable to read file: " + path.toString(), e);
    }
  }
}
