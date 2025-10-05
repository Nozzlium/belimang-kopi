package com.kopi.belimang.image.minio;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "minio")
public class MinioProperties {
    private String endpoint;   // ubah dari "url" jadi "endpoint"
    private String publicEndpoint;
    private String accessKey;
    private String secretKey;
    private boolean secure;
    private int presignExpirySeconds;
    private String bucket;
}

