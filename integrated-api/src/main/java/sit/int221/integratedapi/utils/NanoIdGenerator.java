package sit.int221.integratedapi.utils;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;

public class NanoIdGenerator {
    public static String generateNanoId(int length) {
        // ตรวจสอบความยาวที่ต้องการ ไม่ควรเกินความยาวสูงสุดที่รองรับ
        if (length <= 0 || length > 21) {
            throw new IllegalArgumentException("Length must be between 1 and 21");
        }
        return NanoIdUtils.randomNanoId(NanoIdUtils.DEFAULT_NUMBER_GENERATOR, NanoIdUtils.DEFAULT_ALPHABET, length);
    }
}
