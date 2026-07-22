package com.hillingdon.parking.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EnvConfig {

    @Value("${app.jwt.secret}")
    public String jwtSecret;

    @Value("${app.jwt.expiration-ms}")
    public long jwtExpirationMs;

    @Value("${app.supabase.url}")
    public String supabaseUrl;

    @Value("${app.supabase.service-role-key}")
    public String supabaseServiceRoleKey;

    @Value("${app.supabase.storage-bucket}")
    public String supabaseStorageBucket;
}
