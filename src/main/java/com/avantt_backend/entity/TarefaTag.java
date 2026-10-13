package com.avantt_backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "tarefa_tag", uniqueConstraints = {@UniqueConstraint(columnNames = {"tarefa_id","tag_id"})})
public class TarefaTag {
    @EmbeddedId
    private TarefaTagId id;

    public TarefaTag() {}
    public TarefaTag(TarefaTagId id) { this.id = id; }
    public TarefaTag(Integer tarefaId, Integer tagId) { this.id = new TarefaTagId(tarefaId, tagId); }
    public TarefaTagId getId() { return id; }
    public void setId(TarefaTagId id) { this.id = id; }
}
