package progettoDS2.messages;

import java.security.PublicKey;
import java.util.List;

public class PostMessage extends Message {

  public PostMessage(List<Byte> from, Integer ref, String content, Integer prev) {
    super(from, ref, content.getBytes(), prev);
  }

}
