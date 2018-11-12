package com.jvmfy.digitalsignature.pdf;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pdf")
public class PdfResources {

    @GetMapping(value = "/export")
    public ResponseEntity exportPdf() {
        return ResponseEntity.ok().build();
    }
}
