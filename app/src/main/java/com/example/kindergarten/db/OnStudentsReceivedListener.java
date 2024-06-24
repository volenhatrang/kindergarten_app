package com.example.kindergarten.db;

import com.example.kindergarten.Object.FaceData;

import java.util.List;

public interface OnStudentsReceivedListener {
    void onStudentsReceived(List<FaceData> students);
}
