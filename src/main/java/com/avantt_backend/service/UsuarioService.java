
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

import java.util.Locale;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final ExistenceRepository existenceRepository;
    private final ProjetoService projetoService;
    private final ConviteService conviteService;

    @PersistenceContext
    private EntityManager em;

    public UsuarioService(
            UsuarioRepository usuarioRepository,
            ExistenceRepository existenceRepository,
            ProjetoService projetoService,
            ConviteService conviteService) {

        this.usuarioRepository = usuarioRepository;
        this.existenceRepository = existenceRepository;
        this.projetoService = projetoService;
        this.conviteService = conviteService;
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

        String email = dto.getEmail().trim().toLowerCase(Locale.ROOT);

        if (usuarioRepository.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("Email já cadastrado");
        }

        Usuario entity = new Usuario();

        entity.setOrganizacaoId(organizacaoId);
        entity.setNome(dto.getName());
        entity.setEmail(email);
        entity.setCargo(dto.getRole());
        if (dto.getPapel() != null) {
            entity.setPapel(dto.getPapel().trim().toLowerCase(Locale.ROOT));
        }
        if (dto.getCpf() != null) {
            entity.setCpf(dto.getCpf());
        }
        if (dto.getPhone() != null) {
            entity.setTelefone(dto.getPhone());
        }

        if (!existenceRepository.existsPerfilById(dto.getPerfilId())) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Perfil não encontrado: id=" + dto.getPerfilId());
        }

        entity.setPerfilId(dto.getPerfilId());

        Usuario saved = usuarioRepository.save(entity);

        em.flush();
        em.refresh(saved);

        if (dto.getProjectIds() == null || dto.getProjectIds().isEmpty()) {
            conviteService.convidarSeNecessario(saved.getId());
        } else {
            projetoService.adicionarUsuarioAProjetos(
                    organizacaoId,
                    saved.getId(),
                    dto.getProjectIds());
        }

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
        f.setCpf(saved.getCpf());
        f.setPhone(saved.getTelefone());

        return f;
    }

    @Transactional
    public UsuarioResponseDTO update(
            Integer id,
            UsuarioRequestDTO dto) {

        Usuario u = usuarioRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Usuário não encontrado"));

        if (dto.getEmail() != null) {
            String email = dto.getEmail().trim().toLowerCase(Locale.ROOT);

            if (email.isBlank()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "E-mail é obrigatório.");
            }

            if (!email.equalsIgnoreCase(u.getEmail().trim())) {
                if (usuarioRepository.existsByEmailIgnoreCase(email)) {
                    throw new ConflictException("Email já cadastrado");
                }

                u.setEmail(email);
            }
        }

        if (dto.getName() != null) {
            u.setNome(dto.getName());
        }

        if (dto.getRole() != null) {
            u.setCargo(dto.getRole());
        }
        if (dto.getPapel() != null) {
            u.setPapel(dto.getPapel().trim().toLowerCase(Locale.ROOT));
        }
        if (dto.getCpf() != null) {
            u.setCpf(dto.getCpf().isBlank() ? null : dto.getCpf().trim());
        }
        if (dto.getPhone() != null) {
            u.setTelefone(dto.getPhone().isBlank() ? null : dto.getPhone().trim());
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
        f.setCpf(saved.getCpf());
        f.setPhone(saved.getTelefone());

        return f;
    }

    public java.util.List<UsuarioResponseDTO> listAll() {
        return listAll(null);
    }

    public java.util.List<UsuarioResponseDTO> listAvailable() {
        String sql =
                "SELECT u.id, u.nome, u.email, u.cargo, u.papel "
                        + "FROM usuario u "
                        + "LEFT JOIN projeto_usuario pu ON pu.usuario_id = u.id "
                        + "WHERE pu.usuario_id IS NULL "
                        + "AND u.is_ativo = TRUE "
                        + "AND u.primeiro_acesso_validado = FALSE "
                        + "ORDER BY u.nome";

        var q = em.createNativeQuery(sql);

        @SuppressWarnings("unchecked")
        java.util.List<Object[]> rows = q.getResultList();
        java.util.List<UsuarioResponseDTO> available =
                new java.util.ArrayList<>();

        for (Object[] row : rows) {
            Integer uid = row[0] == null ? null : ((Number) row[0]).intValue();
            if (uid == null) continue;

            UsuarioResponseDTO u = new UsuarioResponseDTO();
            u.setId(String.valueOf(uid));
            u.setName(row[1] == null ? "" : row[1].toString());
            u.setEmail(row[2] == null ? "" : row[2].toString());
            u.setRole(row[3] == null ? "Colaborador" : row[3].toString());
            u.setPapel(row[4] == null ? null : row[4].toString());
            u.setAvatar(generateInitials(u.getName()));
            u.setColor("#4B7BF5");
            u.setTasks(new TasksDTO(0, 0, 0, 0));
            u.setProjects(new java.util.ArrayList<>());
            u.setWorkload(0);
            available.add(u);
        }

        return available;
    }

    public java.util.List<UsuarioResponseDTO> listAvailable(String email) {
        String sql =
                "SELECT u.id, u.nome, u.email, u.cargo, u.papel "
                        + "FROM usuario u "
                        + "LEFT JOIN projeto_usuario pu ON pu.usuario_id = u.id "
                        + "WHERE pu.usuario_id IS NULL "
                        + "AND u.is_ativo = TRUE "
                        + "AND u.primeiro_acesso_validado = FALSE "
                        + "AND LOWER(u.email) = LOWER(:email) "
                        + "ORDER BY u.nome";

        var query = em.createNativeQuery(sql);
        query.setParameter("email", email.trim());

        @SuppressWarnings("unchecked")
        java.util.List<Object[]> rows = query.getResultList();
        java.util.List<UsuarioResponseDTO> available =
                new java.util.ArrayList<>();

        for (Object[] row : rows) {
            Integer id = row[0] == null ? null : ((Number) row[0]).intValue();
            if (id == null) continue;

            UsuarioResponseDTO user = new UsuarioResponseDTO();
            user.setId(String.valueOf(id));
            user.setName(row[1] == null ? "" : row[1].toString());
            user.setEmail(row[2] == null ? "" : row[2].toString());
            user.setRole(row[3] == null ? "Colaborador" : row[3].toString());
            user.setPapel(row[4] == null ? null : row[4].toString());
            user.setAvatar(generateInitials(user.getName()));
            user.setColor("#4B7BF5");
            user.setTasks(new TasksDTO(0, 0, 0, 0));
            user.setProjects(new java.util.ArrayList<>());
            user.setWorkload(0);
            available.add(user);
        }

        return available;
    }

    public java.util.List<UsuarioResponseDTO> listAll(
            Integer projetoId,
            Integer organizacaoId) {
        if (organizacaoId == null) return listAll(projetoId);

        java.util.Map<Integer, UsuarioResponseDTO> map =
                new java.util.LinkedHashMap<>();
        String sql =
                "SELECT u.id AS usuario_id, u.nome AS usuario_nome, "
                        + "u.email, u.cargo, u.papel, u.is_ativo, "
                        + "p.id AS projeto_id, p.nome AS projeto_nome "
                        + "FROM usuario u "
                        + "LEFT JOIN projeto_usuario pu ON pu.usuario_id = u.id "
                        + "LEFT JOIN projeto p ON p.id = pu.projeto_id "
                        + "WHERE u.is_ativo = TRUE AND u.organizacao_id = :organizacaoId "
                        + (projetoId == null ? "" : "AND pu.projeto_id = :projetoId ")
                        + "ORDER BY u.nome, p.nome";

        var q = em.createNativeQuery(sql);
        q.setParameter("organizacaoId", organizacaoId);
        if (projetoId != null) q.setParameter("projetoId", projetoId);

        @SuppressWarnings("unchecked")
        java.util.List<Object[]> rows = q.getResultList();
        for (Object[] row : rows) {
            Integer uid = row[0] == null ? null : ((Number) row[0]).intValue();
            if (uid == null) continue;
            String nome = row[1] == null ? "" : row[1].toString();
            String email = row[2] == null ? "" : row[2].toString();
            String cargo = row[3] == null ? "" : row[3].toString();
            String projetoNome = row[7] == null ? null : row[7].toString();

            UsuarioResponseDTO u = map.computeIfAbsent(uid, id -> {
                UsuarioResponseDTO member = new UsuarioResponseDTO();
                member.setId(String.valueOf(id));
                member.setName(nome);
                member.setEmail(email);
                member.setRole(cargo);
                member.setPapel(row[4] == null ? null : row[4].toString());
                member.setAvatar(generateInitials(nome));
                member.setColor("#4B7BF5");
                member.setTasks(new TasksDTO(0, 0, 0, 0));
                member.setProjects(new java.util.ArrayList<>());
                member.setWorkload(0);
                return member;
            });
            if (projetoNome != null && !projetoNome.isBlank()
                    && !u.getProjects().contains(projetoNome)) {
                u.getProjects().add(projetoNome);
            }
        }
        return new java.util.ArrayList<>(map.values());
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
