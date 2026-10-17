package com.avantt_backend.service;

import com.avantt_backend.dto.TasksDTO;
import com.avantt_backend.dto.UsuarioResponseDTO;
import com.avantt_backend.dto.UsuarioRequestDTO;
import com.avantt_backend.entity.Usuario;
import com.avantt_backend.entity.ProjetoUsuario;
import com.avantt_backend.entity.ProjetoUsuarioId;
import com.avantt_backend.exception.ConflictException;
import com.avantt_backend.exception.ResourceNotFoundException;
import com.avantt_backend.repository.ExistenceRepository;
import com.avantt_backend.repository.UsuarioRepository;
import com.avantt_backend.repository.ProjetoUsuarioRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final ExistenceRepository existenceRepository;
    private final ProjetoUsuarioRepository projetoUsuarioRepository;

    @PersistenceContext
    private EntityManager em;

    public UsuarioService(UsuarioRepository usuarioRepository, ExistenceRepository existenceRepository,
                          ProjetoUsuarioRepository projetoUsuarioRepository) {
        this.usuarioRepository = usuarioRepository;
        this.existenceRepository = existenceRepository;
        this.projetoUsuarioRepository = projetoUsuarioRepository;
    }

    @Transactional
    public UsuarioResponseDTO create(UsuarioRequestDTO dto) {
        return create(dto, null);
    }

    @Transactional
    public UsuarioResponseDTO create(UsuarioRequestDTO dto, Integer organizacaoId) {
        // email unique check
        if (usuarioRepository.existsByEmail(dto.getEmail())) {
            throw new ConflictException("Email já cadastrado");
        }

        Usuario entity = new Usuario();
        entity.setNome(dto.getName());
        entity.setEmail(dto.getEmail());
        entity.setCargo(dto.getRole());
        entity.setOrganizacaoId(organizacaoId);
        // perfilId existence is validated via ExistenceRepository; DTO validation ensures non-null
        if (!existenceRepository.existsPerfilById(dto.getPerfilId())) throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Perfil não encontrado: id=" + dto.getPerfilId());
        entity.setPerfilId(dto.getPerfilId());

        // organizacaoId is optional

        Usuario saved = usuarioRepository.save(entity);

        // refresh to get DB generated columns (data_cadastro)
        em.flush();
        em.refresh(saved);

        if (dto.getProjectIds() != null && !dto.getProjectIds().isEmpty()) {
            var requested = new java.util.HashSet<>(dto.getProjectIds());
            String projectScope = organizacaoId == null
                    ? "SELECT COUNT(*) FROM projeto WHERE id IN (:ids)"
                    : "SELECT COUNT(*) FROM projeto WHERE id IN (:ids) AND organizacao_id = :oid";
            var projectQuery = em.createNativeQuery(projectScope).setParameter("ids", requested);
            if (organizacaoId != null) projectQuery.setParameter("oid", organizacaoId);
            if (((Number) projectQuery.getSingleResult()).intValue() != requested.size()) {
                throw new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.BAD_REQUEST,
                        "Um ou mais projetos não pertencem à organização do usuário");
            }
            for (Integer projectId : requested) {
                projetoUsuarioRepository.save(new ProjetoUsuario(new ProjetoUsuarioId(projectId, saved.getId()), null));
            }
        }

        // Build the response from persisted project relationships.
        return toResponse(saved, dto);
    }

    @Transactional
    public UsuarioResponseDTO update(Integer id, UsuarioRequestDTO dto) {
        return update(id, dto, null);
    }

    @Transactional
    public UsuarioResponseDTO update(Integer id, UsuarioRequestDTO dto, Integer organizacaoId) {
        Usuario u = usuarioRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
        if (organizacaoId != null && !organizacaoId.equals(u.getOrganizacaoId())) {
            throw new ResourceNotFoundException("Usuário não encontrado");
        }

        if (dto.getEmail() != null && !dto.getEmail().equals(u.getEmail())) {
            if (usuarioRepository.existsByEmail(dto.getEmail())) {
                throw new ConflictException("Email já cadastrado");
            }
            u.setEmail(dto.getEmail());
        }

        if (dto.getName() != null) u.setNome(dto.getName());
        if (dto.getRole() != null) u.setCargo(dto.getRole());
        if (dto.getProjectIds() != null) {
            var current = new java.util.HashSet<Integer>();
            projetoUsuarioRepository.findByIdUsuarioId(id).forEach(link -> current.add(link.getId().getProjetoId()));
            var requested = new java.util.HashSet<>(dto.getProjectIds());
            requested.remove(null);
            if (!requested.isEmpty()) {
                String projectScope = organizacaoId == null
                        ? "SELECT COUNT(*) FROM projeto WHERE id IN (:ids)"
                        : "SELECT COUNT(*) FROM projeto WHERE id IN (:ids) AND organizacao_id = :oid";
                var projectQuery = em.createNativeQuery(projectScope).setParameter("ids", requested);
                if (organizacaoId != null) projectQuery.setParameter("oid", organizacaoId);
                Number validProjects = (Number) projectQuery.getSingleResult();
                if (validProjects.intValue() != requested.size()) {
                    throw new org.springframework.web.server.ResponseStatusException(
                            org.springframework.http.HttpStatus.BAD_REQUEST,
                            "Um ou mais projetos não pertencem à organização do usuário");
                }
            }
            for (Integer projectId : requested) {
                if (!current.contains(projectId)) {
                    ProjetoUsuario link = new ProjetoUsuario();
                    link.setId(new ProjetoUsuarioId(projectId, id));
                    projetoUsuarioRepository.save(link);
                }
            }
            for (Integer projectId : current) {
                if (!requested.contains(projectId)) {
                    projetoUsuarioRepository.deleteById(new ProjetoUsuarioId(projectId, id));
                }
            }
        }

        Usuario saved = usuarioRepository.save(u);

        return toResponse(saved, dto);
    }

    private UsuarioResponseDTO toResponse(Usuario saved, UsuarioRequestDTO dto) {
        UsuarioResponseDTO f = new UsuarioResponseDTO();
        f.setId(saved.getId() == null ? null : String.valueOf(saved.getId()));
        f.setName(saved.getNome());
        f.setEmail(saved.getEmail());
        f.setRole(saved.getCargo());
        f.setAvatar(dto.getAvatar() == null ? generateInitials(saved.getNome()) : dto.getAvatar());
        f.setColor(dto.getColor() == null ? "#2563eb" : dto.getColor());
        var projectIds = projetoUsuarioRepository.findByIdUsuarioId(saved.getId()).stream()
                .map(link -> String.valueOf(link.getId().getProjetoId())).toList();
        var projectNames = new java.util.ArrayList<String>();
        for (String projectId : projectIds) {
            var names = em.createNativeQuery("SELECT nome FROM projeto WHERE id = :pid")
                    .setParameter("pid", Integer.valueOf(projectId)).getResultList();
            if (!names.isEmpty() && names.get(0) != null) projectNames.add(String.valueOf(names.get(0)));
        }
        f.setProjectIds(projectIds);
        f.setProjects(projectNames);
        f.setTasks(dto.getTasks() == null ? new TasksDTO(0,0,0,0) : dto.getTasks());
        f.setWorkload(dto.getWorkload() == null ? 0 : dto.getWorkload());
        f.setOrganizationId(saved.getOrganizacaoId());
        return f;
    }

    // kept single response DTO approach: public mapping helpers can return frontend DTO
    // If callers need the internal full response, we can add methods later. For now the app uses UsuarioFrontendDTO.

    public java.util.List<UsuarioResponseDTO> listAll() {
        return listAll(null, null);
    }

    public java.util.List<UsuarioResponseDTO> listAll(Integer projetoId) {
        return listAll(projetoId, null);
    }

    public java.util.List<UsuarioResponseDTO> listAll(Integer projetoId, Integer organizacaoId) {
        // if projetoId provided: return users that belong to that project (active users)
        // otherwise return all active users with their projects (LEFT JOIN)
        java.util.Map<Integer, UsuarioResponseDTO> map = new java.util.LinkedHashMap<>();

        if (projetoId != null) {
            // query users that are linked to the given project
            String sql = "SELECT u.id, u.nome, u.email, u.cargo FROM usuario u " +
                    "JOIN projeto_usuario pu ON pu.usuario_id = u.id " +
                    "JOIN projeto p ON p.id = pu.projeto_id " +
                    "WHERE pu.projeto_id = :pid AND u.is_ativo = TRUE " +
                    (organizacaoId == null ? "" : "AND u.organizacao_id = :oid AND p.organizacao_id = :oid ") +
                    "ORDER BY u.nome";
            var q = em.createNativeQuery(sql);
            q.setParameter("pid", projetoId);
            if (organizacaoId != null) q.setParameter("oid", organizacaoId);
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
                u.setOrganizationId(organizacaoId);
                if (organizacaoId != null) {
                    var organization = em.createNativeQuery("SELECT nome_fantasia FROM organizacao WHERE id = :oid")
                            .setParameter("oid", organizacaoId).getResultList();
                    if (!organization.isEmpty()) u.setOrganizationName(String.valueOf(organization.get(0)));
                }
                // fetch project name to include in projects array
                var projOpt = em.createNativeQuery("SELECT nome FROM projeto WHERE id = :pid").setParameter("pid", projetoId).getResultList();
                if (!projOpt.isEmpty()) {
                    Object pname = ((java.util.List)projOpt).get(0);
                    String projectName = pname == null ? "" : pname.toString();
                    u.getProjects().add(projectName);
                }

                map.put(uid, u);
            }

            map.values().forEach(this::enrichProjectIds);
            return new java.util.ArrayList<>(map.values());
        }

        // geral: LEFT JOIN users and projects
        String sql = "SELECT u.id AS usuario_id, u.nome AS usuario_nome, u.email, u.cargo, u.is_ativo, p.id AS projeto_id, p.nome AS projeto_nome " +
                "FROM usuario u " +
                "LEFT JOIN projeto_usuario pu ON pu.usuario_id = u.id " +
                "LEFT JOIN projeto p ON p.id = pu.projeto_id " +
                "WHERE u.is_ativo = TRUE " +
                (organizacaoId == null ? "" : "AND u.organizacao_id = :oid ") +
                "ORDER BY u.nome, p.nome";
        var q = em.createNativeQuery(sql);
        if (organizacaoId != null) q.setParameter("oid", organizacaoId);
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
            Usuario entity = usuarioRepository.findById(uid).orElse(null);
            if (entity != null) {
                UsuarioResponseDTO response = map.get(uid);
                response.setOrganizationId(entity.getOrganizacaoId());
                if (entity.getOrganizacaoId() != null) {
                    var organization = em.createNativeQuery("SELECT nome_fantasia FROM organizacao WHERE id = :oid")
                            .setParameter("oid", entity.getOrganizacaoId()).getResultList();
                    if (!organization.isEmpty()) response.setOrganizationName(String.valueOf(organization.get(0)));
                }
            }
        }

        map.values().forEach(this::enrichProjectIds);
        return new java.util.ArrayList<>(map.values());
    }

    private void enrichProjectIds(UsuarioResponseDTO response) {
        Integer userId = Integer.valueOf(response.getId());
        response.setProjectIds(projetoUsuarioRepository.findByIdUsuarioId(userId).stream()
                .map(link -> String.valueOf(link.getId().getProjetoId())).toList());
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
