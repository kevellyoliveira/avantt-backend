package com.avantt_backend.controller;

import com.avantt_backend.dto.ExampleDto;
import com.avantt_backend.service.ExampleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Exemplo de Controller REST para começar a estrutura MVC.
 */
@RestController
@RequestMapping("/api/example")
public class HomeController {

    private final ExampleService service;

    @Autowired
    public HomeController(ExampleService service) {
        this.service = service;
    }

    @GetMapping
    public List<ExampleDto> list() {
        return service.findAll();
    }
}
