
package com.avantt_backend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public class ProjetoMarcoDTO {

    @NotBlank(message = "O nome do marco é obrigatório")
    private String name;

    @NotNull(message = "A data do marco é obrigatória")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    private Boolean done = false;

    public ProjetoMarcoDTO() {
    }

    public ProjetoMarcoDTO(String name, LocalDate date, Boolean done) {
        this.name = name;
        this.date = date;
        this.done = done;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Boolean getDone() {
        return done;
    }

    public void setDone(Boolean done) {
        this.done = done;
    }
}
