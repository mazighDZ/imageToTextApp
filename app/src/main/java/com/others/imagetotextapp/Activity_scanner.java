package com.others.imagetotextapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextPaint;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

public class Activity_scanner extends AppCompatActivity {


    //widget
    private ImageView captureIv;
    private TextView resultTv;
    private Button snapBtn, detectBtn;
    private Bitmap imageBitmap;

    static  final int REQUEST_IMAGE_CAPTURE = 1;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);

        captureIv = findViewById(R.id.idIVCaptureImage);
        resultTv = findViewById(R.id.idTCDetectedText);
        snapBtn = findViewById(R.id.idBTNSnap);
        detectBtn = findViewById(R.id.idBTNDetect);

        detectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                detectText();
            }
        });

        snapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (myCheckPermission()){
                    captureImage();
                    Log.d("myTag" , "camera permisiion" );
                }else {
                    myRequestPermissions();
                }
            }
        });
    }

    private void myRequestPermissions() {
        Log.d("myTag" , "ask for permission" );
   int PERMISSION_CODE =200;
        ActivityCompat.requestPermissions(this ,new String[]{
                Manifest.permission.CAMERA
        },PERMISSION_CODE);
    }
//permission method
    private boolean myCheckPermission() {

        int cameraPermission = ContextCompat.checkSelfPermission(getApplicationContext(),CAMERA_SERVICE);

        return cameraPermission == PackageManager.PERMISSION_GRANTED;
    }

    private void captureImage() {
        Intent takePicture =new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(takePicture.resolveActivity(getPackageManager())!= null){

            startActivityForResult(takePicture, REQUEST_IMAGE_CAPTURE);
        }

    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(grantResults.length>0){
            boolean cameraPermission = grantResults[0] == PackageManager.PERMISSION_GRANTED;
            if(cameraPermission){
                Toast.makeText(this ,"Permission Granted" , Toast.LENGTH_SHORT).show();
                captureImage();
            }else {
                Toast.makeText(this ,"Permission Denied!" , Toast.LENGTH_SHORT).show();

            }
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode== REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK){
            Bundle extras = data.getExtras();
            imageBitmap = (Bitmap) extras.get("data");
            captureIv.setImageBitmap(imageBitmap);
        }

    }

    private void detectText() {
        //this image that will analyse by ML library
        InputImage myImage = InputImage.fromBitmap(imageBitmap, 0);
        TextRecognizer textRecognizer = TextRecognition.getClient(
                TextRecognizerOptions.DEFAULT_OPTIONS
        );

        Task<Text> result = textRecognizer.process(myImage).addOnSuccessListener(new OnSuccessListener<Text>() {
            @Override
            public void onSuccess(Text text) {
                StringBuilder textResult = new StringBuilder();

                for (Text.TextBlock block : text.getTextBlocks()){
                    String blockText = block.getText();
                    Point[] blockCornerPoints = block.getCornerPoints();
                    Rect blockFrame = block.getBoundingBox();
                    for (Text.Line line : block.getLines()) {
                        String lineText = line.getText();
                        Point[] lineCornerPoints = line.getCornerPoints();
                        Rect lineFrame = line.getBoundingBox();
                        for (Text.Element element : line.getElements()) {
                            String elementText = element.getText();
                            Point[] elementCornerPoints = element.getCornerPoints();
                            Rect elementFrame = element.getBoundingBox();
                            for (Text.Symbol symbol : element.getSymbols()) {
                                String symbolText = symbol.getText();
                                Point[] symbolCornerPoints = symbol.getCornerPoints();
                                Rect symbolFrame = symbol.getBoundingBox();
                                textResult.append(symbolText);
                            }
                            textResult.append(" "); // Add a space between elements within a line

                        }
                        textResult.append("\n"); // Add a new line after each line

                    }
                }

                resultTv.setText(textResult.toString());
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(),"Failed to detect Text from Image" , Toast.LENGTH_SHORT).show();
            }
        });

    }
}