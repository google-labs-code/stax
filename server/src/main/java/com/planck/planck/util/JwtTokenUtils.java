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

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenUtils {

  public String extractUserName(String token, Key signingKey) {
    return extractClaim(token, Claims::getSubject, signingKey);
  }

  public String generateToken(
      Map<String, Object> extraClaims, UserDetails userDetails, long validity, Key signingKey) {
    return Jwts.builder()
        .setClaims(extraClaims)
        .setSubject(userDetails.getUsername())
        .setIssuedAt(new Date(System.currentTimeMillis()))
        .setExpiration(new Date(System.currentTimeMillis() + (validity * 1000)))
        .signWith(signingKey, SignatureAlgorithm.HS256)
        .compact();
  }

  public boolean isTokenValid(String token, UserDetails userDetails, Key signingKey) {
    final String userName = extractUserName(token, signingKey);
    return (userName.equals(userDetails.getUsername())) && !isTokenExpired(token, signingKey);
  }

  public <T> T extractClaim(String token, Function<Claims, T> claimsResolvers, Key signingKey) {
    final Claims claims = extractAllClaims(token, signingKey);
    return claimsResolvers.apply(claims);
  }

  public boolean isTokenExpired(String token, Key signingKey) {
    return extractExpiration(token, signingKey).before(new Date());
  }

  public Date extractExpiration(String token, Key signingKey) {
    return extractClaim(token, Claims::getExpiration, signingKey);
  }

  public Claims extractAllClaims(String token, Key signingKey) {
    return Jwts.parserBuilder().setSigningKey(signingKey).build().parseClaimsJws(token).getBody();
  }

  public Key getSigningKey(String signingKey) {
    byte[] keyBytes = Decoders.BASE64.decode(signingKey);
    return Keys.hmacShaKeyFor(keyBytes);
  }
}
