package com.example.kindergarten.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kindergarten.CameraActivity;
import com.example.kindergarten.Object.FaceData;
import com.example.kindergarten.R;
import com.example.kindergarten.MainActivity;

import java.util.List;

public class StudentAdapter extends ArrayAdapter<FaceData> {
    private Context context;
    private List<FaceData> students;

    public StudentAdapter(Context context, List<FaceData> students) {
        super(context, R.layout.item_list_student, students);
        this.context = context;
        this.students = students;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item_list_student, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.id = convertView.findViewById(R.id.student_id_txt);
            viewHolder.name = convertView.findViewById(R.id.student_name_txt);
            viewHolder.checkFace = convertView.findViewById(R.id.check_face);
            viewHolder.addFaceButton = convertView.findViewById(R.id.addFaceButton); // Added addFaceButton to ViewHolder
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        FaceData student = students.get(position);
        viewHolder.id.setText(String.valueOf(student.getStudentId()));
        viewHolder.name.setText(student.getName());
        if (student.getEmbedding() != null && !student.getEmbedding().isEmpty()) {
            viewHolder.checkFace.setText("X");
            viewHolder.addFaceButton.setVisibility(View.GONE); // Hide the button if embedding is present
        } else {
            viewHolder.checkFace.setText("");
            viewHolder.addFaceButton.setVisibility(View.VISIBLE); // Show the button if embedding is not present
            viewHolder.addFaceButton.setOnClickListener(v -> {
                // Open Camera_Recognition activity to add new face
                Intent intent = new Intent(context, CameraActivity.class);
                intent.putExtra("student_id", student.getStudentId());
                context.startActivity(intent);
            });
        }

        return convertView;
    }

    static class ViewHolder {
        TextView id;
        TextView name;
        TextView checkFace;
        ImageView addFaceButton; // Added addFaceButton to ViewHolder
    }
}

