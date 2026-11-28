package com.avantt_backend.service;

import com.avantt_backend.dto.ClienteRequestDTO;
import com.avantt_backend.entity.Cliente;
import com.avantt_backend.repository.ClienteRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private ClienteService clienteService;

    @BeforeEach
    void injectEntityManager() throws Exception {
        Field field = ClienteService.class.getDeclaredField("em");
        field.setAccessible(true);
        field.set(clienteService, entityManager);
    }

    @Test
    void listAll_filtersByOrganization() {
        Cliente cliente = new Cliente();
        cliente.setId(5);
        cliente.setOrganizacaoId(8);
        cliente.setNome("Cliente da organização");
        when(clienteRepository.findByOrganizacaoId(8)).thenReturn(List.of(cliente));

        var result = clienteService.listAll(8);

        assertEquals(1, result.size());
        assertEquals(5, result.get(0).getId());
        assertEquals("Cliente da organização", result.get(0).getName());
        verify(clienteRepository).findByOrganizacaoId(8);
    }

    @Test
    void create_setsAuthenticatedOrganization() {
        ClienteRequestDTO request = new ClienteRequestDTO();
        request.setName("Novo cliente");
        request.setPhone("(11) 99999-1111");
        when(clienteRepository.save(any(Cliente.class)))
                .thenAnswer(invocation -> {
                    Cliente cliente = invocation.getArgument(0);
                    cliente.setId(12);
                    return cliente;
                });

        var response = clienteService.create(request, 8);

        ArgumentCaptor<Cliente> clienteCaptor = ArgumentCaptor.forClass(Cliente.class);
        verify(clienteRepository).save(clienteCaptor.capture());
        assertEquals(8, clienteCaptor.getValue().getOrganizacaoId());
        assertEquals("(11) 99999-1111", clienteCaptor.getValue().getTelefone());
        assertEquals("(11) 99999-1111", response.getPhone());
        verify(entityManager).flush();
        verify(entityManager).refresh(clienteCaptor.getValue());
    }
}
