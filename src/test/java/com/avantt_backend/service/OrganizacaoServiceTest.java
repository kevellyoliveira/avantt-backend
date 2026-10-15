package com.avantt_backend.service;

import com.avantt_backend.dto.OrganizacaoRequestDTO;
import com.avantt_backend.dto.OrganizacaoResponseDTO;
import com.avantt_backend.entity.Organizacao;
import com.avantt_backend.exception.ConflictException;
import com.avantt_backend.exception.ResourceNotFoundException;
import com.avantt_backend.repository.OrganizacaoRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrganizacaoServiceTest {

    @Mock
    OrganizacaoRepository organizacaoRepository;

    @Mock
    EntityManager em;

    @InjectMocks
    OrganizacaoService organizacaoService;

    @BeforeEach
    void setup() throws Exception {
        Field f = OrganizacaoService.class.getDeclaredField("em");
        f.setAccessible(true);
        f.set(organizacaoService, em);
    }

    @Test
    void create_conflictWhenCnpjExists() {
        OrganizacaoRequestDTO dto = new OrganizacaoRequestDTO();
        dto.setNomeFantasia("X");
        dto.setCnpj("111");

        when(organizacaoRepository.existsByCnpj("111")).thenReturn(true);

        assertThrows(ConflictException.class, () -> organizacaoService.create(dto));
    }

    @Test
    void create_success() {
        OrganizacaoRequestDTO dto = new OrganizacaoRequestDTO();
        dto.setNomeFantasia("Org");
        dto.setCnpj("111");

        when(organizacaoRepository.existsByCnpj(any())).thenReturn(false);
        when(organizacaoRepository.save(any(Organizacao.class))).thenAnswer(inv -> {
            Organizacao o = inv.getArgument(0);
            o.setId(10);
            return o;
        });

        OrganizacaoResponseDTO res = organizacaoService.create(dto);
        assertEquals("10", res.getId());
        assertEquals("Org", res.getNomeFantasia());
        verify(em).flush();
        verify(em).refresh(any(Organizacao.class));
    }

    @Test
    void update_nonExisting_throwsNotFound() {
        when(organizacaoRepository.findById(5)).thenReturn(Optional.empty());
        OrganizacaoRequestDTO dto = new OrganizacaoRequestDTO();
        dto.setNomeFantasia("X");
        assertThrows(ResourceNotFoundException.class, () -> organizacaoService.update(5, dto));
    }

    @Test
    void update_success_changesFields() {
        Organizacao existing = new Organizacao();
        existing.setId(7);
        existing.setNomeFantasia("Old");
        existing.setIsAtivo(true);

        when(organizacaoRepository.findById(7)).thenReturn(Optional.of(existing));
        when(organizacaoRepository.existsByCnpj(any())).thenReturn(false);
        when(organizacaoRepository.save(any(Organizacao.class))).thenAnswer(inv -> inv.getArgument(0));

        OrganizacaoRequestDTO dto = new OrganizacaoRequestDTO();
        dto.setNomeFantasia("New");
        dto.setIsAtivo(false);

        OrganizacaoResponseDTO res = organizacaoService.update(7, dto);
        assertEquals("7", res.getId());
        assertEquals("New", res.getNomeFantasia());
        assertFalse(res.getIsAtivo());
        assertNotNull(res.getDataDesativacao());
    }
}
