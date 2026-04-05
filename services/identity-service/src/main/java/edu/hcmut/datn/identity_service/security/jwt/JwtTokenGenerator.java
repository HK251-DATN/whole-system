package edu.hcmut.datn.identity_service.security.jwt;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import edu.hcmut.datn.identity_service.dao.User;
import edu.hcmut.datn.identity_service.dto.misc.PermissionBasicView;
import edu.hcmut.datn.identity_service.repository.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Component
@Slf4j
public class JwtTokenGenerator {
    @Autowired
    private UserRepository userRepository;

    public String generateToken(User user) {
        Instant now = Instant.now();

        List<String> permissions = userRepository.getUserPermissions(user.getUserId()).stream()
                .map(PermissionBasicView::getPerCode).toList();

        log.info("Time now: {}", Date.from(now));
        log.info("Token expired at: {}", Date.from(now.plusSeconds(3600 * 24)));
        
        return Jwts.builder()
                .setSubject(user.getUserEmail())
                .setIssuer("identity-service")
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(3600 * 24)))
                .claim("userId", user.getUserId())
                .claim("userEmail", user.getUserEmail())
                .claim("permissions", permissions)
                .signWith(loadPrivateKey(), SignatureAlgorithm.RS256)
                .compact();
        // return null;
    }

    private PrivateKey loadPrivateKey() {
        try {
            Resource resource = new ClassPathResource("keys/private.pem");

            String pem = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

            String privateKeyContent = pem
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");

            byte[] decoded = Base64.getDecoder().decode(privateKeyContent);

            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decoded);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");

            return keyFactory.generatePrivate(keySpec);

        } catch (Exception e) {
            throw new IllegalStateException("Failed to load private key", e);
        }
    }

}
