
package com.avantt_backend.service;

import com.avantt_backend.dto.OrganizacaoRequestDTO;
import com.avantt_backend.dto.OrganizacaoResponseDTO;
import com.avantt_backend.entity.Organizacao;
import com.avantt_backend.exception.ConflictException;
import com.avantt_backend.exception.ResourceNotFoundException;
import com.avantt_backend.repository.OrganizacaoRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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

        String normalizedCnpj = normalizarCnpj(dto.getCnpj());

        if (normalizedCnpj != null
                && organizacaoRepository.existsByCnpj(normalizedCnpj)) {
            throw new ConflictException("CNPJ já cadastrado");
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

        em.flush();
        em.refresh(saved);

        return toDto(saved);
    }

    /**
     * Procura uma organização pelo CNPJ.
     *
     * Se já existir, reutiliza a organização encontrada.
     * Caso contrário, realiza o cadastro de uma nova organização.
     */
    @Transactional
    public OrganizacaoResponseDTO findOrCreateByCnpj(
            OrganizacaoRequestDTO dto) {

        String normalizedCnpj = normalizarCnpj(dto.getCnpj());

        if (normalizedCnpj == null || normalizedCnpj.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "CNPJ da organização é obrigatório."
            );
        }

        Organizacao existente =
                organizacaoRepository.findByCnpj(normalizedCnpj);

        if (existente != null) {
            if (Boolean.FALSE.equals(existente.getIsAtivo())) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "A organização informada está inativa."
                );
            }

            return toDto(existente);
        }

        dto.setCnpj(normalizedCnpj);

        return create(dto);
    }

    @Transactional
    public OrganizacaoResponseDTO update(
            Integer id,
            OrganizacaoRequestDTO dto) {

        var optional = organizacaoRepository.findById(id);

        if (optional.isEmpty()) {
            throw new ResourceNotFoundException(
                    "Organização não encontrada"
            );
        }

        Organizacao organizacao = optional.get();

        if (dto.getNomeFantasia() != null) {
            organizacao.setNomeFantasia(dto.getNomeFantasia());
        }

        if (dto.getCnpj() != null) {

            String normalized = normalizarCnpj(dto.getCnpj());

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

    private String normalizarCnpj(String cnpj) {

        if (cnpj == null) {
            return null;
        }

        String normalized = cnpj.replaceAll("\\D", "");

        return normalized.isBlank() ? null : normalized;
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
