package com.example.kindergarten.Face_Recognition;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.util.Log;
import android.util.Pair;

import com.example.kindergarten.Networking.ApiConfig;
import com.example.kindergarten.Networking.ApiRespone;
import com.example.kindergarten.Networking.ListOfStudentActivity;
import com.example.kindergarten.Object.AddFaceResponse;
import com.example.kindergarten.Object.FaceData;
import com.example.kindergarten.Object.ListOfStudent;
import com.example.kindergarten.db.OnStudentsReceivedListener;
import com.example.kindergarten.db.ProcessData;
//import com.example.kindergarten.db.DBHelper;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TFLiteFaceRecognition
        implements FaceClassifier {

    //private static final int OUTPUT_SIZE = 512;
    private static final int OUTPUT_SIZE = 512;

    // Only return this many results.
    private static final int NUM_DETECTIONS = 1;

    // Float model
    private static final float IMAGE_MEAN = 128.0f;
    private static final float IMAGE_STD = 128.0f;

    private boolean isModelQuantized;
    // Config values.
    private int inputSize;

    private int[] intValues;

    private float[][] embeedings;

    private ByteBuffer imgData;

    private Interpreter tfLite;
    private Context context; // Context của ứng dụng

    public HashMap<Integer, Recognition> registered = new HashMap<>();
    //DBHelper dbHelper;

    public void register(int studentId, Recognition rec) {
  /*      dbHelper.insertFace(studentId,rec.getEmbeeding());
        registered.put(studentId, rec);*/
        Log.e("API", "1");
        float[][] floatList = (float[][]) rec.getEmbeeding();
        String embeddingString = "";
        for(Float f: floatList[0]){
            embeddingString += f.toString()+",";
        }
        Log.e("API", "2");
        FaceData faceData = new FaceData(studentId, embeddingString.toString());
        // FaceData faceData = new FaceData(studentId, "face_url test");
        Log.e("API", faceData.getEmbedding());
        ApiRespone.getApiService().addFace(faceData)
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        Log.e("API_TEST", String.valueOf(response));
                        if (response.isSuccessful() && response.body() != null) {
                            //String message = response.body().getMessage();
                            Log.e("API_RESPONSE", "Face added successfully: ");

                        } else {
                            System.out.println("Failed to add face: " + response.message());
                            Log.e("API_RESPONSE", "Failed to add face:: " + response.message());
                        }
                    }
                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        t.printStackTrace();
                    }
                });
    }


    public TFLiteFaceRecognition(Context context) {
        this.context = context;
        // Khởi tạo các giá trị khác
    }
    private TFLiteFaceRecognition() {
        //dbHelper = new DBHelper(ctx);
        Log.e("CHECK_LOAD MODEL 4: ", "INTO FACECLASSIFIER.");
        ProcessData processData = new ProcessData();
        processData.getListStudents(new OnStudentsReceivedListener() {
            @Override
            public void onStudentsReceived(List<FaceData> liststudent) {
                registered = processData.getAllFaces(liststudent);
                Log.e("CHECK_LOAD MODEL 4: ", "INTO FACECLASSIFIER.");
            }
        });
    }

    //TODO loads the models into mapped byte buffer format
    private static MappedByteBuffer loadModelFile(AssetManager assets, String modelFilename)
            throws IOException {
        AssetFileDescriptor fileDescriptor = assets.openFd(modelFilename);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    public static FaceClassifier create(
            final AssetManager assetManager,
            final String modelFilename,
            final int inputSize,
            final boolean isQuantized,Context ctx)
            throws IOException {
        Log.e("CHECK_LOAD MODEL 2: ", "INTO FACECLASSIFIER.");
        final TFLiteFaceRecognition d = new TFLiteFaceRecognition();
        d.inputSize = inputSize;
        Log.e("CHECK_LOAD MODEL 3: ", "INTO FACECLASSIFIER.");

        try {
            d.tfLite = new Interpreter(loadModelFile(assetManager, modelFilename));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        d.isModelQuantized = isQuantized;
        // Pre-allocate buffers.
        int numBytesPerChannel;
        if (isQuantized) {
            numBytesPerChannel = 1; // Quantized
        } else {
            numBytesPerChannel = 4; // Floating point
        }
        d.imgData = ByteBuffer.allocateDirect(1 * d.inputSize * d.inputSize * 3 * numBytesPerChannel);
        d.imgData.order(ByteOrder.nativeOrder());
        d.intValues = new int[d.inputSize * d.inputSize];
        return d;
    }

    //TODO  looks for the nearest embeeding in the dataset
    // and retrurns the pair <id, distance>
    private Pair<Integer, Float> findNearest(float[] emb) {
        Pair<Integer, Float> ret = null;
        for (Map.Entry<Integer, Recognition> entry : registered.entrySet()) {
            final int student = entry.getKey();
            final float[] knownEmb = ((float[][]) entry.getValue().getEmbeeding())[0];

            float distance = 0;
            for (int i = 0; i < emb.length; i++) {
                float diff = emb[i] - knownEmb[i];
                distance += diff*diff;
            }
            distance = (float) Math.sqrt(distance);
            if (ret == null || distance < ret.second) {
                ret = new Pair<>(student, distance);
            }
        }
        return ret;
    }
    //TODO TAKE INPUT IMAGE AND RETURN RECOGNITIONS


    @Override
    public Recognition recognizeImage(final Bitmap bitmap, boolean storeExtra) {
        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        imgData.rewind();
        for (int i = 0; i < inputSize; ++i) {
            for (int j = 0; j < inputSize; ++j) {
                int pixelValue = intValues[i * inputSize + j];
                if (isModelQuantized) {
                    // Quantized model
                    imgData.put((byte) ((pixelValue >> 16) & 0xFF));
                    imgData.put((byte) ((pixelValue >> 8) & 0xFF));
                    imgData.put((byte) (pixelValue & 0xFF));
                } else { // Float model
                    imgData.putFloat((((pixelValue >> 16) & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
                    imgData.putFloat((((pixelValue >> 8) & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
                    imgData.putFloat(((pixelValue & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
                }
            }
        }
        Object[] inputArray = {imgData};
        // Here outputMap is changed to fit the Face Mask detector
        Map<Integer, Object> outputMap = new HashMap<>();

        embeedings = new float[1][OUTPUT_SIZE];
        outputMap.put(0, embeedings);

        // Run the inference call.
        tfLite.runForMultipleInputsOutputs(inputArray, outputMap);

        float distance = Float.MAX_VALUE;
        String id = "0";
        String label = "?";



        if (registered.size() > 0) {
            final Pair<Integer, Float> nearest = findNearest(embeedings[0]);
            if (nearest != null) {
                final int studentId = nearest.first;
                label = String.valueOf(studentId);
                distance = nearest.second;
                Log.e("CHECK_LOAD:", String.valueOf(distance));
                if (distance > 0.5)
                {

                }
            }
        }
        final int numDetectionsOutput = 1;
        Recognition rec = new Recognition(
                id,
                label,
                distance,
                new RectF());
        if (storeExtra) {
            rec.setEmbeeding(embeedings);
        }
        return rec;
    }
}
