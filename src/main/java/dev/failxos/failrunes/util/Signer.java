package dev.failxos.failrunes.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

/** HMAC signatures so that PDC data cannot be forged or copied onto other items. */
public final class Signer {
    private final SecretKeySpec key;
    public Signer(String secret) { key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"); }

    public String sign(String... parts) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(key);
            byte[] out = mac.doFinal(String.join("|", parts).getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 12; i++) sb.append(String.format("%02x", out[i]));
            return sb.toString();
        } catch (Exception e) { throw new IllegalStateException(e); }
    }
    public boolean verify(String sig, String... parts) { return sig != null && sig.equals(sign(parts)); }
}
