package com.avantt_backend.service;

import com.avantt_backend.dto.ClienteRequestDTO;
import com.avantt_backend.dto.ClienteResponseDTO;
import com.avantt_backend.entity.Cliente;
import com.avantt_backend.exception.ResourceNotFoundException;
import com.avantt_backend.repository.ClienteRepository;
import com.avantt_backend.repository.OrganizacaoRepository;
import com.avantt_backend.repository.UsuarioRepository;
import com.avantt_backend.repository.PerfilRepository;
import com.avantt_backend.entity.Organizacao;
import com.avantt_backend.entity.Usuario;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ClienteService {
    private final ClienteRepository repository;
    private final OrganizacaoRepository organizacaoRepository;
    private final UsuarioRepository usuarioRepository;
    private final PerfilRepository perfilRepository;
    private final EmailService emailService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public ClienteService(ClienteRepository repository, OrganizacaoRepository organizacaoRepository,
                          UsuarioRepository usuarioRepository, PerfilRepository perfilRepository,
                          EmailService emailService) {
        this.repository = repository;
        this.organizacaoRepository = organizacaoRepository;
        this.usuarioRepository = usuarioRepository;
        this.perfilRepository = perfilRepository;
        this.emailService = emailService;
    }

    @Transactional(readOnly = true)
    public List<ClienteResponseDTO> listByOrganization(Integer organizationId) {
        if (organizationId == null) return List.of();
        return repository.findByOrganizacaoIdOrderByNome(organizationId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional
    public ClienteResponseDTO create(ClienteRequestDTO request, Integer organizationId) {
        if (organizationId == null) throw new ResourceNotFoundException("Usuário não vinculado a uma organização");
        Cliente client = new Cliente();
        client.setOrganizacaoId(organizationId);
        apply(client, request);
        if (usuarioRepository.existsByEmail(request.getEmail().trim().toLowerCase())) {
            throw new IllegalStateException("Já existe um usuário com este e-mail.");
        }
        Cliente savedClient = repository.save(client);

        Organizacao clientOrganization = new Organizacao();
        clientOrganization.setNomeFantasia(request.getNome().trim());
        clientOrganization.setCnpj(request.getDocumento() == null ? null : request.getDocumento().replaceAll("\\D", ""));
        clientOrganization.setEmailContato(request.getEmail().trim().toLowerCase());
        clientOrganization.setIsAtivo(true);
        clientOrganization = organizacaoRepository.save(clientOrganization);
        savedClient.setOrganizacaoClienteId(clientOrganization.getId());
        savedClient = repository.save(savedClient);

        Usuario manager = new Usuario();
        manager.setNome("Cadastro pendente");
        manager.setEmail(request.getEmail().trim().toLowerCase());
        manager.setCargo("Gestor");
        manager.setPerfilId(perfilRepository.findByNomeIgnoreCase("Administrador")
                .or(() -> perfilRepository.findByNomeIgnoreCase("Admin"))
                .orElseThrow(() -> new IllegalStateException("Perfil Admin não configurado")).getId());
        manager.setOrganizacaoId(clientOrganization.getId());
        manager.setSenha(null);
        manager.setIsAtivo(false);
        manager.setResetToken(UUID.randomUUID().toString());
        manager.setResetTokenExpiry(LocalDateTime.now().plusHours(48));
        manager = usuarioRepository.save(manager);
        emailService.sendClientManagerInvitation(manager.getEmail(), clientOrganization.getNomeFantasia(), manager.getResetToken());
        return toResponse(savedClient);
    }

    @Transactional
    public ClienteResponseDTO update(Integer id, ClienteRequestDTO request, Integer organizationId) {
        Cliente client = repository.findByIdAndOrganizacaoId(id, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado"));
        apply(client, request);
        return toResponse(repository.save(client));
    }

    private void apply(Cliente client, ClienteRequestDTO request) {
        client.setNome(request.getNome().trim());
        client.setDocumento(request.getDocumento());
        client.setEmailContato(request.getEmail());
        client.setTelefone(request.getTelefone());
    }

    private ClienteResponseDTO toResponse(Cliente client) {
        ClienteResponseDTO response = new ClienteResponseDTO();
        response.setId(client.getId());
        response.setOrganizationId(client.getOrganizacaoId());
        response.setClientOrganizationId(client.getOrganizacaoClienteId());
        response.setName(client.getNome());
        response.setDocument(client.getDocumento());
        response.setEmail(client.getEmailContato());
        response.setPhone(client.getTelefone());
        response.setCreatedAt(client.getDataCadastro() == null ? null : client.getDataCadastro().toString());
        return response;
    }
}
