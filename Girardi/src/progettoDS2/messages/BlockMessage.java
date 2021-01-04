package progettoDS2.messages;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

public class BlockMessage extends Message {

  public BlockMessage(List<Byte> from, Integer ref, byte[] who, Integer prev) {
    super(from, ref, who, prev);
    // TODO Auto-generated constructor stub
  }
  public List<Byte> getTarget() {
    return Arrays.asList(ArrayUtils.toObject(this.Content));
  }

}
