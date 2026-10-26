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
        // perfilId existence is validated via ExistenceRepository; DTO validation ensures non-null
        if (!existenceRepository.existsPerfilById(dto.getPerfilId())) throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Perfil não encontrado: id=" + dto.getPerfilId());
        entity.setPerfilId(dto.getPerfilId());

        // organizacaoId is optional

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
        return listAll(null);
    }

    public java.util.List<UsuarioResponseDTO> listAll(Integer projetoId) {
        // if projetoId provided: return users that belong to that project (active users)
        // otherwise return all active users with their projects (LEFT JOIN)
        java.util.Map<Integer, UsuarioResponseDTO> map = new java.util.LinkedHashMap<>();

        if (projetoId != null) {
            // query users that are linked to the given project
            String sql = "SELECT u.id, u.nome, u.email, u.cargo FROM usuario u " +
                    "JOIN projeto_usuario pu ON pu.usuario_id = u.id " +
                    "WHERE pu.projeto_id = :pid AND u.is_ativo = TRUE " +
                    "ORDER BY u.nome";
            var q = em.createNativeQuery(sql);
            q.setParameter("pid", projetoId);
            @SuppressWarnings("unchecked")
            java.util.List<Object[]> rows = q.getResultList();
            for (Object[] row : rows) {
                Integer uid = row[0] == null ? null : ((Number) row[0]).intValue();
                String nome = row[1] == null ? "" : row[1].toString();
                String email = row[2] == null ? "" : row[2].toString();
                String cargo = row[3] == null ? "" : row[3].toString();

                UsuarioResponseDTO u = new UsuarioResponseDTO();
                u.setId(uid == null ? "" : String.valueOf(uid));
                u.setName(nome);
                u.setEmail(email);
                u.setRole(cargo == null ? "" : cargo);
                u.setAvatar(generateInitials(nome));
                u.setColor("#4B7BF5");
                u.setTasks(new TasksDTO(0,0,0,0));
                u.setProjects(new java.util.ArrayList<>());
                u.setWorkload(0);
                // fetch project name to include in projects array
                var projOpt = em.createNativeQuery("SELECT nome FROM projeto WHERE id = :pid").setParameter("pid", projetoId).getResultList();
                if (!projOpt.isEmpty()) {
                    Object pname = ((java.util.List)projOpt).get(0);
                    String projectName = pname == null ? "" : pname.toString();
                    u.getProjects().add(projectName);
                }

                map.put(uid, u);
            }

            return new java.util.ArrayList<>(map.values());
        }

        // geral: LEFT JOIN users and projects
        String sql = "SELECT u.id AS usuario_id, u.nome AS usuario_nome, u.email, u.cargo, u.is_ativo, p.id AS projeto_id, p.nome AS projeto_nome " +
                "FROM usuario u " +
                "LEFT JOIN projeto_usuario pu ON pu.usuario_id = u.id " +
                "LEFT JOIN projeto p ON p.id = pu.projeto_id " +
                "WHERE u.is_ativo = TRUE " +
                "ORDER BY u.nome, p.nome";
        var q = em.createNativeQuery(sql);
        @SuppressWarnings("unchecked")
        java.util.List<Object[]> rows = q.getResultList();

        for (Object[] row : rows) {
            Integer uid = row[0] == null ? null : ((Number) row[0]).intValue();
            String nome = row[1] == null ? "" : row[1].toString();
            String email = row[2] == null ? "" : row[2].toString();
            String cargo = row[3] == null ? "" : row[3].toString();
            // row[4] is is_ativo
            String projetoNome = row[6] == null ? null : row[6].toString();

            if (!map.containsKey(uid)) {
                UsuarioResponseDTO u = new UsuarioResponseDTO();
                u.setId(uid == null ? "" : String.valueOf(uid));
                u.setName(nome == null ? "" : nome);
                u.setEmail(email == null ? "" : email);
                u.setRole(cargo == null ? "" : cargo);
                u.setAvatar(generateInitials(nome));
                u.setColor("#4B7BF5");
                u.setTasks(new TasksDTO(0,0,0,0));
                u.setProjects(new java.util.ArrayList<>());
                u.setWorkload(0);
                map.put(uid, u);
            }

            if (projetoNome != null && !projetoNome.isBlank()) {
                var u = map.get(uid);
                if (!u.getProjects().contains(projetoNome)) u.getProjects().add(projetoNome);
            }
        }

        return new java.util.ArrayList<>(map.values());
    }

    private String generateInitials(String nome) {
        if (nome == null || nome.trim().isEmpty()) return "";
        String[] parts = nome.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        }
        // build up to 3 initials: first, second (if present), last
        StringBuilder sb = new StringBuilder();
        sb.append(parts[0].substring(0,1));
        if (parts.length >= 3) {
            sb.append(parts[1].substring(0,1));
            sb.append(parts[parts.length-1].substring(0,1));
        } else {
            // two parts only: first + last
            sb.append(parts[parts.length-1].substring(0,1));
        }
        return sb.toString().toUpperCase();
    }
}
