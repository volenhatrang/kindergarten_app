package com.example.kindergarten.Object;

import com.google.gson.annotations.SerializedName;

public class FaceData {
    @SerializedName("id")
    private int id;

    @SerializedName("name")
    private String name;

    @SerializedName("face_url")
    private String face_url;
    private int company_id ;


    public FaceData(int studentId, String name, String embedding, int company_id){
        this.id = studentId;
        this.name = name;
        this.face_url= embedding;
        this.company_id = 9;
    }
    public FaceData(int studentId, String name, String embedding){
        this.id = studentId;
        this.name = name;
        this.face_url= embedding;
        this.company_id = 9;
    }

    public FaceData(int studentId){
        this.id = studentId;
        this.company_id = 9;
    }

    public FaceData(int studentId, String embedding) {
        this.id = studentId;
        this.face_url = embedding;
        this.company_id = 9;
    }

    public FaceData(int studentId, String embedding, int company_id) {
        this.id = studentId;
        this.face_url = embedding;
        this.company_id = 9;
    }

    public int getStudentId() {
        return id;
    }

    public void setStudentId(int studentId) {
        this.id = studentId;
    }

    public String getEmbedding() {
        return face_url;
    }

    public void setEmbedding(String embedding) {
        this.face_url = embedding;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    public String toString() {
        return "FaceData{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", faceUrl='" + face_url + '\'' +
                ", companyId=" + 9 +
                '}';
    }
}
