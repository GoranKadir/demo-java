package com.example.demo.controller;

// QuotePdfController.java
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.http.HttpRequest;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.springframework.http.*;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.net.http.*;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/pdf")
public class QuotePdfController {

  private final ObjectMapper objectMapper = new ObjectMapper();

  @PostMapping("/generate")
  public ResponseEntity<byte[]> generatePdfFromQuoteUrl(@RequestBody Map<String, String> body) {
    try {
      String quotePreviewUrl = body.get("url");
      System.out.println("📨 Mottog begäran om att generera PDF från URL: " + quotePreviewUrl);

      // Temporär fil för PDF
      Path tempPdf = Files.createTempFile("offert-", ".pdf");

      // Kör wkhtmltopdf som CLI-kommando
      ProcessBuilder pb = new ProcessBuilder(
          "wkhtmltopdf",
          "--enable-local-file-access", // krävs ibland för Tailwind/CSS-resurser
          quotePreviewUrl,
          tempPdf.toAbsolutePath().toString()
      );

      pb.redirectErrorStream(true);
      Process process = pb.start();

      // Läs logg från wkhtmltopdf
      try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
        String line;
        while ((line = reader.readLine()) != null) {
          System.out.println("[wkhtmltopdf] " + line);
        }
      }

      int exitCode = process.waitFor();
      if (exitCode != 0) {
        System.err.println("🚨 wkhtmltopdf misslyckades med kod: " + exitCode);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
      }

      // Läs ut PDF-filen som bytes
      byte[] pdfBytes = Files.readAllBytes(tempPdf);
      Files.deleteIfExists(tempPdf); // städa efter oss

      return ResponseEntity.ok()
          .contentType(MediaType.APPLICATION_PDF)
          .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=offert.pdf")
          .body(pdfBytes);

    } catch (Exception e) {
      e.printStackTrace();
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }
}