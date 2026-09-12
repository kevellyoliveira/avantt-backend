package com.avantt_backend.dto;

public class SprintsDTO {
    private int total;
    private int done;
    private int active;
    private int delayed;

    public SprintsDTO() {}

    public SprintsDTO(int total, int done, int active, int delayed) {
        this.total = total;
        this.done = done;
        this.active = active;
        this.delayed = delayed;
    }

    public int getTotal() { return total; }
    public void setTotal(int total) { this.total = total; }

    public int getDone() { return done; }
    public void setDone(int done) { this.done = done; }

    public int getActive() { return active; }
    public void setActive(int active) { this.active = active; }

    public int getDelayed() { return delayed; }
    public void setDelayed(int delayed) { this.delayed = delayed; }
}
