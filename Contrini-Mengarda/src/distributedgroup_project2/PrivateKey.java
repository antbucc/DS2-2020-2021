package distributedgroup_project2;

import java.util.Base64;

import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters;

/**
 * Wraps an Ed25519 private key to provide equals and hashCode implementations 
 */
public class PrivateKey {
	private final Ed25519PrivateKeyParameters key;

	// Keep the string representation of the key.
	private final String keyAsString;

	/**
	 * Constructor of the class PrivateKey.
	 * 
	 * @param key: the key expressed as AsymmetricKeyParameter.
	 */
	public PrivateKey(AsymmetricKeyParameter key) {
		this((Ed25519PrivateKeyParameters) key);
	}

	/**
	 * Constructor of the class PrivateKey.
	 * 
	 * @param key: the key expressed as Ed25519PrivateKeyParameters.
	 */
	public PrivateKey(Ed25519PrivateKeyParameters key) {
		this.key = key;
		this.keyAsString = Base64.getEncoder().encodeToString(key.getEncoded());
	}

	/**
	 * Method to retrieve the Ed25519PrivateKeyParameters key.
	 * 
	 * @return: the Ed25519PrivateKeyParameters key.
	 */
	public Ed25519PrivateKeyParameters getKey() {
		return key;
	}

	/**
	 * Method to parse the key to String.
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
	 * @param obj: the object to check against this PrivateKey;
	 * @return: true if the two keys are equal, false otherwise.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PrivateKey other = (PrivateKey) obj;
		if (keyAsString == null)
			if (other.keyAsString != null)
				return false;
		else if (!keyAsString.equals(other.keyAsString))
			return false;
		return true;
	}
	
}
