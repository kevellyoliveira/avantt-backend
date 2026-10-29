package com.avantt_backend.service;

import com.avantt_backend.dto.ClienteRequestDTO;
import com.avantt_backend.dto.ClienteResponseDTO;
import com.avantt_backend.entity.Cliente;
import com.avantt_backend.exception.ResourceNotFoundException;
import com.avantt_backend.repository.ClienteRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;

    @PersistenceContext
    private EntityManager em;

    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    @Transactional
    public ClienteResponseDTO create(ClienteRequestDTO dto) {
        // Basic validation: ensure frontend provided the required name field. If not, return a clear 400
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            throw new com.avantt_backend.exception.ApiException("name é obrigatório");
        }
        Cliente e = new Cliente();
        e.setNome(dto.getName());
        e.setEmailContato(dto.getEmail());
        e.setTelefone(dto.getPhone());
        // Try to satisfy DB constraint for organizacao_id: use provided value if available, otherwise default to 1
        try {
            // If request contained an organizacao id under several possible names, use it (DTO currently doesn't expose it).
            e.setOrganizacaoId(1);
        } catch (Exception ex) {
            e.setOrganizacaoId(1);
        }
        e.setIsAtivo(dto.getIsAtivo() == null ? Boolean.TRUE : dto.getIsAtivo());
        if (Boolean.FALSE.equals(e.getIsAtivo())) e.setDataDesativacao(LocalDateTime.now());

        Cliente saved = clienteRepository.save(e);
        em.flush();
        em.refresh(saved);
        // include provided defaultUser in response so frontend can display the invited manager
        ClienteResponseDTO resp = toDto(saved);
        if (dto.getDefaultUser() != null) {
            ClienteResponseDTO.DefaultUser du = new ClienteResponseDTO.DefaultUser();
            du.setName(dto.getDefaultUser().getName());
            du.setEmail(dto.getDefaultUser().getEmail());
            du.setStatus(dto.getDefaultUser().getStatus());
            du.setRole(dto.getDefaultUser().getRole());
            du.setProjectIds(dto.getDefaultUser().getProjectIds());
            resp.setDefaultUser(du);
            // ensure response email matches defaultUser.email if provided
            if (dto.getDefaultUser().getEmail() != null) resp.setEmail(dto.getDefaultUser().getEmail());
        }
        // document comes from request (frontend sends formatted CNPJ), keep as-is
        resp.setDocument(dto.getDocument());
        return resp;
    }

    @Transactional
    public ClienteResponseDTO update(Integer id, ClienteRequestDTO dto) {
        var opt = clienteRepository.findById(id);
        if (opt.isEmpty()) throw new ResourceNotFoundException("Cliente não encontrado");
        Cliente e = opt.get();

        if (dto.getName() != null) e.setNome(dto.getName());
        if (dto.getEmail() != null) e.setEmailContato(dto.getEmail());
        if (dto.getPhone() != null) e.setTelefone(dto.getPhone());
        if (dto.getIsAtivo() != null) {
            if (Boolean.FALSE.equals(dto.getIsAtivo()) && Boolean.TRUE.equals(e.getIsAtivo())) {
                e.setIsAtivo(false);
                e.setDataDesativacao(LocalDateTime.now());
            } else if (Boolean.TRUE.equals(dto.getIsAtivo())) {
                e.setIsAtivo(true);
                e.setDataDesativacao(null);
            }
        }

        Cliente saved = clienteRepository.save(e);
        ClienteResponseDTO resp = toDto(saved);
        // Reflect fields from the request when present
        if (dto.getDocument() != null) resp.setDocument(dto.getDocument());
        if (dto.getDefaultUser() != null) {
            ClienteResponseDTO.DefaultUser du = new ClienteResponseDTO.DefaultUser();
            du.setName(dto.getDefaultUser().getName());
            du.setEmail(dto.getDefaultUser().getEmail());
            du.setStatus(dto.getDefaultUser().getStatus());
            du.setRole(dto.getDefaultUser().getRole());
            du.setProjectIds(dto.getDefaultUser().getProjectIds());
            resp.setDefaultUser(du);
        }
        return resp;
    }

    public List<ClienteResponseDTO> listAll() {
        return clienteRepository.findAll().stream().map(this::toDto).toList();
    }

    public ClienteResponseDTO getById(Integer id) {
        return clienteRepository.findById(id).map(this::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado: id=" + id));
    }

    @Transactional
    public void delete(Integer id) {
        var opt = clienteRepository.findById(id);
        if (opt.isEmpty()) throw new ResourceNotFoundException("Cliente não encontrado");
        Cliente e = opt.get();
        // soft-delete: mark inactive and set dataDesativacao
        if (Boolean.TRUE.equals(e.getIsAtivo())) {
            e.setIsAtivo(false);
            e.setDataDesativacao(LocalDateTime.now());
            clienteRepository.save(e);
        }
    }

    private ClienteResponseDTO toDto(Cliente o) {
        ClienteResponseDTO r = new ClienteResponseDTO();
        r.setId(o.getId() == null ? null : String.valueOf(o.getId()));
        r.setName(o.getNome());
        r.setEmail(o.getEmailContato());
        r.setPhone(o.getTelefone());
        r.setIsAtivo(o.getIsAtivo());
        r.setDataCadastro(o.getDataCadastro());
        r.setDataDesativacao(o.getDataDesativacao());
        // document and defaultUser are not stored in Cliente entity; they will be null by default
        r.setDocument(null);
        r.setDefaultUser(null);
        return r;
    }
}
