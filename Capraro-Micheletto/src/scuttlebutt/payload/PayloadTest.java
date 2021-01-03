package scuttlebutt.payload;

import static org.junit.Assert.assertEquals;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import scuttlebutt.event.Event;
import scuttlebutt.exception.SerializationException;
import scuttlebutt.serialization.Serializer;
import scuttlebutt.store.Frontier;

/**
 * JUnit test to perform testing on payload serialization
 */
public class PayloadTest {

  /**
   * Test whether a PULL payload is correctly serialized and then deserialized
   * 
   * @throws SerializationException
   */
  @Test
  public void testPayloadSerializationPull() throws SerializationException {
    Frontier frontier = new Frontier();
    Payload payload = new Payload(null, frontier);
    String serialized = Serializer.serialize(payload);
    Payload deserialized = (new Serializer<Payload>()).deserialize(serialized);
    assertEquals(payload.getFrontier(), deserialized.getFrontier());
  }


  /**
   * Test whether a PUSHPULL payload is correctly serialized and then deserialized
   * 
   * @throws SerializationException
   */
  @Test
  public void testPayloadSerializationPushPull() throws SerializationException {
    Frontier frontier = new Frontier();
    List<Event> news = new ArrayList<Event>();
    Payload payload = new Payload(null, frontier, news);
    String serialized = Serializer.serialize(payload);
    Payload deserialized = (new Serializer<Payload>()).deserialize(serialized);
    assertEquals(payload.getFrontier(), deserialized.getFrontier());
    assertEquals(payload.getNews(), deserialized.getNews());
  }


  /**
   * Test whether a PUSH payload is correctly serialized and then deserialized
   * 
   * @throws SerializationException
   */
  @Test
  public void testPayloadSerializationPush() throws SerializationException {
    List<Event> news = new ArrayList<Event>();
    Payload payload = new Payload(null, news);
    String serialized = Serializer.serialize(payload);
    Payload deserialized = (new Serializer<Payload>()).deserialize(serialized);
    assertEquals(payload.getNews(), deserialized.getNews());
  }


  /**
   * Test whether a string that does not derive from a serialization properly throws a serialization
   * exception
   * 
   * @throws SerializationException
   */
  @Test(expected = SerializationException.class)
  public void testBadDeserialization() throws SerializationException {
    (new Serializer<Payload>()).deserialize("aaa");
  }

}
