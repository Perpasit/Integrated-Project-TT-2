package sit.int221.integratedapi;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;

public class PasswordTest {
    public static void main(String[] args) {
        // Password to test
        String password = "ip23/SOM";

        // Create Argon2 instance for Argon2id
        Argon2 argon2 = Argon2Factory.create();

        // Create a new hash for the password with the correct parameters
        String newHash = argon2.hash(2, 16384, 1, password);
        System.out.println("Generated Hash: " + newHash);

        // Example stored hash
        String storedHashedPassword = "$argon2id$v=19$m=16384,t=2,p=1$MzBnNmJsc0NFb01Pb2RLbA$tYQXME829H8lr0zVfC0mOd2D3GcYA2utLWym01p5hPM";

        // Verify the password
        boolean isPasswordValid = argon2.verify(storedHashedPassword, password);
        System.out.println("Password verification result for password '" + password + "': " + isPasswordValid);
    }
}

