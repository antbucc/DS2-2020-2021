package progettoDS2.messages;

import java.util.List;
import java.util.Map;

import progettoDS2.actors.Log;

/**
 * This message shars the current local log set,
 * it should be compressed etc. but this is just
 * a mockup
 */
public class SyncLogsMessage extends Message {

  Map<byte[], Log> storage;
  public SyncLogsMessage(List<Byte> from, Integer ref, Map<byte[], Log> local, Integer prev) {
    super(from, ref, new byte[0], prev);
    storage = local;
  }

}
