/*
 * Copyright 2025 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.planck.planck.util;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.springframework.stereotype.Component;

@Component
@NoArgsConstructor
@Slf4j
public class EncryptionUtil {

  private static final String EMPTY = "";
  private static final String ALGORITHM = "AES";

  private static String getSecretKey() {
    String secretKey = PlanckEnvironmentUtil.getProperty("aes.secretKey");
    if (secretKey == null || secretKey.isEmpty()) {
      log.error("AES secret key is not configured.");
      throw new IllegalStateException("AES secret key is not configured.");
    }
    return secretKey;
  }

  public static String decrypt(String encryptedValue) {
    log.info("Decryption Start");
    Key key;
    try {
      key = generateKey();
      Cipher cipher = Cipher.getInstance(ALGORITHM);
      cipher.init(Cipher.DECRYPT_MODE, key);
      byte[] decodedValue = Base64.decodeBase64(encryptedValue);
      byte[] decValue = cipher.doFinal(decodedValue);
      log.info("Decryption End");
      return new String(decValue);
    } catch (Exception e) {
      log.error("Error while Decrypting.Error is ::", e);
      return EMPTY;
    }
  }

  private static Key generateKey() throws Exception {
    log.info("Generate Key Start");
    byte[] keyBytes = getSecretKey().getBytes(StandardCharsets.UTF_8);
    log.info("Generate Key End");
    return new SecretKeySpec(keyBytes, ALGORITHM);
  }

  public static String encrypt(String valueToEnc) throws Exception {
    log.info("Encryption Start");
    Key key = generateKey();
    Cipher cipher = Cipher.getInstance(ALGORITHM);
    cipher.init(Cipher.ENCRYPT_MODE, key);
    byte[] encValue = cipher.doFinal(valueToEnc.getBytes());
    log.info("Encryption End");
    return Base64.encodeBase64String(encValue);
  }
}
