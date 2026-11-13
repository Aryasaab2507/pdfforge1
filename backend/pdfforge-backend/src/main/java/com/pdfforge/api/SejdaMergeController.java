package com.pdfforge.api;

import com.pdfforge.sejda.SejdaMergeService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/pdf")
public class SejdaMergeController {
    private static final long MAX_FILE_BYTES = 50L * 1024 * 1024;
    private static final int MAX_FILES = 20;

    @Value("${pdfforge.api-key:}")
    private String configuredApiKey;

    private final SejdaMergeService mergeService = new SejdaMergeService();

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "ok", "engine", "sejda");
    }

    @PostMapping(value = "/merge", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> merge(
            @RequestHeader(value = "X-PDFForge-API-Key", required = false) String providedApiKey,
            @RequestParam("files") List<MultipartFile> uploads) throws IOException {

        if (configuredApiKey == null || configuredApiKey.isBlank()) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("error", "PDFforge API authentication is not configured."));
        }
        if (providedApiKey == null || !MessageDigest.isEqual(
                configuredApiKey.getBytes(StandardCharsets.UTF_8),
                providedApiKey.getBytes(StandardCharsets.UTF_8))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "A valid X-PDFForge-API-Key header is required."));
        }

        if (uploads == null || uploads.size() < 2) {
            throw new IllegalArgumentException("Upload at least 2 PDF files using the 'files' field.");
        }
        if (uploads.size() > MAX_FILES) {
            throw new IllegalArgumentException("A maximum of 20 PDF files is allowed per request.");
        }

        List<File> temporaryInputs = new ArrayList<>();
        File mergedOutput = null;
        try {
            for (MultipartFile upload : uploads) {
                if (upload == null || upload.isEmpty()) {
                    throw new IllegalArgumentException("An uploaded file is empty.");
                }
                if (upload.getSize() > MAX_FILE_BYTES) {
                    throw new IllegalArgumentException("Each PDF must be 50 MB or smaller.");
                }
                String filename = Optional.ofNullable(upload.getOriginalFilename()).orElse("upload.pdf");
                if (!filename.toLowerCase(Locale.ROOT).endsWith(".pdf")) {
                    throw new IllegalArgumentException("Only PDF files are accepted.");
                }

                File temporary = Files.createTempFile("pdfforge-input-", ".pdf").toFile();
                upload.transferTo(temporary);
                temporaryInputs.add(temporary);
            }

            mergedOutput = mergeService.merge(temporaryInputs);
            byte[] response = Files.readAllBytes(mergedOutput.toPath());
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"merged.pdf\"")
                    .body(response);
        } finally {
            for (File input : temporaryInputs) {
                try { Files.deleteIfExists(input.toPath()); } catch (IOException ignored) { }
            }
            if (mergedOutput != null) {
                try { Files.deleteIfExists(mergedOutput.toPath()); } catch (IOException ignored) { }
            }
        }
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> badRequest(IllegalArgumentException exception) {
        return ResponseEntity.badRequest().body(Map.of("error", exception.getMessage()));
    }
}
