package com.avantt_backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "tag", uniqueConstraints = {@UniqueConstraint(columnNames = {"nome"})})
public class Tag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "nome", length = 100, nullable = false)
    private String nome;

    // tags are global; association with tarefas stored in tarefa_tag join table
    public Tag() {}

    public Tag(String nome) {
        this.nome = nome;
    }

    public Tag(Integer id, String nome) {
        this.id = id;
        this.nome = nome;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
}
