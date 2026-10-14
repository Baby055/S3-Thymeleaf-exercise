package com.sombiniaina.project.endpoint.rest.controller;

import com.sombiniaina.project.service.VaultService;
import java.io.IOException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
@AllArgsConstructor
public class VaultController {

  private final VaultService vaultService;

  @GetMapping("/vault")
  public String vault(Model model) {
    model.addAttribute("documentsList", vaultService.getAllDocuments());
    return "vault";
  }

  @PostMapping("/vault/upload")
  public String uploadDocument(
      @RequestParam("studentName") String studentName,
      @RequestParam("documentType") String documentType,
      @RequestParam("file") MultipartFile file)
      throws IOException {
    vaultService.upload(studentName, documentType, file);
    return "redirect:/vault";
  }
}
