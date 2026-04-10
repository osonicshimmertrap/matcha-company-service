package az.matcha.company.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

@Configuration
public class JwtConfig {

    @Bean
    public JwtDecoder jwtDecoder(RsaKeyConfig rsaKeyConfig) {
        return NimbusJwtDecoder.withPublicKey(rsaKeyConfig.publicKey()).build();
    }
}
