package viettel.telecom.backend.service.flow;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Simple service to load a Bearer token from application.properties.
 * e.g. myapp.bearerToken=eyJhbGciOiJIUzI1Ni...
 */
@Service
public class MyTokenService {

    @Value("${myapp.bearerToken:}")
    private String bearerToken;

    public String getBearerToken() {
        return bearerToken;
    }
}
