package com.sombiniaina.project.model;

import java.time.Instant;
import java.util.UUID;

public record VaultDocumentView(
        UUID id,
        String studentName,
        String documentType,
        String s3key,
        Instant uploadDate,
        String presignedUrl
) {}
