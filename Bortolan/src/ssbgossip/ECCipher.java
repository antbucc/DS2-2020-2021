package ssbgossip;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;

class ECCipher{
	
    public static byte[] sign(PrivateKey privateKey, String message) throws Exception {
        Signature signature = Signature.getInstance("SHA256withECDSA");
        signature.initSign(privateKey);
        signature.update(message.getBytes());
        return signature.sign();
    }

    public static boolean verify(PublicKey publicKey, byte[] signed, String message) throws Exception {
        Signature signature = Signature.getInstance("SHA256withECDSA");
        signature.initVerify(publicKey);
        signature.update(message.getBytes());
        return signature.verify(signed);
    }
}