package Database;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

public class PBKDF2WithHmacSHA512{

    //attributes
    private final int iterationNum = 1000000;
    private final int keyLength = 512;

    //get algorithm name
    private final String algorithmName = PBKDF2WithHmacSHA512.class.getSimpleName();;

    //Hashes a given String with the given salt
    public byte[] hash(String password, byte[] salt) throws NoSuchAlgorithmException, InvalidKeySpecException {
        final KeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt, iterationNum, keyLength);
        final SecretKeyFactory secretKeyfactory = SecretKeyFactory.getInstance(algorithmName);
        return secretKeyfactory.generateSecret(keySpec).getEncoded();

    }
    //Generates a Salt
    public byte[] salt() throws NoSuchAlgorithmException {
        final byte[] salt = new byte[16];
        SecureRandom.getInstance("SHA1PRNG").nextBytes(salt);
        return salt;
    }



}
