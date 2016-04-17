package polyglot.io;

import static com.google.common.truth.Truth.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;

import org.junit.Before;
import org.junit.Test;

import com.google.protobuf.util.JsonFormat;

import polyglot.io.testing.Testdata;

/** Unit tests for {@link FileMessageWriter}. */
public class FileMessageWriterTest {
  private ByteArrayOutputStream outputStream;
  private FileMessageWriter writer;

  @Before
  public void setUp() {
    outputStream = new ByteArrayOutputStream();
    writer = new FileMessageWriter(JsonFormat.printer(), new PrintWriter(outputStream));
  }

  @Test
  public void writesSingleMessage() {
    writer.onNext(Testdata.REQUEST);
    writer.onCompleted();
    String result = outputStream.toString();
    assertThat(result).isEqualTo(Testdata.REQUEST_JSON);
  }

  @Test
  public void writesMultipleMessages() {
    writer.onNext(Testdata.REQUEST);
    writer.onNext(Testdata.REQUEST);
    writer.onNext(Testdata.REQUEST);
    writer.onCompleted();
    String result = outputStream.toString();
    assertThat(result)
        .isEqualTo(Testdata.REQUEST_JSON + Testdata.REQUEST_JSON + Testdata.REQUEST_JSON);
  }
}
