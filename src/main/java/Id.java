import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import java.util.UUID;

public class Id {
    public static String random() {
        try {
            char[] uuid = UUID.randomUUID().toString().replaceAll("-", "").toCharArray();
            byte[] unhexed = Hex.decodeHex(uuid);
            return Base58.encode(unhexed);
        } catch (DecoderException e) {
            throw new RuntimeException(e);
        }
    }
}
