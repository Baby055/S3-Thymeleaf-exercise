package com.sombiniaina.project.repository;

import com.sombiniaina.project.repository.model.JDocument;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentRepository extends JpaRepository<JDocument, UUID> {}
