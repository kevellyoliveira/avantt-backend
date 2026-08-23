package com.avantt_backend.service;

import com.avantt_backend.dto.ExampleDto;
import com.avantt_backend.model.ExampleModel;
import com.avantt_backend.repository.ExampleRepository;
import com.avantt_backend.util.MapperUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExampleService {

    private final ExampleRepository repository;

    @Autowired
    public ExampleService(ExampleRepository repository) {
        this.repository = repository;
    }

    public List<ExampleDto> findAll() {
        List<ExampleModel> items = repository.findAll();
        return items.stream().map(MapperUtil::toDto).collect(Collectors.toList());
    }
}
