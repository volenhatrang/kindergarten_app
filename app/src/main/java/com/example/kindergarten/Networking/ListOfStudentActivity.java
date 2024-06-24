package com.example.kindergarten.Networking;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kindergarten.Adapter.StudentAdapter;
import com.example.kindergarten.CameraActivity;
import com.example.kindergarten.Face_Recognition.FaceClassifier;
import com.example.kindergarten.Login;
import com.example.kindergarten.MainActivity;
import com.example.kindergarten.Object.FaceData;
import com.example.kindergarten.Object.ListOfStudent;
import com.example.kindergarten.R;
import com.example.kindergarten.db.OnStudentsReceivedListener;
import com.example.kindergarten.db.ProcessData;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ListOfStudentActivity extends AppCompatActivity {
    private StudentAdapter studentAdapter;
    private ListView listViewStudent;
    private ProcessData processData;
    private List<FaceData> students = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_of_student);
        // Initialize RecyclerView
        listViewStudent = findViewById(R.id.listViewStudent);
        //listViewStudent.setLayoutManager(new LinearLayoutManager(this));
        Button checkin_btn = findViewById(R.id.checking_btn);
        checkin_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent();
                i.setClass(ListOfStudentActivity.this, CameraActivity.class);
                startActivity(i);
            }
        });
        // Khởi tạo ProcessData và lấy danh sách học sinh
        processData = new ProcessData();
        processData.getListStudents(new OnStudentsReceivedListener() {
            @Override
            public void onStudentsReceived(List<FaceData> students) {
                // Sau khi dữ liệu được lấy về, thiết lập adapter
                studentAdapter = new StudentAdapter(ListOfStudentActivity.this, students);
                listViewStudent.setAdapter(studentAdapter);

                // Log số lượng học sinh
                Log.e("CHECK_LOAD MODEL 6: ", String.valueOf(students.size()));
            }
        });
    }


    // Method to get all faces

}
