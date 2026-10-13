package com.avantt_backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public class TarefaTagId implements Serializable {
    @Column(name = "tarefa_id")
    private Integer tarefaId;

    @Column(name = "tag_id")
    private Integer tagId;

    public TarefaTagId() {}
    public TarefaTagId(Integer tarefaId, Integer tagId) { this.tarefaId = tarefaId; this.tagId = tagId; }
    public Integer getTarefaId() { return tarefaId; }
    public void setTarefaId(Integer tarefaId) { this.tarefaId = tarefaId; }
    public Integer getTagId() { return tagId; }
    public void setTagId(Integer tagId) { this.tagId = tagId; }
    @Override public boolean equals(Object o) { if (this==o) return true; if (o==null||getClass()!=o.getClass()) return false; TarefaTagId that=(TarefaTagId)o; return java.util.Objects.equals(tarefaId,that.tarefaId)&&java.util.Objects.equals(tagId,that.tagId); }
    @Override public int hashCode() { return java.util.Objects.hash(tarefaId, tagId); }
}
