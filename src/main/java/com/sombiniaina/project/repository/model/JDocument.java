package com.sombiniaina.project.repository.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.time.Instant;
import java.util.UUID;
import lombok.*;
import software.amazon.awssdk.annotations.NotNull;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class JDocument {
  @Id private UUID id;

  @NotNull private String studentName;
  @NotNull private String documentType;

  private String s3key;

  @NotNull private Instant uploadDate;
}
