package com.example.kindergarten.Object;

import android.util.Log;

import com.example.kindergarten.Adapter.StudentAdapter;
import com.example.kindergarten.Networking.ApiRespone;
import com.example.kindergarten.Networking.ListOfStudentActivity;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ListOfStudent {

    private List<FaceData> data;
    private Meta meta;

    public List<FaceData> getData() {
        return data;
    }

    public void setData(List<FaceData> data) {
        this.data = data;
    }

    public Meta getMeta() {
        return meta;
    }

    public void setMeta(Meta meta) {
        this.meta = meta;
    }


}
