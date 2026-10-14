package com.sombiniaina.project.model;

import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record Document(UUID id, String studentName, String documentType, String s3key, Instant uploadDate) {}
