package me.dinowernli.grpc.polyglot.io.testing;

import com.google.common.collect.ImmutableList;
import com.google.protobuf.DynamicMessage;
import polyglot.test.TestProto.TestRequest;

/** Test data used for unit tests of the io package. */
public class TestData {
  public static final DynamicMessage REQUEST = makeProto("some message");

  public static final ImmutableList<DynamicMessage> REQUESTS_MULTI = ImmutableList.of(
      makeProto("message!"),
      makeProto("more message!"),
      makeProto("even more message"));

  public static final DynamicMessage REQUEST_WITH_PRIMITIVE = DynamicMessage.newBuilder(
      TestRequest.newBuilder()
          .setMessage("some message")
          .setNumber(3)
          .build())
      .build();

  private static DynamicMessage makeProto(String content) {
    return DynamicMessage.newBuilder(
        TestRequest.newBuilder()
            .setMessage(content)
            .build())
        .build();
  }
}
