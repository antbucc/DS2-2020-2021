package progettoDS2.messages;

import java.util.List;

public class UnfollowMessage extends Message {

  public UnfollowMessage(List<Byte> from, Integer ref, byte[] who, Integer prev) {
    super(from, ref, who, prev);
  }

}
