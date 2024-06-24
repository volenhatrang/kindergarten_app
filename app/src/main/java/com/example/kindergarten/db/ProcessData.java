package com.example.kindergarten.db;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.kindergarten.Adapter.StudentAdapter;
import com.example.kindergarten.Face_Recognition.FaceClassifier;
import com.example.kindergarten.Networking.ApiRespone;
import com.example.kindergarten.Networking.ListOfStudentActivity;
import com.example.kindergarten.Object.FaceData;
import com.example.kindergarten.Object.ListOfStudent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProcessData {

    private List<FaceData> students;

    public ProcessData() {
        // Khởi tạo danh sách học sinh
        students = new ArrayList<>();
    }
    public List<FaceData> getStudents() {
        return students;
    }

    public void getListStudents(OnStudentsReceivedListener listener) {
        Log.e("DEBUG", "getListStudents called");
        ApiRespone.getApiService().getListOfStudent("id,name,face_url")
                .enqueue(new Callback<ListOfStudent>() {
                    @Override
                    public void onResponse(Call<ListOfStudent> call, Response<ListOfStudent> response) {
                        Log.e("DEBUG", "API response received");
                        if (response.isSuccessful() && response.body() != null) {
                            students = response.body().getData();
                            Log.e("DEBUG", "API response successful");
                            try {
                                Log.e("DEBUG", "Full response: " + response.raw().toString());
                                // Log.e("DEBUG", "Response body: " + students.toString());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            if (students != null && !students.isEmpty()) {
                                for (FaceData student : students) {
                                    Log.e("Student", student.toString());
                                }
                            } else {
                                Log.e("DEBUG", "No students received");
                            }
                            // Notify the listener with the received data
                            listener.onStudentsReceived(students);
                        } else {
                            Log.e("DEBUG", "Request failed: " + response.message());
                        }
                    }

                    @Override
                    public void onFailure(Call<ListOfStudent> call, Throwable t) {
                        Log.e("DEBUG", "API call failed" + t.getMessage());
                        t.printStackTrace();
                    }
                });
    }


    public HashMap<Integer, FaceClassifier.Recognition> getAllFaces(@NonNull List<FaceData> students) {
        HashMap<Integer, FaceClassifier.Recognition> registered = new HashMap<>();
        for (FaceData student : students) {
            String embeddingString = student.getEmbedding();
            if (embeddingString != null) {

                String[] stringList = embeddingString.split(",");
                ArrayList<Float> embeddingFloat = new ArrayList<>();
                for (String s : stringList) {
                    embeddingFloat.add(Float.parseFloat(s));
                }
                float[][] bigArray = new float[1][];
                float[] floatArray = new float[embeddingFloat.size()];
                for (int i = 0; i < embeddingFloat.size(); i++) {
                    floatArray[i] = embeddingFloat.get(i);
                }
                bigArray[0] = floatArray;

                FaceClassifier.Recognition recognition = new FaceClassifier.Recognition(student.getName(), bigArray);
                registered.putIfAbsent(Integer.valueOf(student.getStudentId()), recognition);
            }
        }
        Log.d("tryRL", "rl=" + registered.size());
        return registered;
    }
}