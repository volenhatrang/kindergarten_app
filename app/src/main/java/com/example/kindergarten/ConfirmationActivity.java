package com.example.kindergarten;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.kindergarten.Networking.ApiRespone;
import com.example.kindergarten.Networking.ListOfStudentActivity;
import com.example.kindergarten.Object.FaceData;
import com.example.kindergarten.Object.attendant;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ConfirmationActivity extends AppCompatActivity {
    public static final String EXTRA_STUDENT_ID = "extra_student_id";
    public static final String RESULT_CONFIRMATION = "result_confirmation";

    private int studentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirmation);

        Intent intent = getIntent();
        studentId = Integer.parseInt(intent.getStringExtra("studentID"));

        Button confirmButton = findViewById(R.id.confirm_button);
        Button cancelButton = findViewById(R.id.cancel_button);

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ToTakeAttendance(studentId);
                Intent i = new Intent();
                i.setClass(ConfirmationActivity.this, CameraActivity.class);
                startActivity(i);
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra(RESULT_CONFIRMATION, false);
                setResult(RESULT_CANCELED, resultIntent);
                finish();
            }
        });
    }

    public void ToTakeAttendance (int studentId){
        attendant student = new attendant(studentId);
        // FaceData faceData = new FaceData(studentId, "face_url test");
        Log.e("API_PRV_UPLOAD", String.valueOf(student));
        ApiRespone.getApiService().attendance(student)
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        Log.e("API_TEST", String.valueOf(response));
                        if (response.isSuccessful() && response.body() != null) {
                            //String message = response.body().getMessage();
                            Log.e("API_RESPONSE", "to take attendance successfully: ");

                        } else {
                            System.out.println("Failed to add face: " + response.message());
                            Log.e("API_RESPONSE", "Failed to take attendance: " + response.message());
                        }
                    }
                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        t.printStackTrace();
                    }
                });
    }
}