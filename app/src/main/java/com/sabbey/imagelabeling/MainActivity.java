package com.sabbey.imagelabeling;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabeler;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    Button openCamera, imagePicker;
    final int CAMERA = 4;
    final int GALLERY = 3;
    FirebaseVisionImageLabeler labeler;
    FirebaseVisionImage image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        openCamera = findViewById(R.id.openCamera);
        imagePicker = findViewById(R.id.imagePicker);

        labeler = FirebaseVision.getInstance()
                .getOnDeviceImageLabeler();
        onCamera();
        onImagePick();
    }

    public void onImagePick(){

        imagePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, GALLERY);
            }
        });
    }

    public void onCamera(){

        openCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, CAMERA);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY && resultCode == RESULT_OK)
        {
            Uri uri = data.getData();
            try {
                image = FirebaseVisionImage.fromFilePath(MainActivity.this, uri);
                getLabel(image);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (requestCode == CAMERA && resultCode == RESULT_OK)
        {
            Bundle extras = data.getExtras();
            Bitmap photo = (Bitmap) extras.get("data");
            image = FirebaseVisionImage.fromBitmap(photo);
            getLabel(image);
        }
    }

    public void getLabel(FirebaseVisionImage image){


        labeler.processImage(image).addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionImageLabel>>() {
            @Override
            public void onSuccess(List<FirebaseVisionImageLabel> firebaseVisionImageLabels) {

                String text = "";
                if (firebaseVisionImageLabels.size() > 0)
                {
                    for (FirebaseVisionImageLabel label : firebaseVisionImageLabels) {
                            text = text.concat("Item: " + label.getText() + String.format(" ,Confidence: %.2f", label.getConfidence()*100) + "%" +"\n");
                    }
                    alertDialog(text);
                }

                else
                    Toast.makeText(MainActivity.this, "Nothing Detected", Toast.LENGTH_SHORT).show();

            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "Error", Toast.LENGTH_SHORT).show();
                    }
                });

    }

    public void alertDialog(String s){

        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage(s);
        builder.setTitle("Result");
        builder.setCancelable(false);
        builder.create();

        builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

}
