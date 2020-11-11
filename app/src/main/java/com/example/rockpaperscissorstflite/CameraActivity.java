package com.example.rockpaperscissorstflite;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;

public class CameraActivity extends AppCompatActivity {

    Button capture_btn;
    ImageView capture_img;
    TextView pred;
    private static final int CAMERA_PERM_CODE = 121;
    private String filepath= "mobilenetv2_converted.tflite";
    private int inputSize= 224;
    private String labelpath = "labels.txt";
    private Classifier classifier;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        try {
            initClassifier();
        }
        catch (IOException e){
            e.printStackTrace();
        }

        capture_btn = (Button)findViewById(R.id.capture_btn);
        capture_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                askPermissions();
            }
        });
    }

    private void initClassifier() throws  IOException{
        classifier = new Classifier(getAssets(), filepath, labelpath, inputSize);
        Log.i("RockPaperScissors", "Initialized classifier");
    }

    private void askPermissions() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERM_CODE);
        }
        else{
            openCamera();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == CAMERA_PERM_CODE){
            if((grantResults.length > 0) && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                openCamera();
            }
            else {
                Toast.makeText(this, "Camera Permission is required to use", Toast.LENGTH_SHORT).show();
            }
        }

    }

    private void openCamera() {
        Intent camera_intent
                = new Intent(MediaStore
                .ACTION_IMAGE_CAPTURE);

        // Start the activity with camera_intent,
        // and request pic id
        startActivityForResult(camera_intent, CAMERA_PERM_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode,
                                    int resultCode,
                                    Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Match the request 'pic id with requestCode
        if (requestCode == CAMERA_PERM_CODE && resultCode == Activity.RESULT_OK) {

            // BitMap is data structure of image file
            // which store the image in memory
            capture_img = (ImageView)findViewById(R.id.capture_img);
            pred = (TextView)findViewById(R.id.pred);

            Bitmap photo = (Bitmap) data.getExtras()
                    .get("data");

            // Set the image in imageview for display
            List<Classifier.Recognition> result= classifier.recognizeImage(photo);
            capture_img.setImageBitmap(photo);
            pred.setText(result.get(0).toString());
        }
    }
}