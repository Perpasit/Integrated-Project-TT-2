package sit.int221.integratedapi.services;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class UserService {
    private final Argon2 argon2;

    public UserService() {
        this.argon2 = Argon2Factory.create();
    }

    public String hashPassword(String password) {
        try {
            return argon2.hash(10, 65536, 1, password);
        } catch (Exception e) {
            // การจัดการข้อผิดพลาด
            e.printStackTrace(); // พิมพ์ stack trace ไปที่ console
            return null;
        }
    }

    public boolean verifyPassword(String password, String hashedPassword) {
        try {
            return argon2.verify(hashedPassword, password);
        } catch (Exception e) {
            // การจัดการข้อผิดพลาด
            e.printStackTrace(); // พิมพ์ stack trace ไปที่ console
            return false;
        }
    }
}

