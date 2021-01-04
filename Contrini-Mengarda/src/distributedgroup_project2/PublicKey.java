package distributedgroup_project2;

import java.util.Base64;

import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters;

/**
 * Wraps an Ed25519 public key to provide equals and hashCode implementations 
 */
public class PublicKey {
	private final Ed25519PublicKeyParameters key;

	// Keep the string representation of the key.
	private final String keyAsString;

	/**
	 * Constructor of the class PublicKey.
	 * 
	 * @param key: the key expressed as AsymmetricKeyParameter.
	 */
	public PublicKey(AsymmetricKeyParameter key) {
		this((Ed25519PublicKeyParameters) key);
	}

	/**
	 * Constructor of the class PublicKey.
	 * 
	 * @param key: the key expressed as Ed25519PublicKeyParameters.
	 */
	public PublicKey(Ed25519PublicKeyParameters key) {
		this.key = key;
		this.keyAsString = Base64.getEncoder().encodeToString(key.getEncoded());
	}

	/**
	 * Method to retrieve the Ed25519PublicKeyParameters key.
	 * 
	 * @return: the Ed25519PublicKeyParameters key.
	 */
	public Ed25519PublicKeyParameters getKey() {
		return key;
	}

	/**
	 * Method to return the key represented as as String.
	 * 
	 * @return: the string representation of the key.
	 */
	@Override
	public String toString() {
		return keyAsString;
	}

	/**
	 * Method to retrieve the hashcode of the key.
	 * We only consider the key as String, because the original key does not provide an hashCode method.
	 * 
	 * @return: the hashcode of the key.
	 */
	@Override
	public int hashCode() {
		return keyAsString.hashCode();
	}

	/**
	 * Method to check whether to keys are equal.
	 * We only consider the key as String, because the original key does not provide an equals method.
	 * 
	 * @param obj: the object to check against this PublicKey;
	 * @return: true if the two keys are equal, false otherwise.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;			
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;			
		}
		PublicKey other = (PublicKey) obj;
		if (keyAsString == null) {
			if (other.keyAsString != null) {
				return false;				
			}
		} else if (!keyAsString.equals(other.keyAsString)) {
			return false;			
		}
		return true;
	}
}
