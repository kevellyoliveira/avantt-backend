
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

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final ExistenceRepository existenceRepository;

    @PersistenceContext
    private EntityManager em;

    public UsuarioService(
            UsuarioRepository usuarioRepository,
            ExistenceRepository existenceRepository) {

        this.usuarioRepository = usuarioRepository;
        this.existenceRepository = existenceRepository;
    }

    /*
     * Método mantido para compatibilidade com chamadas antigas.
     * Não permite cadastrar usuário sem informar a organização.
     */
    @Transactional
    public UsuarioResponseDTO create(UsuarioRequestDTO dto) {

        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "É necessário informar a organização para cadastrar um usuário.");
    }

    @Transactional
    public UsuarioResponseDTO create(
            UsuarioRequestDTO dto,
            Integer organizacaoId) {

        if (organizacaoId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A organização do usuário autenticado não foi identificada.");
        }

        if (usuarioRepository.existsByEmail(dto.getEmail())) {
            throw new ConflictException("Email já cadastrado");
        }

        Usuario entity = new Usuario();

        entity.setOrganizacaoId(organizacaoId);
        entity.setNome(dto.getName());
        entity.setEmail(dto.getEmail());
        entity.setCargo(dto.getRole());
        entity.setPapel(dto.getPapel());

        if (!existenceRepository.existsPerfilById(dto.getPerfilId())) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Perfil não encontrado: id=" + dto.getPerfilId());
        }

        entity.setPerfilId(dto.getPerfilId());

        Usuario saved = usuarioRepository.save(entity);

        em.flush();
        em.refresh(saved);

        UsuarioResponseDTO f = new UsuarioResponseDTO();

        f.setId(
                saved.getId() == null
                        ? null
                        : String.valueOf(saved.getId()));

        f.setName(saved.getNome());
        f.setEmail(saved.getEmail());
        f.setRole(saved.getCargo());
        f.setPapel(saved.getPapel());

        f.setAvatar(
                dto.getAvatar() == null
                        ? generateInitials(saved.getNome())
                        : dto.getAvatar());

        f.setColor(
                dto.getColor() == null
                        ? "#2563eb"
                        : dto.getColor());

        f.setProjects(
                dto.getProjects() == null
                        ? new java.util.ArrayList<>()
                        : dto.getProjects());

        f.setTasks(
                dto.getTasks() == null
                        ? new TasksDTO(0, 0, 0, 0)
                        : dto.getTasks());

        f.setWorkload(
                dto.getWorkload() == null
                        ? 0
                        : dto.getWorkload());

        return f;
    }

    @Transactional
    public UsuarioResponseDTO update(
            Integer id,
            UsuarioRequestDTO dto) {

        Usuario u = usuarioRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Usuário não encontrado"));

        if (dto.getEmail() != null
                && !dto.getEmail().equals(u.getEmail())) {

            if (usuarioRepository.existsByEmail(dto.getEmail())) {
                throw new ConflictException("Email já cadastrado");
            }

            u.setEmail(dto.getEmail());
        }

        if (dto.getName() != null) {
            u.setNome(dto.getName());
        }

        if (dto.getRole() != null) {
            u.setCargo(dto.getRole());
        }
        if (dto.getPapel() != null) {
            u.setPapel(dto.getPapel());
        }

        // Outros campos do frontend não são persistidos nesta entidade.

        Usuario saved = usuarioRepository.save(u);

        UsuarioResponseDTO f = new UsuarioResponseDTO();

        f.setId(
                saved.getId() == null
                        ? null
                        : String.valueOf(saved.getId()));

        f.setName(saved.getNome());
        f.setEmail(saved.getEmail());
        f.setRole(saved.getCargo());
        f.setPapel(saved.getPapel());

        f.setAvatar(
                dto.getAvatar() == null
                        ? generateInitials(saved.getNome())
                        : dto.getAvatar());

        f.setColor(
                dto.getColor() == null
                        ? "#2563eb"
                        : dto.getColor());

        f.setProjects(
                dto.getProjects() == null
                        ? new java.util.ArrayList<>()
                        : dto.getProjects());

        f.setTasks(
                dto.getTasks() == null
                        ? new TasksDTO(0, 0, 0, 0)
                        : dto.getTasks());

        f.setWorkload(
                dto.getWorkload() == null
                        ? 0
                        : dto.getWorkload());

        return f;
    }

    public java.util.List<UsuarioResponseDTO> listAll() {
        return listAll(null);
    }

    public java.util.List<UsuarioResponseDTO> listAll(Integer projetoId) {

        java.util.Map<Integer, UsuarioResponseDTO> map =
                new java.util.LinkedHashMap<>();

        if (projetoId != null) {

            String sql =
                    "SELECT u.id, u.nome, u.email, u.cargo, u.papel FROM usuario u "
                            + "JOIN projeto_usuario pu ON pu.usuario_id = u.id "
                            + "WHERE pu.projeto_id = :pid AND u.is_ativo = TRUE "
                            + "ORDER BY u.nome";

            var q = em.createNativeQuery(sql);
            q.setParameter("pid", projetoId);

            @SuppressWarnings("unchecked")
            java.util.List<Object[]> rows = q.getResultList();

            for (Object[] row : rows) {

                Integer uid = row[0] == null
                        ? null
                        : ((Number) row[0]).intValue();

                String nome = row[1] == null ? "" : row[1].toString();
                String email = row[2] == null ? "" : row[2].toString();
                String cargo = row[3] == null ? "" : row[3].toString();
                String papel = row[4] == null ? null : row[4].toString();

                UsuarioResponseDTO u = new UsuarioResponseDTO();

                u.setId(uid == null ? "" : String.valueOf(uid));
                u.setName(nome);
                u.setEmail(email);
                u.setRole(cargo);
                u.setPapel(papel);
                u.setAvatar(generateInitials(nome));
                u.setColor("#4B7BF5");
                u.setTasks(new TasksDTO(0, 0, 0, 0));
                u.setProjects(new java.util.ArrayList<>());
                u.setWorkload(0);

                var projOpt = em.createNativeQuery(
                                "SELECT nome FROM projeto WHERE id = :pid")
                        .setParameter("pid", projetoId)
                        .getResultList();

                if (!projOpt.isEmpty()) {
                    Object pname = ((java.util.List) projOpt).get(0);
                    String projectName = pname == null ? "" : pname.toString();
                    u.getProjects().add(projectName);
                }

                map.put(uid, u);
            }

            return new java.util.ArrayList<>(map.values());
        }

        // Listagem geral: usuários e seus projetos.
        String sql =
                "SELECT u.id AS usuario_id, u.nome AS usuario_nome, "
                        + "u.email, u.cargo, u.papel, u.is_ativo, "
                        + "p.id AS projeto_id, p.nome AS projeto_nome "
                        + "FROM usuario u "
                        + "LEFT JOIN projeto_usuario pu ON pu.usuario_id = u.id "
                        + "LEFT JOIN projeto p ON p.id = pu.projeto_id "
                        + "WHERE u.is_ativo = TRUE "
                        + "ORDER BY u.nome, p.nome";

        var q = em.createNativeQuery(sql);

        @SuppressWarnings("unchecked")
        java.util.List<Object[]> rows = q.getResultList();

        for (Object[] row : rows) {

            Integer uid = row[0] == null
                    ? null
                    : ((Number) row[0]).intValue();

            String nome = row[1] == null ? "" : row[1].toString();
            String email = row[2] == null ? "" : row[2].toString();
            String cargo = row[3] == null ? "" : row[3].toString();
            String papel = row[4] == null ? null : row[4].toString();
            String projetoNome = row[7] == null
                    ? null
                    : row[7].toString();

            if (!map.containsKey(uid)) {

                UsuarioResponseDTO u = new UsuarioResponseDTO();

                u.setId(uid == null ? "" : String.valueOf(uid));
                u.setName(nome);
                u.setEmail(email);
                u.setRole(cargo);
                u.setPapel(papel);
                u.setAvatar(generateInitials(nome));
                u.setColor("#4B7BF5");
                u.setTasks(new TasksDTO(0, 0, 0, 0));
                u.setProjects(new java.util.ArrayList<>());
                u.setWorkload(0);

                map.put(uid, u);
            }

            if (projetoNome != null && !projetoNome.isBlank()) {

                var u = map.get(uid);

                if (!u.getProjects().contains(projetoNome)) {
                    u.getProjects().add(projetoNome);
                }
            }
        }

        return new java.util.ArrayList<>(map.values());
    }

    public java.util.List<UsuarioResponseDTO> listForOrganization(
            Integer organizacaoId,
            Integer projetoId,
            java.util.List<Integer> projetosAcessiveis) {

        if (organizacaoId == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Não foi possível identificar a organização do usuário."
            );
        }

        java.util.List<Integer> idsAcessiveis = projetosAcessiveis == null
                ? java.util.Collections.emptyList()
                : projetosAcessiveis;

        if (projetoId != null && !idsAcessiveis.contains(projetoId)) {
            return java.util.Collections.emptyList();
        }

        StringBuilder sql = new StringBuilder(
                "SELECT u.id, u.nome, u.email, u.cargo, u.papel, p.id, p.nome "
                        + "FROM usuario u "
                        + "LEFT JOIN projeto_usuario pu ON pu.usuario_id = u.id "
                        + "LEFT JOIN projeto p ON p.id = pu.projeto_id "
                        + "WHERE u.is_ativo = TRUE "
                        + "AND u.organizacao_id = :organizacaoId "
        );

        if (projetoId != null) {
            sql.append("AND pu.projeto_id = :projetoId ");
        }
        sql.append("ORDER BY u.nome, p.nome");

        var query = em.createNativeQuery(sql.toString())
                .setParameter("organizacaoId", organizacaoId);

        if (projetoId != null) {
            query.setParameter("projetoId", projetoId);
        }

        @SuppressWarnings("unchecked")
        java.util.List<Object[]> rows = query.getResultList();

        java.util.Map<Integer, UsuarioResponseDTO> usuarios =
                new java.util.LinkedHashMap<>();

        for (Object[] row : rows) {
            Integer usuarioId = row[0] == null
                    ? null
                    : ((Number) row[0]).intValue();
            String nome = row[1] == null ? "" : row[1].toString();
            String email = row[2] == null ? "" : row[2].toString();
            String cargo = row[3] == null ? "" : row[3].toString();
            String papel = row[4] == null ? null : row[4].toString();
            Integer projetoRelacionadoId = row[5] == null
                    ? null
                    : ((Number) row[5]).intValue();
            String projetoNome = row[6] == null ? null : row[6].toString();

            UsuarioResponseDTO usuario = usuarios.get(usuarioId);
            if (usuario == null) {
                usuario = new UsuarioResponseDTO();
                usuario.setId(usuarioId == null ? "" : String.valueOf(usuarioId));
                usuario.setName(nome);
                usuario.setEmail(email);
                usuario.setRole(cargo);
                usuario.setPapel(papel);
                usuario.setOrganizacaoId(organizacaoId);
                usuario.setAvatar(generateInitials(nome));
                usuario.setColor("#4B7BF5");
                usuario.setTasks(new TasksDTO(0, 0, 0, 0));
                usuario.setProjects(new java.util.ArrayList<>());
                usuario.setWorkload(0);
                usuarios.put(usuarioId, usuario);
            }

            if (projetoRelacionadoId != null
                    && idsAcessiveis.contains(projetoRelacionadoId)
                    && projetoNome != null
                    && !projetoNome.isBlank()
                    && !usuario.getProjects().contains(projetoNome)) {
                usuario.getProjects().add(projetoNome);
            }
        }

        return new java.util.ArrayList<>(usuarios.values());
    }

    private String generateInitials(String nome) {

        if (nome == null || nome.trim().isEmpty()) {
            return "";
        }

        String[] parts = nome.trim().split("\\s+");

        if (parts.length == 1) {
            return parts[0]
                    .substring(0, Math.min(2, parts[0].length()))
                    .toUpperCase();
        }

        StringBuilder sb = new StringBuilder();

        sb.append(parts[0].substring(0, 1));

        if (parts.length >= 3) {
            sb.append(parts[1].substring(0, 1));
            sb.append(parts[parts.length - 1].substring(0, 1));
        } else {
            sb.append(parts[parts.length - 1].substring(0, 1));
        }

        return sb.toString().toUpperCase();
    }
}
