package com.avantt_backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class PingController {

    // Multiple aliases to help debugging frontend path mismatches
    @GetMapping({"/ping", "/api/ping", "/organizacoes/ping", "/api/organizacoes/ping"})
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("pong");
    }
}
