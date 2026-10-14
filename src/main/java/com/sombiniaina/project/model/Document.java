package com.sombiniaina.project.model;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;

@Builder
public record Document(
    UUID id, String studentName, String documentType, String s3key, Instant uploadDate) {}
