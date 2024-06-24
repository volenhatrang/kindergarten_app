package com.example.kindergarten.Object;

public class Meta {
    private Paging paging;
    private double time;

    public Meta(Paging paging, double time) {
        this.paging = paging;
        this.time = time;
    }

    public Paging getPaging() {
        return paging;
    }

    public void setPaging(Paging paging) {
        this.paging = paging;
    }

    public double getTime() {
        return time;
    }

    public void setTime(double time) {
        this.time = time;
    }
}
