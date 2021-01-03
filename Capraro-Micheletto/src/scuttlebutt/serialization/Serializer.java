package scuttlebutt.serialization;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Base64;
import scuttlebutt.exception.SerializationException;

/**
 * This class takes care of the serialization of a serializable object
 */
public class Serializer<T> {

  /**
   * Serialize a serializable object
   * 
   * @param serializable: the object to be serialized
   * @return the string that corresponds to the serialized object
   * @throws SerializationException
   */
  public static String serialize(Serializable serializable) throws SerializationException {
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    ObjectOutputStream outputStream;
    try {
      outputStream = new ObjectOutputStream(byteArrayOutputStream);
      outputStream.writeObject(serializable);
      outputStream.flush();
      return Base64.getEncoder().encodeToString(byteArrayOutputStream.toByteArray());
    } catch (IOException e) {
      e.printStackTrace();
      throw new SerializationException(e);
    }
  }

  /**
   * Deserialize a string that corresponds to a serialized object. Throws a SerializationException
   * if the string does not correspond to a serialized object
   * 
   * @param serialized: the string representing the serialized object
   * @return the object deserialized
   * @throws SerializationException
   */
  public T deserialize(String serialized) throws SerializationException {
    if (serialized.equals("")) {
      return null;
    }
    byte[] bytes = Base64.getDecoder().decode(serialized);
    ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
    ObjectInputStream inputStream;
    try {
      inputStream = new ObjectInputStream(byteArrayInputStream);
      return (T) inputStream.readObject();
    } catch (EOFException e) {
      throw new SerializationException(e);
    } catch (IOException | ClassNotFoundException e) {
      e.printStackTrace();
      throw new SerializationException(e);
    }
  }
}
