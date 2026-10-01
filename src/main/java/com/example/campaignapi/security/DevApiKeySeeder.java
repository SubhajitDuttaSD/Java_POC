package com.example.campaignapi.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Dev-only convenience: seeds a read-only and a full-access API key on first run,
 * printing the raw values once since only their hashes are stored. Not suitable
 * for production key issuance.
 */
@Component
public class DevApiKeySeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DevApiKeySeeder.class);

    private final ApiKeyRepository apiKeyRepository;

    public DevApiKeySeeder(ApiKeyRepository apiKeyRepository) {
        this.apiKeyRepository = apiKeyRepository;
    }

    @Override
    public void run(String... args) {
        if (apiKeyRepository.count() > 0) {
            return;
        }

        String readOnlyKey = generateRawKey();
        ApiKey readOnly = new ApiKey();
        readOnly.setName("dev-read-only");
        readOnly.setKeyHash(ApiKeyHasher.hash(readOnlyKey));
        readOnly.setScopes(ApiKeyScopes.READ);
        apiKeyRepository.save(readOnly);

        String fullAccessKey = generateRawKey();
        ApiKey fullAccess = new ApiKey();
        fullAccess.setName("dev-full-access");
        fullAccess.setKeyHash(ApiKeyHasher.hash(fullAccessKey));
        fullAccess.setScopes(ApiKeyScopes.READ + "," + ApiKeyScopes.WRITE);
        apiKeyRepository.save(fullAccess);

        log.info("============================================================");
        log.info("Dev API keys generated (shown once — only hashes are stored):");
        log.info("  READ-ONLY key:    {}", readOnlyKey);
        log.info("  FULL-ACCESS key:  {}", fullAccessKey);
        log.info("============================================================");
    }

    private String generateRawKey() {
        return "sk_" + UUID.randomUUID().toString().replace("-", "")
                + UUID.randomUUID().toString().replace("-", "");
    }
}
