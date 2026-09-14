package com.avantt_backend.service;

import com.avantt_backend.dto.TasksDTO;
import com.avantt_backend.dto.UsuarioResponseDTO;
import com.avantt_backend.dto.UsuarioRequestDTO;
import com.avantt_backend.entity.Usuario;
import com.avantt_backend.exception.ConflictException;
import com.avantt_backend.exception.ResourceNotFoundException;
import com.avantt_backend.repository.ExistenceRepository;
import com.avantt_backend.repository.UsuarioRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final ExistenceRepository existenceRepository;

    @PersistenceContext
    private EntityManager em;

    public UsuarioService(UsuarioRepository usuarioRepository, ExistenceRepository existenceRepository) {
        this.usuarioRepository = usuarioRepository;
        this.existenceRepository = existenceRepository;
    }

    @Transactional
    public UsuarioResponseDTO create(UsuarioRequestDTO dto) {
        // email unique check
        if (usuarioRepository.existsByEmail(dto.getEmail())) {
            throw new ConflictException("Email já cadastrado");
        }

        Usuario entity = new Usuario();
        entity.setNome(dto.getName());
        entity.setEmail(dto.getEmail());
        entity.setCargo(dto.getRole());
        // perfilId and organizacaoId are not provided by frontend; leave null or set defaults if needed

        Usuario saved = usuarioRepository.save(entity);

        // refresh to get DB generated columns (data_cadastro)
        em.flush();
        em.refresh(saved);

        // build frontend DTO including fields sent by frontend
        UsuarioResponseDTO f = new UsuarioResponseDTO();
        f.setId(saved.getId() == null ? null : String.valueOf(saved.getId()));
        f.setName(saved.getNome());
        f.setEmail(saved.getEmail());
        f.setRole(saved.getCargo());
        f.setAvatar(dto.getAvatar() == null ? generateInitials(saved.getNome()) : dto.getAvatar());
        f.setColor(dto.getColor() == null ? "#2563eb" : dto.getColor());
        f.setProjects(dto.getProjects() == null ? new java.util.ArrayList<>() : dto.getProjects());
        f.setTasks(dto.getTasks() == null ? new TasksDTO(0,0,0,0) : dto.getTasks());
        f.setWorkload(dto.getWorkload() == null ? 0 : dto.getWorkload());
        return f;
    }

    @Transactional
    public UsuarioResponseDTO update(Integer id, UsuarioRequestDTO dto) {
        Usuario u = usuarioRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        if (dto.getEmail() != null && !dto.getEmail().equals(u.getEmail())) {
            if (usuarioRepository.existsByEmail(dto.getEmail())) {
                throw new ConflictException("Email já cadastrado");
            }
            u.setEmail(dto.getEmail());
        }

        if (dto.getName() != null) u.setNome(dto.getName());
        if (dto.getRole() != null) u.setCargo(dto.getRole());
        // other frontend-only fields are not persisted but returned

        Usuario saved = usuarioRepository.save(u);

        UsuarioResponseDTO f = new UsuarioResponseDTO();
        f.setId(saved.getId() == null ? null : String.valueOf(saved.getId()));
        f.setName(saved.getNome());
        f.setEmail(saved.getEmail());
        f.setRole(saved.getCargo());
        f.setAvatar(dto.getAvatar() == null ? generateInitials(saved.getNome()) : dto.getAvatar());
        f.setColor(dto.getColor() == null ? "#2563eb" : dto.getColor());
        f.setProjects(dto.getProjects() == null ? new java.util.ArrayList<>() : dto.getProjects());
        f.setTasks(dto.getTasks() == null ? new TasksDTO(0,0,0,0) : dto.getTasks());
        f.setWorkload(dto.getWorkload() == null ? 0 : dto.getWorkload());
        return f;
    }

    // kept single response DTO approach: public mapping helpers can return frontend DTO
    // If callers need the internal full response, we can add methods later. For now the app uses UsuarioFrontendDTO.

    public java.util.List<UsuarioResponseDTO> listAll() {
        java.util.List<Usuario> all = usuarioRepository.findAll();
        java.util.List<UsuarioResponseDTO> out = new java.util.ArrayList<>();
        for (Usuario u : all) {
            UsuarioResponseDTO f = new UsuarioResponseDTO();
            f.setId(u.getId() == null ? null : String.valueOf(u.getId()));
            f.setName(u.getNome());
            f.setEmail(u.getEmail());
            // role maps to cargo
            f.setRole(u.getCargo());
            // avatar: initials from name
            f.setAvatar(generateInitials(u.getNome()));
            // color: fixed default (could be improved to be deterministic)
            f.setColor("#2563eb");
            // tasks: default zeros for now
            f.setTasks(new TasksDTO(0,0,0,0));
            f.setProjects(new java.util.ArrayList<>());
            f.setWorkload(0);
            out.add(f);
        }
        return out;
    }

    private String generateInitials(String nome) {
        if (nome == null || nome.trim().isEmpty()) return "";
        String[] parts = nome.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        }
        String first = parts[0].substring(0,1);
        String last = parts[parts.length-1].substring(0,1);
        return (first + last).toUpperCase();
    }
}
