package com.sombiniaina.project.service;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.sombiniaina.project.file.bucket.BucketConf;
import com.sombiniaina.project.model.VaultDocumentView;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

@Service
@AllArgsConstructor
public class VaultService {

  private static final Duration PRESIGN_DURATION = Duration.ofMinutes(15);
  private static final String VAULT_PREFIX = "vault/";
  private static final String META_ID = "id";
  private static final String META_STUDENT_NAME = "studentname";
  private static final String META_DOCUMENT_TYPE = "documenttype";

  private final BucketConf bucketConf;

  public List<VaultDocumentView> getAllDocuments() {
    var s3 = bucketConf.getS3Client();
    var bucket = bucketConf.getBucketName();
    var request = ListObjectsV2Request.builder().bucket(bucket).prefix(VAULT_PREFIX).build();

    return s3.listObjectsV2Paginator(request).contents().stream()
        .map(object -> toView(s3, bucket, object))
        .toList();
  }

  public void upload(String studentName, String documentType, MultipartFile file)
      throws IOException {
    if (file.isEmpty()) {
      throw new IllegalArgumentException("Cannot upload an empty file");
    }

    UUID id = UUID.randomUUID();
    String s3Key = "vault/" + sanitize(studentName) + "/" + id + "-" + safeFileName(file);

    File tempFile = File.createTempFile("poja-upload-", ".tmp");
    try {
      file.transferTo(tempFile);
      bucketConf
          .getS3Client()
          .putObject(
              PutObjectRequest.builder()
                  .bucket(bucketConf.getBucketName())
                  .key(s3Key)
                  .metadata(
                      Map.of(
                          META_ID, id.toString(),
                          META_STUDENT_NAME, studentName,
                          META_DOCUMENT_TYPE, documentType))
                  .build(),
              RequestBody.fromFile(tempFile));
    } finally {
      tempFile.delete();
    }
  }

  private VaultDocumentView toView(S3Client s3, String bucket, S3Object object) {
    HeadObjectResponse head = s3.headObject(b -> b.bucket(bucket).key(object.key()));
    Map<String, String> metadata = head.metadata();

    return new VaultDocumentView(
        idOf(metadata, object.key()),
        metadata.getOrDefault(META_STUDENT_NAME, ""),
        metadata.getOrDefault(META_DOCUMENT_TYPE, ""),
        object.key(),
        head.lastModified(),
        presign(bucket, object.key()));
  }

  private UUID idOf(Map<String, String> metadata, String key) {
    String id = metadata.get(META_ID);
    return id == null ? UUID.nameUUIDFromBytes(key.getBytes(UTF_8)) : UUID.fromString(id);
  }

  private String presign(String bucket, String key) {
    return bucketConf
        .getS3Presigner()
        .presignGetObject(
            GetObjectPresignRequest.builder()
                .signatureDuration(PRESIGN_DURATION)
                .getObjectRequest(GetObjectRequest.builder().bucket(bucket).key(key).build())
                .build())
        .url()
        .toString();
  }

  private String safeFileName(MultipartFile file) {
    String original = file.getOriginalFilename();
    if (original == null || original.isBlank()) {
      return "file";
    }
    return sanitize(Paths.get(original).getFileName().toString());
  }

  private String sanitize(String value) {
    return value.trim().replaceAll("[^a-zA-Z0-9._-]", "_");
  }
}
