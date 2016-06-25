package me.dinowernli.grpc.polyglot.grpc;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.protobuf.Descriptors.MethodDescriptor;
import com.google.protobuf.DynamicMessage;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.stub.StreamObserver;
import polyglot.test.TestProto;
import polyglot.test.TestProto.TestRequest;

/** Unit tests for {@link DynamicGrpcClient}. */
public class DynamicGrpcClientTest {
  private static final CallOptions CALL_OPTIONS = CallOptions.DEFAULT.withDeadlineNanoTime(1234L);

  private static final MethodDescriptor UNARY_METHOD = TestProto
      .getDescriptor()
      .findServiceByName("TestService")
      .findMethodByName("TestMethod");

  private static final MethodDescriptor SERVER_STREAMING_METHOD = TestProto
      .getDescriptor()
      .findServiceByName("TestService")
      .findMethodByName("TestMethodStream");

  private static final MethodDescriptor BIDI_STREAMING_METHOD = TestProto
      .getDescriptor()
      .findServiceByName("TestService")
      .findMethodByName("TestMethodBidi");

  private static final DynamicMessage REQUEST = DynamicMessage.newBuilder(
      TestRequest.newBuilder()
          .setMessage("some message")
          .build())
      .build();

  @Rule public MockitoRule mockitoJunitRule = MockitoJUnit.rule();
  @Mock private Channel mockChannel;
  @Mock private ListeningExecutorService mockExecutor;
  @Mock private StreamObserver<DynamicMessage> mockStreamObserver;
  @Mock private ClientCall<DynamicMessage, DynamicMessage> mockClientCall;
  @Captor private ArgumentCaptor<CallOptions> callOptionsCaptor;

  private DynamicGrpcClient client;

  @Before
  public void setUp() {
    when(mockChannel.newCall(
        Matchers.<io.grpc.MethodDescriptor<DynamicMessage, DynamicMessage>>any(),
        Matchers.any()))
            .thenReturn(mockClientCall);
  }

  @Test
  public void unaryMethodCall() {
    client = new DynamicGrpcClient(UNARY_METHOD, mockChannel, mockExecutor);
    client.call(ImmutableList.of(REQUEST), mockStreamObserver, CALL_OPTIONS);
    // No crash.
  }

  @Test
  public void passesCallOptions() {
    client = new DynamicGrpcClient(UNARY_METHOD, mockChannel, mockExecutor);
    client.call(ImmutableList.of(REQUEST), mockStreamObserver, CALL_OPTIONS);

    verify(mockChannel).newCall(
        Matchers.<io.grpc.MethodDescriptor<DynamicMessage, DynamicMessage>>any(),
        callOptionsCaptor.capture());
    assertThat(callOptionsCaptor.getValue()).isEqualTo(CALL_OPTIONS);
  }

  // TODO(dino): Add some more tests for the streaming and bidi cases.
}
