package com.avantt_backend.dto;

public class TasksDTO {
    private int total;
    private int done;
    private int delayed;
    private int blocked;

    public TasksDTO() {}

    public TasksDTO(int total, int done, int delayed, int blocked) {
        this.total = total;
        this.done = done;
        this.delayed = delayed;
        this.blocked = blocked;
    }

    public int getTotal() { return total; }
    public void setTotal(int total) { this.total = total; }

    public int getDone() { return done; }
    public void setDone(int done) { this.done = done; }

    public int getDelayed() { return delayed; }
    public void setDelayed(int delayed) { this.delayed = delayed; }

    public int getBlocked() { return blocked; }
    public void setBlocked(int blocked) { this.blocked = blocked; }
}
