package com.example.kindergarten;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.Size;
import android.util.TypedValue;
import android.view.Surface;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.kindergarten.Drawing.BorderedText;
import com.example.kindergarten.Drawing.MultiBoxTracker;
import com.example.kindergarten.Drawing.OverlayView;
import com.example.kindergarten.Face_Recognition.FaceClassifier;
import com.example.kindergarten.Face_Recognition.TFLiteFaceRecognition;
import com.example.kindergarten.LiveFeed.CameraConnectionFragment;
import com.example.kindergarten.LiveFeed.ImageUtils;
import com.example.kindergarten.Networking.ListOfStudentActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class CameraActivity extends AppCompatActivity implements ImageReader.OnImageAvailableListener{


    Handler handler;
    private Matrix frameToCropTransform;
    private int sensorOrientation;
    private Matrix cropToFrameTransform;

    private static final boolean MAINTAIN_ASPECT = false;
    private static final float TEXT_SIZE_DIP = 10;
    OverlayView trackingOverlay;
    private BorderedText borderedText;
    private MultiBoxTracker tracker;
    private Integer useFacing = null;
    private static final String KEY_USE_FACING = "use_facing";
    private static final int CROP_SIZE = 1000;
    private static final int TF_OD_API_INPUT_SIZE2 = 160;

    //TODO delcare face detector
    FaceDetector detector;

    //TODO declare face recognizer
    private FaceClassifier faceClassifier;

    boolean registerFace = false;
    int studentID ;
    Button back;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        handler = new Handler();
        Intent intent = getIntent();
        studentID = intent.getIntExtra("student_id", -1);
        Log.e("DEBUG", String.valueOf(studentID));



        //TODO handling permissions
        if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_DENIED) {
            String[] permission = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
            requestPermissions(permission, 121);
        }
        /*Intent intent = getIntent();*/
        useFacing = intent.getIntExtra(KEY_USE_FACING, CameraCharacteristics.LENS_FACING_BACK);

        //TODO show live camera footage
        setFragment();


        //TODO initialize the tracker to draw rectangles
        tracker = new MultiBoxTracker(this);


        //TODO initalize face detector
        // Multiple object detection in static images
        FaceDetectorOptions highAccuracyOpts =
                new FaceDetectorOptions.Builder()
                        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
                        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
                        .build();
        detector = FaceDetection.getClient(highAccuracyOpts);


        //TODO initialize FACE Recognition
        try {
            Log.e("CHECK_LOAD MODEL: ", "INTO FACECLASSIFIER.");

            faceClassifier =
                    TFLiteFaceRecognition.create(
                            getAssets(),
                            "facenet.tflite",
                            TF_OD_API_INPUT_SIZE2,
                            false,getApplicationContext());

        } catch (final IOException e) {
            e.printStackTrace();
            Toast toast =
                    Toast.makeText(
                            getApplicationContext(), "Classifier could not be initialized", Toast.LENGTH_SHORT);
            toast.show();
            finish();
        }
        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent();
                i.setClass(CameraActivity.this, ListOfStudentActivity.class);
                startActivity(i);
            }
        });

        findViewById(R.id.imageView4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerFace = true;
            }
        });

        findViewById(R.id.imageView3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchCamera();
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 121 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            setFragment();
        }
    }

    //TODO fragment which show live footage from camera
    int previewHeight = 0,previewWidth = 0;
    protected void setFragment() {
        final CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        String cameraId = null;
        try {
            cameraId = manager.getCameraIdList()[useFacing];
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        Fragment fragment;
        CameraConnectionFragment camera2Fragment =
                CameraConnectionFragment.newInstance(
                        new CameraConnectionFragment.ConnectionCallback() {
                            @Override
                            public void onPreviewSizeChosen(final Size size, final int rotation) {
                                previewHeight = size.getHeight();
                                previewWidth = size.getWidth();

                                final float textSizePx =
                                        TypedValue.applyDimension(
                                                TypedValue.COMPLEX_UNIT_DIP, TEXT_SIZE_DIP, getResources().getDisplayMetrics());
                                borderedText = new BorderedText(textSizePx);
                                borderedText.setTypeface(Typeface.MONOSPACE);


                                int cropSize = CROP_SIZE;

                                previewWidth = size.getWidth();
                                previewHeight = size.getHeight();

                                sensorOrientation = rotation - getScreenOrientation();

                                rgbFrameBitmap = Bitmap.createBitmap(previewWidth, previewHeight, Bitmap.Config.ARGB_8888);
                                croppedBitmap = Bitmap.createBitmap(cropSize, cropSize, Bitmap.Config.ARGB_8888);

                                frameToCropTransform =
                                        ImageUtils.getTransformationMatrix(
                                                previewWidth, previewHeight,
                                                cropSize, cropSize,
                                                sensorOrientation, MAINTAIN_ASPECT);

                                cropToFrameTransform = new Matrix();
                                frameToCropTransform.invert(cropToFrameTransform);

                                trackingOverlay = (OverlayView) findViewById(R.id.tracking_overlay);
                                trackingOverlay.addCallback(
                                        new OverlayView.DrawCallback() {
                                            @Override
                                            public void drawCallback(final Canvas canvas) {
                                                tracker.draw(canvas);
                                                Log.d("tryDrawRect","inside draw");
                                            }
                                        });
                                tracker.setFrameConfiguration(previewWidth, previewHeight, sensorOrientation);
                            }
                        },
                        this,
                        R.layout.camera_fragment,
                        new Size(640, 480));

        camera2Fragment.setCamera(cameraId);
        fragment = camera2Fragment;
        getFragmentManager().beginTransaction().replace(R.id.container, fragment).commit();
    }


    //TODO getting frames of live camera footage and passing them to model
    private boolean isProcessingFrame = false;
    private byte[][] yuvBytes = new byte[3][];
    private int[] rgbBytes = null;
    private int yRowStride;
    private Runnable postInferenceCallback;
    private Runnable imageConverter;
    private Bitmap rgbFrameBitmap;
    @Override
    public void onImageAvailable(ImageReader reader) {
        // We need wait until we have some size from onPreviewSizeChosen
        if (previewWidth == 0 || previewHeight == 0) {
            return;
        }
        if (rgbBytes == null) {
            rgbBytes = new int[previewWidth * previewHeight];
        }
        try {
            final Image image = reader.acquireLatestImage();

            if (image == null) {
                return;
            }

            if (isProcessingFrame) {
                image.close();
                return;
            }
            isProcessingFrame = true;
            final Image.Plane[] planes = image.getPlanes();
            fillBytes(planes, yuvBytes);
            yRowStride = planes[0].getRowStride();
            final int uvRowStride = planes[1].getRowStride();
            final int uvPixelStride = planes[1].getPixelStride();

            imageConverter =
                    new Runnable() {
                        @Override
                        public void run() {
                            ImageUtils.convertYUV420ToARGB8888(
                                    yuvBytes[0],
                                    yuvBytes[1],
                                    yuvBytes[2],
                                    previewWidth,
                                    previewHeight,
                                    yRowStride,
                                    uvRowStride,
                                    uvPixelStride,
                                    rgbBytes);
                        }
                    };

            postInferenceCallback =
                    new Runnable() {
                        @Override
                        public void run() {
                            image.close();
                            isProcessingFrame = false;
                        }
                    };

            performFaceDetection();

        } catch (final Exception e) {
            Log.d("tryError",e.getMessage()+"abc ");
            return;
        }

    }

    protected void fillBytes(final Image.Plane[] planes, final byte[][] yuvBytes) {
        // Because of the variable row stride it's not possible to know in
        // advance the actual necessary dimensions of the yuv planes.
        for (int i = 0; i < planes.length; ++i) {
            final ByteBuffer buffer = planes[i].getBuffer();
            if (yuvBytes[i] == null) {
                yuvBytes[i] = new byte[buffer.capacity()];
            }
            buffer.get(yuvBytes[i]);
        }
    }
    protected int getScreenOrientation() {
        switch (getWindowManager().getDefaultDisplay().getRotation()) {
            case Surface.ROTATION_270:
                return 270;
            case Surface.ROTATION_180:
                return 180;
            case Surface.ROTATION_90:
                return 90;
            default:
                return 0;
        }
    }

    Bitmap croppedBitmap;
    List<FaceClassifier.Recognition> mappedRecognitions;

    //TODO Perform face detection
    public void performFaceDetection(){
        imageConverter.run();;
        rgbFrameBitmap.setPixels(rgbBytes, 0, previewWidth, 0, 0, previewWidth, previewHeight);

        final Canvas canvas = new Canvas(croppedBitmap);
        canvas.drawBitmap(rgbFrameBitmap, frameToCropTransform, null);

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                mappedRecognitions = new ArrayList<>();
                InputImage image = InputImage.fromBitmap(croppedBitmap,0);
                detector.process(image)
                        .addOnSuccessListener(
                                new OnSuccessListener<List<Face>>() {
                                    @Override
                                    public void onSuccess(List<Face> faces) {

                                        for(Face face:faces) {
                                            final Rect bounds = face.getBoundingBox();
                                            performFaceRecognition(face);

                                        }
                                        registerFace = false;
                                        tracker.trackResults(mappedRecognitions, 10);
                                        trackingOverlay.postInvalidate();
                                        postInferenceCallback.run();

                                    }
                                })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Task failed with an exception
                                        // ...
                                    }
                                });



            }
        });
    }

    //TODO perform face recognition
    public void performFaceRecognition(Face face){
        //TODO crop the face
        Rect bounds = face.getBoundingBox();
        if(bounds.top<0){
            bounds.top = 0;
        }
        if(bounds.left<0){
            bounds.left = 0;
        }
        if(bounds.left+bounds.width()>croppedBitmap.getWidth()){
            bounds.right = croppedBitmap.getWidth()-1;
        }
        if(bounds.top+bounds.height()>croppedBitmap.getHeight()){
            bounds.bottom = croppedBitmap.getHeight()-1;
        }

        Bitmap crop = Bitmap.createBitmap(croppedBitmap,
                bounds.left,
                bounds.top,
                bounds.width(),
                bounds.height());
        crop = Bitmap.createScaledBitmap(crop,TF_OD_API_INPUT_SIZE2,TF_OD_API_INPUT_SIZE2,false);


        final FaceClassifier.Recognition result = faceClassifier.recognizeImage(crop, registerFace);
        String title = "Unknown";
        float confidence = 0;
        if (result !=null) {
            if(registerFace){
                registerFaceDialogue(crop,result);
            }else {
                if (result.getDistance() < 0.75f) {
                    confidence = result.getDistance();
                    title = result.getTitle();

                }
            }
        }

        RectF location = new RectF(bounds);
        if (bounds != null) {
            if(useFacing == CameraCharacteristics.LENS_FACING_BACK) {
                location.right = croppedBitmap.getWidth() - location.right;
                location.left = croppedBitmap.getWidth() - location.left;
            }
            cropToFrameTransform.mapRect(location);
            FaceClassifier.Recognition recognition = new FaceClassifier.Recognition(face.getTrackingId()+"",title,confidence,location);
            mappedRecognitions.add(recognition);

        }
        if (!title.equals("Unknown") && !registerFace) {
            Intent intent = new Intent(CameraActivity.this, ConfirmationActivity.class);
            intent.putExtra("studentID", title);
            Log.e("CHECK_LOAD_OK: ", title);
            startActivity(intent);
        }



    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        detector.close();
    }

    //TODO register face dialogue
    private void registerFaceDialogue(Bitmap croppedFace, FaceClassifier.Recognition rec) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.register_face_dialogue);
        ImageView ivFace = dialog.findViewById(R.id.dlg_image);
        EditText nameEd = dialog.findViewById(R.id.dlg_input);
        Button register = dialog.findViewById(R.id.button2);
        ivFace.setImageBitmap(croppedFace);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = nameEd.getText().toString();
                if (name.isEmpty()) {
                    nameEd.setError("Enter Name");
                    return;
                }
                Log.e("API", "PREV");
                faceClassifier.register(studentID, rec);
                Toast.makeText(CameraActivity.this, "Face Registered Successfully", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    //TODO switch camera
    public void switchCamera() {

        Intent intent = getIntent();

        if (useFacing == CameraCharacteristics.LENS_FACING_FRONT) {
            useFacing = CameraCharacteristics.LENS_FACING_BACK;
        } else {
            useFacing = CameraCharacteristics.LENS_FACING_FRONT;
        }

        intent.putExtra(KEY_USE_FACING, useFacing);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);

        restartWith(intent);

    }

    private void restartWith(Intent intent) {
        finish();
        overridePendingTransition(0, 0);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }
}