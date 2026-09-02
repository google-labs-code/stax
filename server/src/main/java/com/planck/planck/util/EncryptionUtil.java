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
import java.security.SecureRandom;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
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
  private static final String TRANSFORMATION = "AES/GCM/NoPadding";
  private static final String LEGACY_TRANSFORMATION = "AES/ECB/PKCS5Padding";
  private static final int GCM_TAG_LENGTH = 128;
  private static final int GCM_IV_LENGTH = 12;

  private static String getSecretKey() {
    String secretKey = PlanckEnvironmentUtil.getProperty("aes.secretKey");
    if (secretKey == null || secretKey.isEmpty()) {
      log.error("AES secret key is not configured.");
      throw new IllegalStateException("AES secret key is not configured.");
    }
    return secretKey;
  }

  public static String decrypt(String encryptedValue) {
    if (encryptedValue == null || encryptedValue.trim().isEmpty()) {
      return EMPTY;
    }
    try {
      Key key = generateKey();
      byte[] decodedValue = Base64.decodeBase64(encryptedValue);
      if (decodedValue.length < GCM_IV_LENGTH) {
        return decryptLegacy(decodedValue, key);
      }
      try {
        GCMParameterSpec spec =
            new GCMParameterSpec(GCM_TAG_LENGTH, decodedValue, 0, GCM_IV_LENGTH);
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, key, spec);
        int len = decodedValue.length - GCM_IV_LENGTH;
        byte[] decValue = cipher.doFinal(decodedValue, GCM_IV_LENGTH, len);
        return new String(decValue, StandardCharsets.UTF_8);
      } catch (Exception gcmException) {
        return decryptLegacy(decodedValue, key);
      }
    } catch (Exception e) {
      log.error("Error while Decrypting. Error is ::", e);
      return EMPTY;
    }
  }

  private static String decryptLegacy(byte[] decodedValue, Key key) throws Exception {
    Cipher cipher = Cipher.getInstance(LEGACY_TRANSFORMATION);
    cipher.init(Cipher.DECRYPT_MODE, key);
    byte[] decValue = cipher.doFinal(decodedValue);
    return new String(decValue, StandardCharsets.UTF_8);
  }

  private static Key generateKey() throws Exception {
    byte[] keyBytes = getSecretKey().getBytes(StandardCharsets.UTF_8);
    return new SecretKeySpec(keyBytes, ALGORITHM);
  }

  public static String encrypt(String valueToEnc) throws Exception {
    if (valueToEnc == null) {
      return EMPTY;
    }
    Key key = generateKey();
    byte[] iv = new byte[GCM_IV_LENGTH];
    new SecureRandom().nextBytes(iv);
    GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
    Cipher cipher = Cipher.getInstance(TRANSFORMATION);
    cipher.init(Cipher.ENCRYPT_MODE, key, parameterSpec);
    byte[] input = valueToEnc.getBytes(StandardCharsets.UTF_8);
    byte[] encValue = cipher.doFinal(input);
    byte[] combined = new byte[iv.length + encValue.length];
    System.arraycopy(iv, 0, combined, 0, iv.length);
    System.arraycopy(encValue, 0, combined, iv.length, encValue.length);
    return Base64.encodeBase64String(combined);
  }
}
