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
            // normalize to digits only for storage and uniqueness checks
            normalizedCnpj = dto.getCnpj().replaceAll("\\D", "");
            if (organizacaoRepository.existsByCnpj(normalizedCnpj)) {
                throw new ConflictException("CNPJ já cadastrado");
            }
        }

        Organizacao e = new Organizacao();
        e.setNomeFantasia(dto.getNomeFantasia());
        e.setCnpj(normalizedCnpj == null ? null : normalizedCnpj);
        e.setEmailContato(dto.getEmailContato());
        e.setIsAtivo(dto.getIsAtivo() == null ? Boolean.TRUE : dto.getIsAtivo());
        if (Boolean.FALSE.equals(e.getIsAtivo())) e.setDataDesativacao(LocalDateTime.now());

        Organizacao saved = organizacaoRepository.save(e);
        // ensure DB generated fields are populated
        em.flush();
        em.refresh(saved);

        return toDto(saved);
    }

    @Transactional
    public OrganizacaoResponseDTO update(Integer id, OrganizacaoRequestDTO dto) {
        var opt = organizacaoRepository.findById(id);
        if (opt.isEmpty()) throw new ResourceNotFoundException("Organização não encontrada");
        Organizacao e = opt.get();

        if (dto.getNomeFantasia() != null) e.setNomeFantasia(dto.getNomeFantasia());
        if (dto.getCnpj() != null) {
            String normalized = dto.getCnpj().replaceAll("\\D", "");
            if (!normalized.equals(e.getCnpj())) {
                if (organizacaoRepository.existsByCnpj(normalized)) throw new ConflictException("CNPJ já cadastrado");
                e.setCnpj(normalized);
            }
        }
        if (dto.getEmailContato() != null) e.setEmailContato(dto.getEmailContato());
        if (dto.getIsAtivo() != null) {
            if (Boolean.FALSE.equals(dto.getIsAtivo()) && Boolean.TRUE.equals(e.getIsAtivo())) {
                e.setIsAtivo(false);
                e.setDataDesativacao(LocalDateTime.now());
            } else if (Boolean.TRUE.equals(dto.getIsAtivo())) {
                e.setIsAtivo(true);
                e.setDataDesativacao(null);
            }
        }

        Organizacao saved = organizacaoRepository.save(e);
        return toDto(saved);
    }

    public java.util.List<OrganizacaoResponseDTO> listAll() {
        return organizacaoRepository.findAll().stream().map(this::toDto).toList();
    }

    public OrganizacaoResponseDTO getById(Integer id) {
        return organizacaoRepository.findById(id).map(this::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Organização não encontrada: id=" + id));
    }

    private OrganizacaoResponseDTO toDto(Organizacao o) {
        OrganizacaoResponseDTO r = new OrganizacaoResponseDTO();
        r.setId(o.getId() == null ? null : String.valueOf(o.getId()));
        r.setName(o.getNomeFantasia());
        // populate frontend-friendly aliases
        r.setTradeName(o.getNomeFantasia());
        // other fields (legalName, contact, phone, createdAt) are not stored in this entity; leave null
        // Always return CNPJ as digits-only (no máscara)
        r.setCnpj(o.getCnpj() == null ? null : o.getCnpj().replaceAll("\\D", ""));
        r.setEmail(o.getEmailContato());
        r.setIsActive(o.getIsAtivo());
        r.setDataDesativacao(o.getDataDesativacao());
        return r;
    }
}
