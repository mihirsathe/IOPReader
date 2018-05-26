package com.example.mihir.iopv3;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private static final int MY_CAMERA_REQUEST_CODE = 100;
    private static final int SELECT_FILE_REQUEST_CODE = 200;
    private Uri logURI = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button takePicBtn = findViewById(R.id.takePicBtn);
        Button choosePicBtn = findViewById(R.id.choosePicBtn);
        takePicBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (checkSelfPermission(Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.CAMERA},
                            MY_CAMERA_REQUEST_CODE);
                }
                else{
                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                    File tempIm = getImFile();
                    if (tempIm != null) {
                        Uri photoURI = FileProvider.getUriForFile(getApplicationContext(),BuildConfig.APPLICATION_ID, tempIm);
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                        startActivityForResult(takePictureIntent, MY_CAMERA_REQUEST_CODE);
                        logURI = photoURI;

                    }
                }
            }
        });
        choosePicBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Todo if  no permissions get permissions
                // Todo if permissions granted, call file picker intent
                if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, SELECT_FILE_REQUEST_CODE);
                }
                else{
                    /* Choose an image from Gallery */
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_FILE_REQUEST_CODE);
                }
            }
        });


    }


    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == MY_CAMERA_REQUEST_CODE) {
            if(grantResults.length != 0) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "camera permission granted, please retry", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
                }
            }
        }

        if (requestCode == SELECT_FILE_REQUEST_CODE ){
            if(grantResults.length != 0) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Read permission given, please retry", Toast.LENGTH_LONG).show();
                }
            }
            else{
                Toast.makeText(this, "Read permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    private File getImFile(){
        File tempIm = null;
        try {
            tempIm = File.createTempFile("img",".bmp");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return tempIm;
    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if( resultCode == Activity.RESULT_OK) {
            //if user chooses to take a picture with the camera
            if (requestCode == MY_CAMERA_REQUEST_CODE) {
                Intent i = new Intent(this, loadingScreenAct.class);
                i.putExtra("photoUri", logURI);
                startActivity(i);
            }
            else if (requestCode == SELECT_FILE_REQUEST_CODE) {
                Uri photoUri = data.getData();
                Intent i = new Intent(this, loadingScreenAct.class);
                i.putExtra("photoUri", photoUri);
                startActivity(i);
            }
        }
    }
}
