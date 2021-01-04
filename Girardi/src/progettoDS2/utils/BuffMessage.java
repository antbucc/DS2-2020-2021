package progettoDS2.utils;

import java.util.List;

public class BuffMessage {
  public List<Byte> Src;
  public List<Byte> Dst;
  public Object Msg;
  public boolean Lost;
  public Integer DeliverIn;
  public BuffMessage(List<Byte> src, List<Byte> dst, Object msg, Integer deliverIn) {
    Src = src;
    Dst = dst;
    Msg = msg;
    DeliverIn = deliverIn;
    Lost = false;
  }
}
