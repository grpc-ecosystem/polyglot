package polyglot;

public class GrpcClientException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  public GrpcClientException(String message, Throwable cause) {
    super(message, cause);
  }
}
