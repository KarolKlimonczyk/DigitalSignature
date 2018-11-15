package com.jvmfy.digitalsignature.pdf;

import com.jvmfy.digitalsignature.signature.SigningService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/pdf")
@Slf4j
public class PdfResources {

    private final PdfService pdfService;
    private final SigningService signingService;

    public PdfResources(PdfService pdfService, SigningService signingService) {
        this.pdfService = pdfService;
        this.signingService = signingService;
    }

    @GetMapping(value = "/export", produces = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity exportPdf() {
        try {
            byte[] pdfToSign = this.pdfService.generatePdf();
            byte[] signedPdf = this.signingService.signPdf(pdfToSign);

            return ResponseEntity.ok(signedPdf);
        } catch (IOException e) {
            log.error("Cannot generate PDF file", e);
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
