package polyglot.oauth2;

/** Contains the oauth client name and secret required when talking to an oauth service. */
public class ClientAuth {
  // TODO(dino): Use autovalue for this.
  private final String clientName;
  private final String secret;

  public ClientAuth(String clientName, String secret) {
    this.clientName = clientName;
    this.secret = secret;
  }

  public String getClientName() {
    return clientName;
  }

  public String getSecret() {
    return secret;
  }
}
