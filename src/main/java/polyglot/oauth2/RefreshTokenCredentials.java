package polyglot.oauth2;

import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;

import com.google.api.client.auth.oauth2.RefreshTokenRequest;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.http.BasicAuthentication;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.OAuth2Credentials;

public class RefreshTokenCredentials extends OAuth2Credentials {
  private final String refreshTokenSecret;
  private final String endpoint;
  private final ClientAuth clientAuth;
  private final Clock clock;

  public RefreshTokenCredentials(
      String endpoint, String refreshTokenSecret, ClientAuth clientAuth, Clock clock) {
    this.refreshTokenSecret = refreshTokenSecret;
    this.endpoint = endpoint;
    this.clientAuth = clientAuth;
    this.clock = clock;
  }

  @Override
  public AccessToken refreshAccessToken() throws IOException {
    RefreshTokenRequest refreshRequest = new RefreshTokenRequest(
        new NetHttpTransport(),
        new JacksonFactory(),
        new GenericUrl(endpoint),
        refreshTokenSecret);
    refreshRequest.setClientAuthentication(
        new BasicAuthentication(clientAuth.getClientName(), clientAuth.getSecret()));
    TokenResponse refreshResponse = refreshRequest.execute();
    return new AccessToken(
        refreshResponse.getAccessToken(),
        computeExpirtyDate(refreshResponse.getExpiresInSeconds()));
  }

  public Date computeExpirtyDate(long expiresInSeconds) {
    Instant expiresAtSecond = clock.instant().plusSeconds(expiresInSeconds);
    return Date.from(expiresAtSecond);
  }
}
