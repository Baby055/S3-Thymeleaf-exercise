package com.sombiniaina.project.repository;

import com.sombiniaina.project.repository.model.JDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DocumentRepository extends JpaRepository<JDocument, UUID> {
}
