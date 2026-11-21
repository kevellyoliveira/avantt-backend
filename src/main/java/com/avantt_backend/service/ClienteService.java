
package com.avantt_backend.service;

import com.avantt_backend.dto.ClienteRequestDTO;
import com.avantt_backend.dto.ClienteResponseDTO;
import com.avantt_backend.entity.Cliente;
import com.avantt_backend.exception.ResourceNotFoundException;
import com.avantt_backend.exception.ConflictException;
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
        return create(dto, 1);
    }

    @Transactional
    public ClienteResponseDTO create(ClienteRequestDTO dto, Integer organizacaoId) {

        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            throw new com.avantt_backend.exception.ApiException("name é obrigatório");
        }

        String name = dto.getName().trim();
        String document = dto.getDocument() == null ? null : dto.getDocument().replaceAll("\\D", "");
        String email = dto.getEmail() == null ? null : dto.getEmail().trim().toLowerCase();
        String phone = dto.getPhone() == null ? null : dto.getPhone().replaceAll("\\D", "");

        if (clienteRepository.existsByOrganizacaoIdAndNomeIgnoreCase(organizacaoId, name)
                || (document != null && !document.isEmpty() && clienteRepository.existsByOrganizacaoIdAndDocumento(organizacaoId, document))
                || (email != null && !email.isEmpty() && clienteRepository.existsByOrganizacaoIdAndEmailIgnoreCase(organizacaoId, email))
                || (phone != null && !phone.isEmpty() && clienteRepository.existsByOrganizacaoIdAndTelefone(organizacaoId, phone))) {
            throw new ConflictException("Cliente já cadastrado com estes dados");
        }

        Cliente e = new Cliente();

        e.setNome(name);
        e.setEmail(email);
        e.setTelefone(phone);
        e.setDocumento(document);

        e.setOrganizacaoId(organizacaoId);

        e.setIsAtivo(
                dto.getIsAtivo() == null ? Boolean.TRUE : dto.getIsAtivo()
        );

        if (Boolean.FALSE.equals(e.getIsAtivo())) {
            e.setDataDesativacao(LocalDateTime.now());
        }

        Cliente saved = clienteRepository.save(e);

        em.flush();
        em.refresh(saved);

        ClienteResponseDTO resp = toDto(saved);

        resp.setDocument(saved.getDocumento());

        return resp;
    }

    @Transactional
    public ClienteResponseDTO update(Integer id, ClienteRequestDTO dto) {

        var opt = clienteRepository.findById(id);

        if (opt.isEmpty()) {
            throw new ResourceNotFoundException("Cliente não encontrado");
        }

        Cliente e = opt.get();

        String updatedName = dto.getName() == null ? e.getNome() : dto.getName().trim();
        String updatedDocument = dto.getDocument() == null ? e.getDocumento() : dto.getDocument().replaceAll("\\D", "");
        String updatedEmail = dto.getEmail() == null ? e.getEmail() : dto.getEmail().trim().toLowerCase();
        String updatedPhone = dto.getPhone() == null ? e.getTelefone() : dto.getPhone().replaceAll("\\D", "");

        boolean duplicate = clienteRepository.findByOrganizacaoId(e.getOrganizacaoId()).stream()
                .filter(client -> !client.getId().equals(e.getId()))
                .anyMatch(client ->
                        updatedName.equalsIgnoreCase(client.getNome())
                                || (updatedDocument != null && !updatedDocument.isEmpty() && updatedDocument.equals(client.getDocumento()))
                                || (updatedEmail != null && !updatedEmail.isEmpty() && updatedEmail.equalsIgnoreCase(client.getEmail()))
                                || (updatedPhone != null && !updatedPhone.isEmpty() && updatedPhone.equals(client.getTelefone())));
        if (duplicate) throw new ConflictException("Cliente já cadastrado com estes dados");

        if (dto.getName() != null) {
            e.setNome(dto.getName().trim());
        }

        if (dto.getEmail() != null) {
            e.setEmail(dto.getEmail().trim().toLowerCase());
        }

        if (dto.getPhone() != null) {
            e.setTelefone(dto.getPhone().replaceAll("\\D", ""));
        }

        if (dto.getDocument() != null) {
            e.setDocumento(dto.getDocument().replaceAll("\\D", ""));
        }

        if (dto.getIsAtivo() != null) {

            if (Boolean.FALSE.equals(dto.getIsAtivo())
                    && Boolean.TRUE.equals(e.getIsAtivo())) {

                e.setIsAtivo(false);
                e.setDataDesativacao(LocalDateTime.now());

            } else if (Boolean.TRUE.equals(dto.getIsAtivo())) {

                e.setIsAtivo(true);
                e.setDataDesativacao(null);
            }
        }

        Cliente saved = clienteRepository.save(e);

        ClienteResponseDTO resp = toDto(saved);

        return resp;
    }

    public List<ClienteResponseDTO> listAll() {
        return clienteRepository.findAll()
                .stream()
                .map(this::toDto)
                .toList();
    }

    public List<ClienteResponseDTO> listAll(Integer organizacaoId) {
        if (organizacaoId == null) return listAll();
        return clienteRepository.findByOrganizacaoId(organizacaoId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    public ClienteResponseDTO getById(Integer id) {
        return clienteRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Cliente não encontrado: id=" + id
                        )
                );
    }

    @Transactional
    public void delete(Integer id) {

        var opt = clienteRepository.findById(id);

        if (opt.isEmpty()) {
            throw new ResourceNotFoundException("Cliente não encontrado");
        }

        Cliente e = opt.get();

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
        r.setEmail(o.getEmail());
        r.setPhone(o.getTelefone());
        r.setDocument(o.getDocumento());
        r.setIsAtivo(o.getIsAtivo());
        r.setDataCadastro(o.getDataCriacao());
        r.setDataDesativacao(o.getDataDesativacao());

        return r;
    }
}
