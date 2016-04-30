package polyglot.io.testing;

import com.google.protobuf.DynamicMessage;

import polyglot.test.TestProto.TestRequest;

/** Test data used for unit tests of the io package. */
public class TestData {
  public static final DynamicMessage REQUEST = DynamicMessage.newBuilder(
      TestRequest.newBuilder()
          .setMessage("some message")
          .build())
      .build();

  public static final String REQUEST_JSON = new StringBuilder()
      .append("{\n")
      .append("  \"message\": \"some message\"\n")
      .append("}\n")
      .append("\n").toString();
}
