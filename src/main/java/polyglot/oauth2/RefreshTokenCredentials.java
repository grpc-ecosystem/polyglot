package polyglot.oauth2;

import java.io.IOException;

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
  private final String oauthClient;
  private final String oauthSecret;

  public RefreshTokenCredentials(
      String endpoint, String refreshTokenSecret, String oauthClient, String oauthSecret) {
    this.refreshTokenSecret = refreshTokenSecret;
    this.endpoint = endpoint;
    this.oauthClient = oauthClient;
    this.oauthSecret = oauthSecret;
  }

  @Override
  public AccessToken refreshAccessToken() throws IOException {
    RefreshTokenRequest refreshRequest = new RefreshTokenRequest(
        new NetHttpTransport(),
        new JacksonFactory(),
        new GenericUrl(endpoint),
        refreshTokenSecret);
    refreshRequest.setClientAuthentication(new BasicAuthentication(oauthClient, oauthSecret));
    TokenResponse refreshResponse = refreshRequest.execute();
    return new AccessToken(refreshResponse.getAccessToken(), null);
  }
}
