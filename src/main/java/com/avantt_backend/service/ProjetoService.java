package com.avantt_backend.service;

import com.avantt_backend.dto.ProjetoRequestDTO;
import com.avantt_backend.dto.ProjetoResponseDTO;
import com.avantt_backend.dto.ProjetoMarcoDTO;
import com.avantt_backend.dto.SprintsDTO;
import com.avantt_backend.dto.ProjectTasksDTO;

import com.avantt_backend.entity.Projeto;
import com.avantt_backend.entity.ProjetoUsuario;
import com.avantt_backend.entity.ProjetoUsuarioId;
import com.avantt_backend.entity.ProjetoMarco;
import com.avantt_backend.entity.ProjetoRisco;

import com.avantt_backend.exception.ApiException;

import com.avantt_backend.repository.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class ProjetoService {

    private final ProjetoRepository projetoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProjetoUsuarioRepository projetoUsuarioRepository;
    private final SprintService sprintService;
    private final TarefaService tarefaService;
    private final TarefaRepository tarefaRepository;
    private final OrganizacaoRepository organizacaoRepository;
    private final ClienteRepository clienteRepository;
    private final ProjetoMarcoRepository projetoMarcoRepository;
    private final ProjetoRiscoRepository projetoRiscoRepository;
    private final ConviteService conviteService;

    private static final List<String> STATUS_VALIDOS = Arrays.asList(
            "Planejada",
            "Em andamento",
            "Concluída",
            "Bloqueada"
    );

    public ProjetoService(
            ProjetoRepository projetoRepository,
            UsuarioRepository usuarioRepository,
            ProjetoUsuarioRepository projetoUsuarioRepository,
            SprintService sprintService,
            TarefaService tarefaService,
            TarefaRepository tarefaRepository,
            OrganizacaoRepository organizacaoRepository,
            ClienteRepository clienteRepository,
            ProjetoMarcoRepository projetoMarcoRepository,
            ProjetoRiscoRepository projetoRiscoRepository,
            ConviteService conviteService) {

        this.projetoRepository = projetoRepository;
        this.usuarioRepository = usuarioRepository;
        this.projetoUsuarioRepository = projetoUsuarioRepository;
        this.sprintService = sprintService;
        this.tarefaService = tarefaService;
        this.tarefaRepository = tarefaRepository;
        this.organizacaoRepository = organizacaoRepository;
        this.clienteRepository = clienteRepository;
        this.projetoMarcoRepository = projetoMarcoRepository;
        this.projetoRiscoRepository = projetoRiscoRepository;
        this.conviteService = conviteService;
    }

    public List<ProjetoResponseDTO> listarPorOrganizacaoOuMembro(
            Integer organizacaoId,
            Integer usuarioId) {

        List<ProjetoUsuario> vinculos =
                projetoUsuarioRepository.findByIdUsuarioId(usuarioId);

        Set<Integer> projetosDoUsuario = vinculos.stream()
                .map(pu -> pu.getId().getProjetoId())
                .collect(Collectors.toSet());

        return projetoRepository.findAll()
                .stream()
                .filter(projeto ->
                        organizacaoId.equals(projeto.getOrganizacaoId())
                                || projetosDoUsuario.contains(projeto.getId())
                )
                .map(this::toFrontend)
                .collect(Collectors.toList());
    }

    // Retorna o progresso atual do projeto
    public SprintService.ProjectProgressInfo getProgress(Integer projetoId) {
        return sprintService.recalculateAndPersistProjectProgress(projetoId);
    }

    // Cadastrar projeto
    @Transactional
    public ProjetoResponseDTO create(
            ProjetoRequestDTO dto,
            Integer organizacaoId) {

        Projeto p = new Projeto();

        p.setName(dto.getName());
        p.setDescription(dto.getDescription());
        p.setColor(dto.getColor());

        String status = dto.getStatus() == null
                ? "Planejada"
                : dto.getStatus();

        validarStatus(status);
        p.setStatus(status);

        if (dto.getClienteId() != null) {

            if (!clienteRepository.existsById(dto.getClienteId())) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Cliente não encontrado: id=" + dto.getClienteId()
                );
            }

            p.setClienteId(dto.getClienteId());
        }

        if (organizacaoId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Organização do usuário autenticado não informada."
            );
        }

        if (!organizacaoRepository.existsById(organizacaoId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Organização não encontrada: id=" + organizacaoId
            );
        }

        p.setOrganizacaoId(organizacaoId);

        p.setStartDate(dto.getStartDate());

        validarPeriodo(dto.getStartDate(), dto.getEndDate());

        p.setEndDate(dto.getEndDate());

        p.setProgress(
                dto.getProgress() != null
                        ? dto.getProgress()
                        : 0
        );

        Projeto saved = projetoRepository.save(p);

        // Associar equipe ao projeto.
        // Usuários sem primeiro acesso validado receberão convite.
        if (dto.getTeam() != null) {
            for (Integer usuarioId : dto.getTeam()) {
                adicionarUsuarioEquipe(saved, usuarioId);
            }
        }

        salvarMarcos(saved, dto.getMilestones());
        salvarRiscos(saved, dto.getRisks());

        return toFrontend(saved);
    }

    // Atualizar projeto
    @Transactional
    public ProjetoResponseDTO update(
            Integer projetoId,
            ProjetoRequestDTO dto,
            Integer organizacaoId) {

        Projeto p = projetoRepository
                .findByIdAndOrganizacaoId(projetoId, organizacaoId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Projeto não encontrado: id=" + projetoId
                ));

        if (dto.getClienteId() != null) {

            if (!clienteRepository.existsById(dto.getClienteId())) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Cliente não encontrado: id=" + dto.getClienteId()
                );
            }

            p.setClienteId(dto.getClienteId());
        }

        if (dto.getName() != null) {
            p.setName(dto.getName());
        }

        if (dto.getDescription() != null) {
            p.setDescription(dto.getDescription());
        }

        if (dto.getColor() != null) {
            p.setColor(dto.getColor());
        }

        if (dto.getStatus() != null) {
            validarStatus(dto.getStatus());
            p.setStatus(dto.getStatus());
        }

        LocalDate newStart = dto.getStartDate() == null
                ? p.getStartDate()
                : dto.getStartDate();

        LocalDate newEnd = dto.getEndDate() == null
                ? p.getEndDate()
                : dto.getEndDate();

        validarPeriodo(newStart, newEnd);

        p.setStartDate(newStart);
        p.setEndDate(newEnd);

        if (dto.getProgress() != null) {
            p.setProgress(dto.getProgress());
        }

        if (dto.getTeam() != null) {
            sincronizarEquipe(p, dto.getTeam());
        }

        if (dto.getAddTeam() != null && !dto.getAddTeam().isEmpty()) {
            for (Integer usuarioId : dto.getAddTeam()) {
                adicionarUsuarioEquipe(p, usuarioId);
            }
        }

        if (dto.getRemoveTeam() != null && !dto.getRemoveTeam().isEmpty()) {
            for (Integer usuarioId : dto.getRemoveTeam()) {
                removerUsuarioEquipe(p, usuarioId);
            }
        }

        if (dto.getMilestones() != null) {
            salvarMarcos(p, dto.getMilestones());
        }

        if (dto.getRisks() != null) {
            salvarRiscos(p, dto.getRisks());
        }

        Projeto saved = projetoRepository.save(p);

        return toFrontend(saved);
    }

    // Buscar projeto por ID dentro da organização autenticada
    @Transactional(readOnly = true)
    public ProjetoResponseDTO buscarPorId(
            Integer projetoId,
            Integer organizacaoId) {

        Projeto projeto = projetoRepository
                .findByIdAndOrganizacaoId(projetoId, organizacaoId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Projeto não encontrado: id=" + projetoId
                ));

        return toFrontend(projeto);
    }

    // Listar todos os projetos
    @Transactional(readOnly = true)
    public List<ProjetoResponseDTO> listAll() {

        List<Projeto> all = projetoRepository.findAll();

        return all.stream()
                .map(this::toFrontend)
                .collect(Collectors.toList());
    }

    // Validar status do projeto
    private void validarStatus(String status) {

        if (status == null || !STATUS_VALIDOS.contains(status)) {
            throw new ApiException(
                    "Status inválido. Valores permitidos: "
                            + STATUS_VALIDOS
            );
        }
    }

    // Validar período do projeto
    private void validarPeriodo(
            LocalDate dataInicio,
            LocalDate dataFim) {

        if (dataInicio != null
                && dataFim != null
                && dataFim.isBefore(dataInicio.plusDays(15))) {

            throw new ApiException(
                    "A data de fim deve ser pelo menos 15 dias após a data de início"
            );
        }
    }

    // Salvar marcos do projeto
    private void salvarMarcos(
            Projeto projeto,
            List<ProjetoMarcoDTO> milestones) {

        if (milestones == null) {
            return;
        }

        projetoMarcoRepository.deleteByProjeto_Id(projeto.getId());

        if (milestones.isEmpty()) {
            return;
        }

        List<ProjetoMarco> marcos = new ArrayList<>();

        for (ProjetoMarcoDTO dto : milestones) {

            if (dto.getName() == null || dto.getName().isBlank()) {
                throw new ApiException(
                        "O nome do marco é obrigatório."
                );
            }

            if (dto.getDate() == null) {
                throw new ApiException(
                        "A data do marco é obrigatória."
                );
            }

            ProjetoMarco marco = new ProjetoMarco();

            marco.setProjeto(projeto);
            marco.setNome(dto.getName());
            marco.setDate(dto.getDate());
            marco.setDone(
                    dto.getDone() != null
                            ? dto.getDone()
                            : false
            );

            marcos.add(marco);
        }

        projetoMarcoRepository.saveAll(marcos);
    }

    // Salvar riscos do projeto
    private void salvarRiscos(
            Projeto projeto,
            List<String> risks) {

        if (risks == null) {
            return;
        }

        projetoRiscoRepository.deleteByProjeto_Id(projeto.getId());

        if (risks.isEmpty()) {
            return;
        }

        List<ProjetoRisco> riscos = new ArrayList<>();

        for (String descricao : risks) {

            if (descricao == null || descricao.isBlank()) {
                continue;
            }

            if (descricao.length() > 500) {
                throw new ApiException(
                        "A descrição do risco não pode ultrapassar 500 caracteres."
                );
            }

            ProjetoRisco risco = new ProjetoRisco();

            risco.setProjeto(projeto);
            risco.setDescricao(descricao.trim());

            riscos.add(risco);
        }

        projetoRiscoRepository.saveAll(riscos);
    }

    // Sincronizar equipe completa
    private void sincronizarEquipe(
            Projeto projeto,
            List<Integer> usuariosDesejados) {

        var associados = projetoUsuarioRepository
                .findByIdProjetoId(projeto.getId());

        Set<Integer> usuariosAtuais = new HashSet<>();

        if (associados != null) {
            for (ProjetoUsuario pu : associados) {
                usuariosAtuais.add(pu.getId().getUsuarioId());
            }
        }

        Set<Integer> usuariosNovos = new HashSet<>();

        for (Integer usuarioId : usuariosDesejados) {

            if (usuarioId == null) {
                throw new ApiException(
                        "Usuário inválido na lista de team."
                );
            }

            usuariosNovos.add(usuarioId);
        }

        // Adicionar novos integrantes
        for (Integer usuarioId : usuariosNovos) {

            if (!usuariosAtuais.contains(usuarioId)) {
                adicionarUsuarioEquipe(projeto, usuarioId);
            }
        }

        // Remover integrantes que não estão mais na lista
        for (Integer usuarioId : usuariosAtuais) {

            if (!usuariosNovos.contains(usuarioId)) {
                removerUsuarioEquipe(projeto, usuarioId);
            }
        }
    }

    // Adicionar usuário à equipe
    private void adicionarUsuarioEquipe(
            Projeto projeto,
            Integer usuarioId) {

        if (usuarioId == null) {
            throw new ApiException(
                    "Usuário inválido na lista de team."
            );
        }

        if (!usuarioRepository.existsById(usuarioId)) {
            throw new ApiException(
                    "Usuário não encontrado: id=" + usuarioId
            );
        }

        ProjetoUsuarioId id = new ProjetoUsuarioId(
                projeto.getId(),
                usuarioId
        );

        if (!projetoUsuarioRepository.existsById(id)) {

            ProjetoUsuario pu = new ProjetoUsuario();

            pu.setId(id);
            pu.setPapel(null);

            projetoUsuarioRepository.save(pu);

            // Envia convite somente se o primeiro acesso ainda não foi validado.
            conviteService.convidarSeNecessario(usuarioId);
        }
    }

    // Remover usuário da equipe
    private void removerUsuarioEquipe(
            Projeto projeto,
            Integer usuarioId) {

        if (usuarioId == null) {
            return;
        }

        ProjetoUsuarioId id = new ProjetoUsuarioId(
                projeto.getId(),
                usuarioId
        );

        if (!projetoUsuarioRepository.existsById(id)) {
            return;
        }

        // Verificar se o usuário está associado a alguma tarefa
        int assigned = tarefaRepository.countByProjetoIdAndAssignee(
                projeto.getId(),
                usuarioId
        );

        if (assigned > 0) {
            throw new ApiException(
                    "Não é possível remover o usuário: ele está associado "
                            + "a tarefas deste projeto: id=" + usuarioId
            );
        }

        projetoUsuarioRepository.deleteById(id);
    }

    // Converter entidade para DTO
    private ProjetoResponseDTO toFrontend(Projeto p) {

        ProjetoResponseDTO f = new ProjetoResponseDTO();

        f.setId(
                p.getId() == null
                        ? null
                        : String.valueOf(p.getId())
        );

        f.setName(p.getName());
        f.setDescription(p.getDescription());
        f.setColor(p.getColor());

        f.setStatus(p.getStatus());
        f.setStatusName(p.getStatus());
        f.setStatusId(null);

        f.setStartDate(p.getStartDate());
        f.setEndDate(p.getEndDate());

        f.setProgress(
                p.getProgress() == null
                        ? 0
                        : p.getProgress()
        );

        try {

            String projetoIdStr = p.getId() == null
                    ? null
                    : String.valueOf(p.getId());

            // Buscar sprints do projeto
            var sprints = sprintService.listAll(projetoIdStr);

            SprintsDTO s = new SprintsDTO();

            s.setTotal(sprints.size());

            int done = 0;
            int delayed = 0;
            int active = 0;

            for (var sp : sprints) {

                if (sp.getProgress() >= 100) {
                    done++;
                } else if (sp.getDaysDelayed() > 0) {
                    delayed++;
                } else {
                    active++;
                }
            }

            s.setDone(done);
            s.setDelayed(delayed);
            s.setActive(active);

            // Buscar tarefas do projeto
            var tarefas = tarefaService.listAll(
                    projetoIdStr,
                    null,
                    null
            );

            ProjectTasksDTO t = new ProjectTasksDTO();

            t.setTotal(tarefas.size());

            int tDone = 0;
            int tDelayed = 0;
            int tBlocked = 0;
            int tCancelled = 0;

            for (var ta : tarefas) {

                String status = ta.getStatus();

                if (status != null) {

                    String statusNormalizado = status
                            .trim()
                            .toLowerCase(Locale.ROOT)
                            .replace("í", "i")
                            .replace("ú", "u")
                            .replace("ã", "a");

                    if (statusNormalizado.equals("concluida")
                            || statusNormalizado.equals("concluido")
                            || statusNormalizado.equals("done")) {
                        tDone++;
                    }

                    if (statusNormalizado.equals("cancelada")
                            || statusNormalizado.equals("cancelado")
                            || statusNormalizado.equals("cancelled")) {
                        tCancelled++;
                    }
                }

                if (ta.getDaysDelayed() > 0) {
                    tDelayed++;
                }

                if (ta.getBlockedBy() != null
                        && !ta.getBlockedBy().isBlank()) {
                    tBlocked++;
                }
            }

            t.setDone(tDone);
            t.setDelayed(tDelayed);
            t.setBlocked(tBlocked);
            t.setCancelled(tCancelled);

            // Buscar equipe do projeto
            List<String> teamNames = new ArrayList<>();
            List<Integer> teamIds = new ArrayList<>();

            var puList = projetoUsuarioRepository
                    .findByIdProjetoId(p.getId());

            if (puList != null && !puList.isEmpty()) {

                for (var pu : puList) {

                    Integer uid = pu.getId().getUsuarioId();

                    teamIds.add(uid);

                    usuarioRepository.findById(uid)
                            .ifPresent(u -> teamNames.add(u.getNome()));
                }
            }

            // Buscar marcos do projeto
            List<ProjetoMarcoDTO> milestones = new ArrayList<>();

            var marcos = projetoMarcoRepository
                    .findByProjeto_IdOrderByDateAsc(p.getId());

            if (marcos != null) {

                for (ProjetoMarco marco : marcos) {

                    ProjetoMarcoDTO marcoDTO = new ProjetoMarcoDTO();

                    marcoDTO.setName(marco.getNome());
                    marcoDTO.setDate(marco.getDate());
                    marcoDTO.setDone(
                            marco.getDone() != null
                                    ? marco.getDone()
                                    : false
                    );

                    milestones.add(marcoDTO);
                }
            }

            // Buscar riscos do projeto
            List<String> risks = new ArrayList<>();

            var riscos = projetoRiscoRepository
                    .findByProjeto_Id(p.getId());

            if (riscos != null) {

                for (ProjetoRisco risco : riscos) {
                    risks.add(risco.getDescricao());
                }
            }

            f.setSprints(s);
            f.setTasks(t);

            f.setTeam(teamNames);
            f.setTeamIds(teamIds);

            f.setMilestones(milestones);
            f.setRisks(risks);

            f.setClienteId(p.getClienteId());
            f.setOrganizacaoId(p.getOrganizacaoId());

            if (p.getOrganizacaoId() != null) {

                organizacaoRepository.findById(p.getOrganizacaoId())
                        .ifPresent(org ->
                                f.setOrganizacaoName(org.getNomeFantasia())
                        );
            }

        } catch (Exception e) {

            throw new ApiException(
                    "Erro ao carregar os dados do projeto",
                    e
            );
        }

        return f;
    }
}