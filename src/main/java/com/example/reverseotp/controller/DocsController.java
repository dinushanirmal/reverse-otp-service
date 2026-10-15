package com.example.reverseotp.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Controller
public class DocsController {

    private final Resource markdownResource = new ClassPathResource("static/docs.md");

    @GetMapping(value = "/docs.md", produces = "text/markdown; charset=UTF-8")
    @ResponseBody
    public ResponseEntity<String> getDocsMarkdown() throws IOException {
        String content = StreamUtils.copyToString(markdownResource.getInputStream(), StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "text/markdown; charset=UTF-8")
                .body(content);
    }

    @GetMapping(value = "/docs")
    public String getDocsHtml(@RequestHeader(value = HttpHeaders.ACCEPT, required = false) String acceptHeader) {
        if (acceptHeader != null && (acceptHeader.contains("text/markdown") || acceptHeader.contains("text/plain"))) {
            return "forward:/docs.md";
        }
        return "forward:/index.html";
    }

    @GetMapping("/")
    public String rootRedirect() {
        return "forward:/index.html";
    }
}
