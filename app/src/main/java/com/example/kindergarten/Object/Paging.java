package com.example.kindergarten.Object;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Paging {
    @SerializedName("links")
    private links links;
    @SerializedName("total")
    private int total;

    public links getLinks() {
        return links;
    }

    public void setLinks(links links) {
        this.links = links;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }
}
