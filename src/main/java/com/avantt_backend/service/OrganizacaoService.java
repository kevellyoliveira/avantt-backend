package com.avantt_backend.service;

import com.avantt_backend.dto.OrganizacaoRequestDTO;
import com.avantt_backend.dto.OrganizacaoResponseDTO;
import com.avantt_backend.entity.Organizacao;
import com.avantt_backend.exception.ConflictException;
import com.avantt_backend.exception.ResourceNotFoundException;
import com.avantt_backend.repository.OrganizacaoRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrganizacaoService {

    private final OrganizacaoRepository organizacaoRepository;

    @PersistenceContext
    private EntityManager em;

    public OrganizacaoService(OrganizacaoRepository organizacaoRepository) {
        this.organizacaoRepository = organizacaoRepository;
    }

    @Transactional
    public OrganizacaoResponseDTO create(OrganizacaoRequestDTO dto) {

        String normalizedCnpj = null;

        if (dto.getCnpj() != null) {

            normalizedCnpj = dto.getCnpj().replaceAll("\\D", "");

            if (organizacaoRepository.existsByCnpj(normalizedCnpj)) {
                throw new ConflictException("CNPJ já cadastrado");
            }
        }

        Organizacao organizacao = new Organizacao();

        organizacao.setNomeFantasia(dto.getNomeFantasia());
        organizacao.setCnpj(normalizedCnpj);
        organizacao.setEmailContato(dto.getEmailContato());

        organizacao.setIsAtivo(
                dto.getIsAtivo() == null
                        ? Boolean.TRUE
                        : dto.getIsAtivo()
        );

        if (Boolean.FALSE.equals(organizacao.getIsAtivo())) {
            organizacao.setDataDesativacao(LocalDateTime.now());
        }

        Organizacao saved = organizacaoRepository.save(organizacao);

        // Garante que os campos gerados pelo banco sejam carregados.
        em.flush();
        em.refresh(saved);

        return toDto(saved);
    }

    @Transactional
    public OrganizacaoResponseDTO update(Integer id, OrganizacaoRequestDTO dto) {

        var optional = organizacaoRepository.findById(id);

        if (optional.isEmpty()) {
            throw new ResourceNotFoundException("Organização não encontrada");
        }

        Organizacao organizacao = optional.get();

        if (dto.getNomeFantasia() != null) {
            organizacao.setNomeFantasia(dto.getNomeFantasia());
        }

        if (dto.getCnpj() != null) {

            String normalized = dto.getCnpj().replaceAll("\\D", "");

            if (!normalized.equals(organizacao.getCnpj())) {

                if (organizacaoRepository.existsByCnpj(normalized)) {
                    throw new ConflictException("CNPJ já cadastrado");
                }

                organizacao.setCnpj(normalized);
            }
        }

        if (dto.getEmailContato() != null) {
            organizacao.setEmailContato(dto.getEmailContato());
        }

        if (dto.getIsAtivo() != null) {

            if (Boolean.FALSE.equals(dto.getIsAtivo())
                    && Boolean.TRUE.equals(organizacao.getIsAtivo())) {

                organizacao.setIsAtivo(false);
                organizacao.setDataDesativacao(LocalDateTime.now());

            } else if (Boolean.TRUE.equals(dto.getIsAtivo())) {

                organizacao.setIsAtivo(true);
                organizacao.setDataDesativacao(null);
            }
        }

        Organizacao saved = organizacaoRepository.save(organizacao);

        return toDto(saved);
    }

    public List<OrganizacaoResponseDTO> listAll() {

        return organizacaoRepository.findAll()
                .stream()
                .map(this::toDto)
                .toList();
    }

    public OrganizacaoResponseDTO getById(Integer id) {

        return organizacaoRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Organização não encontrada: id=" + id
                        )
                );
    }

    private OrganizacaoResponseDTO toDto(Organizacao organizacao) {

        OrganizacaoResponseDTO response = new OrganizacaoResponseDTO();

        response.setId(
                organizacao.getId() == null
                        ? null
                        : String.valueOf(organizacao.getId())
        );

        response.setName(organizacao.getNomeFantasia());
        response.setTradeName(organizacao.getNomeFantasia());

        // Campos não existentes na entidade permanecem nulos.
        response.setCnpj(
                organizacao.getCnpj() == null
                        ? null
                        : organizacao.getCnpj().replaceAll("\\D", "")
        );

        response.setEmail(organizacao.getEmailContato());
        response.setIsActive(organizacao.getIsAtivo());
        response.setDataDesativacao(organizacao.getDataDesativacao());

        return response;
    }
}