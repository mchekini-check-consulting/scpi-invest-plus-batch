package fr.formationacademy.scpiinvestplusbatch.service;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.InputStream;

@Service
public class S3FileService {

    private final S3Client s3Client;

    @Value("${spring.s3.bucket.name}")
    private String bucketName;

    @Value("${spring.s3.file.key}")
    private String fileKey;

    public S3FileService(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public InputStream getScpiFileAsStream() {
        GetObjectRequest objectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(fileKey)
                .build();
        return s3Client.getObject(objectRequest, ResponseTransformer.toInputStream());
    }
}
