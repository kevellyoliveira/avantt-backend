package com.avantt_backend.controller;

import com.avantt_backend.dto.OrganizacaoRequestDTO;
import com.avantt_backend.dto.OrganizacaoResponseDTO;
import com.avantt_backend.dto.RegisterOrganizacaoRequestDTO;
import com.avantt_backend.dto.UsuarioResponseDTO;
import com.avantt_backend.service.AuthService;
import com.avantt_backend.service.ConviteService;
import com.avantt_backend.service.OrganizacaoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    AuthService authService;

    @Mock
    OrganizacaoService organizacaoService;

    @Mock
    ConviteService conviteService;

    @InjectMocks
    AuthController authController;

    @Test
    void registerOrganizacao_usesCreatorAsContactAndPersistsOrganizationDetails() {
        RegisterOrganizacaoRequestDTO request = new RegisterOrganizacaoRequestDTO();
        request.setName("Ana Silva");
        request.setEmail("ana@example.com");
        request.setPassword("password123");
        request.setOrganizationName("Empresa Ana");
        request.setCnpj("12.345.678/0001-90");
        request.setTelefone("(11) 99999-1234");

        OrganizacaoResponseDTO organization = new OrganizacaoResponseDTO();
        organization.setId("9");
        when(organizacaoService.create(any(OrganizacaoRequestDTO.class)))
                .thenReturn(organization);
        when(authService.register(any(), eq(9)))
                .thenReturn(new UsuarioResponseDTO());

        authController.registerOrganizacao(request);

        ArgumentCaptor<OrganizacaoRequestDTO> organizationRequest =
                ArgumentCaptor.forClass(OrganizacaoRequestDTO.class);
        verify(organizacaoService).create(organizationRequest.capture());
        assertEquals("Ana Silva", organizationRequest.getValue().getContato());
        assertEquals("ana@example.com", organizationRequest.getValue().getEmailContato());
        assertEquals("(11) 99999-1234", organizationRequest.getValue().getTelefone());
    }
}
