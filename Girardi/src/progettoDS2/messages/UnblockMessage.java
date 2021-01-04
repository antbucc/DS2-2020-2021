package progettoDS2.messages;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

public class UnblockMessage extends Message {

  public UnblockMessage(List<Byte> from, Integer ref, byte[] who, Integer prev) {
    super(from, ref, who, prev);
  }
  public List<Byte> getTarget() {
    return Arrays.asList(ArrayUtils.toObject(this.Content));
  }
}
