package polyglot.grpc;

import static org.mockito.Mockito.when;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.stub.StreamObserver;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import polyglot.test.TestProto;
import polyglot.test.TestProto.TestRequest;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.protobuf.Descriptors.MethodDescriptor;
import com.google.protobuf.DynamicMessage;

/** Unit tests for {@link DynamicGrpcClient}. */
public class DynamicGrpcClientTest {
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
    client.call(REQUEST, mockStreamObserver);
  }
}
