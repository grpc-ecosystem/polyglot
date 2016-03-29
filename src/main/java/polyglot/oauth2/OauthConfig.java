package polyglot.oauth2;

/** Contains the oauth client name and secret required when talking to an oauth service. */
public class OauthConfig {
  // TODO(dino): Use autovalue for this.
  private final String clientName;
  private final String secret;
  private final String tokenEndpoint;

  public OauthConfig(String clientName, String secret, String tokenEndpoint) {
    this.clientName = clientName;
    this.secret = secret;
    this.tokenEndpoint = tokenEndpoint;
  }

  public String getClientName() {
    return clientName;
  }

  public String getSecret() {
    return secret;
  }

  public String getTokenEndpoint() {
    return tokenEndpoint;
  }
}
