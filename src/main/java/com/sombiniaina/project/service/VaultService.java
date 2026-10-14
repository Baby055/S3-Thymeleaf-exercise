package com.sombiniaina.project.service;

import com.sombiniaina.project.file.bucket.BucketComponent;
import com.sombiniaina.project.model.VaultDocumentView;
import com.sombiniaina.project.repository.DocumentRepository;
import com.sombiniaina.project.repository.model.JDocument;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@AllArgsConstructor
public class VaultService {

  private static final Duration PRESIGN_DURATION = Duration.ofMinutes(15);

  private final DocumentRepository documentRepository;
  private final BucketComponent bucketComponent;

  public List<VaultDocumentView> getAllDocuments() {
    return documentRepository.findAll().stream().map(this::toView).toList();
  }

  public JDocument upload(String studentName, String documentType, MultipartFile file)
      throws IOException {
    if (file.isEmpty()) {
      throw new IllegalArgumentException("Cannot upload an empty file");
    }

    UUID id = UUID.randomUUID();
    String s3Key = "vault/" + sanitize(studentName) + "/" + id + "-" + safeFileName(file);

    File tempFile = File.createTempFile("poja-upload-", ".tmp");
    try {
      file.transferTo(tempFile);
      bucketComponent.upload(tempFile, s3Key);

      return documentRepository.save(
          JDocument.builder()
              .id(id)
              .studentName(studentName)
              .documentType(documentType)
              .s3key(s3Key)
              .uploadDate(Instant.now())
              .build());
    } finally {
      tempFile.delete();
    }
  }

  private VaultDocumentView toView(JDocument entity) {
    String url = bucketComponent.presign(entity.getS3key(), PRESIGN_DURATION).toString();
    return new VaultDocumentView(
        entity.getId(),
        entity.getStudentName(),
        entity.getDocumentType(),
        entity.getS3key(),
        entity.getUploadDate(),
        url);
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
