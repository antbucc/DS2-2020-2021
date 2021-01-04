package progettoDS2.messages;

import java.util.List;

public class FollowMessage extends Message {

  public FollowMessage(List<Byte> from, Integer ref, byte[] who, Integer prev) {
    super(from, ref, who, prev);
  }

}
