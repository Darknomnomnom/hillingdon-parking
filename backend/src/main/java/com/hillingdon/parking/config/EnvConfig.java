package com.hillingdon.parking.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EnvConfig {

    @Value("${app.jwt.secret}")
    public String jwtSecret;

    @Value("${app.jwt.expiration-ms}")
    public long jwtExpirationMs;

    @Value("${app.aws.bucket-name}")
    public String s3BucketName;

    @Value("${app.aws.region}")
    public String awsRegion;
}
