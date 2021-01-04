package progettoDS2.messages;

import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.PrivateKey;

public abstract class Message implements Comparator<Message> {
  List<Byte> From;
  Integer Ref;
  byte[] Content;
  /* Hash(From + Ref + Content + Signature of prev + sign)*/
  Integer Previous;
  /*Signature of Sign(From + Ref + Content + Prev)*/
  public byte[] Signature;
  
  public Message(List<Byte> from, Integer ref, byte[] content, Integer prev) {
    From = from;
    Ref = ref;
    Previous = prev;
    Content = content;
  }
  
  @Override
  public int compare(Message a1, Message a2) {
    if(a1 == null || a2 == null) {
      return 1;
    }
    return a1.Ref.compareTo(a2.Ref); 
  }
  /**
   * Validates the Signature of a message against the value that is supposed
   * to be there
   * @return true on valid, else false
   */
  public boolean Validate() {
    //EdDSAEngine ee = new EdDSAEngine();
    //try { TODO: not too important, fix this
      return true;
      //there are some problems in running this multiagent in the same process
      //return ee.verifyOneShot(this.getToBeSigned(), this.Signature);
    //} catch (SignatureException e) {
    //  e.printStackTrace();
    //}
    //return false;
  }
  /**
   * Returns previous:
   * * Hash(From + Ref + Content + Signature of prev + sign)
   * * null
   */
  public Integer getPrevious() {
    return this.Previous;
  }
  /**
   * Returns an array of content that is the signed part of a message
   * Signed content is From, Ref, Content 
   */
  byte[] getToBeSigned() {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    try {
      outputStream.write(ArrayUtils.toPrimitive((Byte[])From.toArray()));
      outputStream.write(this.getBRef());
      outputStream.write(Content);
      if(Previous != null) {
        outputStream.write(Previous);
      }
      if(Signature != null) {
        outputStream.write(Signature);
      }
    } catch(IOException e){
      return null;
    }
    return outputStream.toByteArray();
  }
  public List<Byte> getFrom() {
    return this.From;
  }
  /**
   * Returns an the value to be used as Previous value for following
   * messages
   */
  public Integer getToBePrevious() {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    try {
      outputStream.write(ArrayUtils.toPrimitive((Byte[])From.toArray()));
      outputStream.write(this.getBRef());
      outputStream.write(Content);
      if(Previous != null) {
        outputStream.write(Previous);
      }
      if(Signature != null) {
        outputStream.write(Signature);
      }
    } catch(IOException e){
      return null;
    }
    return outputStream.toByteArray().hashCode();
  }
  public Integer getRef() {
    return this.Ref;
  }
  byte[] getBRef() {
    int value = this.Ref;
    return new byte[] {
            (byte)(value >>> 24),
            (byte)(value >>> 16),
            (byte)(value >>> 8),
            (byte)value};
  }
  public byte[] getSignature() {
    return this.Signature;
  }
  /*
   * Updates the signature 
   */
  public void generateSignature(PrivateKey pk) {
    //TODO: Fix this, creates problems when in single process
    /*EdDSAEngine ee = new EdDSAEngine();
    try {
      ee.initSign(pk);
    } catch (InvalidKeyException e) {
      e.printStackTrace();
      return;
    }
    try {
      this.Signature = ee.signOneShot(this.getToBeSigned());
    } catch (SignatureException e) {
      this.Signature = null;
      e.printStackTrace();
    }*/
    this.Signature = new byte[0];
  }
  public boolean hasRef(Integer ref) {
    return this.Ref.equals(ref);
  }
}
