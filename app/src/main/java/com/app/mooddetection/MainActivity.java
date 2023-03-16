package com.app.mooddetection;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
//import com.google.firebase.FirebaseApp;
//import com.google.firebase.ml.vision.FirebaseVision;
//import com.google.firebase.ml.vision.common.FirebaseVisionImage;
//import com.google.firebase.ml.vision.face.FirebaseVisionFace;
//import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
//import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;
import com.google.mlkit.vision.face.FaceLandmark;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_VIDEO_CAPTURE = 1;
    private Uri videoUri;
    private Button mbutton;
    private VideoView vview;

    private TextView moodtype;
    private TextView emojiview;

    private Button moodbtn;


    private  double totalSmilingProbability = 0.0;
    private  int frameCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mbutton = findViewById(R.id.btn);
        vview = findViewById(R.id.videoView);
        moodbtn = findViewById(R.id.moodbtn);
        moodtype = findViewById(R.id.moodtype);
        emojiview = findViewById(R.id.emoji);


        mbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                emojiview.setText(null);
                moodtype.setText(null);
                dispatchTakeVideoIntent();
            }
        });

        moodbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (videoUri == null) {

                } else {
                    totalSmilingProbability = 0.0;
                    frameCount = 0;
                    FaceDetectorOptions options =
                            new FaceDetectorOptions.Builder()
                                    .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                                    .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                                    .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                                    .build();

                    MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                    retriever.setDataSource(getApplicationContext(), videoUri);
                    String durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                    long duration = Long.parseLong(durationStr);
                    long interval = 1000;

                    for (long time = 0; time < duration; time += interval) {
                        final long finalTime = time;
                        Bitmap frame = retriever.getFrameAtTime(time * 1000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
                        InputImage image = InputImage.fromBitmap(frame, 0);
                        FaceDetector faceDetector = FaceDetection.getClient(options);
                        //        Toast.makeText(MainActivity.this, Long.toString(time), Toast.LENGTH_SHORT).show();
                        Task<List<Face>> result =
                                faceDetector.process(image)
                                        .addOnSuccessListener(
                                                new OnSuccessListener<List<Face>>() {
                                                    @Override
                                                    public void onSuccess(List<Face> faces) {
                                                        // Task completed successfully
                                                        for (Face face : faces) {
                                                            // Detect the emotion of the face using Google Cloud's Vision API
                                                            // detectEmotion(face);

//                                                          FaceLandmark lefteye  =  face.getLandmark(FaceLandmark.LEFT_EYE);
//                                                            FaceLandmark righteye  =  face.getLandmark(FaceLandmark.RIGHT_EYE);
//                                                            FaceLandmark noseBase  =  face.getLandmark(FaceLandmark.NOSE_BASE);
//                                                            FaceLandmark mouthLeft  =  face.getLandmark(FaceLandmark.MOUTH_LEFT);
//                                                            FaceLandmark mouthRight  =  face.getLandmark(FaceLandmark.MOUTH_RIGHT);
//                                                            FaceLandmark mouthBottom  =  face.getLandmark(FaceLandmark.MOUTH_BOTTOM);
//                                                            FaceLandmark leftEyebrowTop  =  face.getLandmark(FaceLandmark.LEFT_EYE);
//                                                            FaceLandmark leftEyebrowBottom  =  face.getLandmark(FaceLandmark.LEFT_EYE);
//                                                            FaceLandmark rightEyebrowTop  =  face.getLandmark(FaceLandmark.LEFT_EYE);
//                                                            FaceLandmark rightEyebrowBottom  =  face.getLandmark(FaceLandmark.LEFT_EYE);

                                                            float smilingProbability = face.getSmilingProbability();

                                                            totalSmilingProbability += smilingProbability;
                                                            frameCount++;

                                                            // check if it's the last iteration of the outer loop
                                                            if (finalTime + interval >= duration) {
                                                                // perform some action

                                                               double averageSmilingProbability = totalSmilingProbability / frameCount;
                                                                Toast.makeText(MainActivity.this, Double.toString(averageSmilingProbability
                                                                ), Toast.LENGTH_SHORT).show();
                                                                if (averageSmilingProbability > 0.8) {
                                                                    moodtype.setText("You are happy dear");
                                                                    int  txt = 0x1f600;
                                                                    String emoji = String.valueOf(Character.toChars(txt));
                                                                    emojiview.setText(emoji);

                                                                }else if ( averageSmilingProbability > 0.5 && averageSmilingProbability < 0.8 ) {
                                                                    moodtype.setText("you are relieved");
                                                                    int  txt = 0x1f60C;
                                                                    String emoji = String.valueOf(Character.toChars(txt));
                                                                    emojiview.setText(emoji);

                                                                } else if ( averageSmilingProbability > 0.3 && averageSmilingProbability < 0.5 ) {
                                                                    moodtype.setText("you are anguished");
                                                                    int  txt = 0x1f627;
                                                                    String emoji = String.valueOf(Character.toChars(txt));
                                                                    emojiview.setText(emoji);

                                                                }else if (averageSmilingProbability < 0.3 && averageSmilingProbability > 0.1) {
                                                                    moodtype.setText("You seems to be disappointed ");
                                                                    int  txt = 0x1f61E;
                                                                    String emoji = String.valueOf(Character.toChars(txt));
                                                                    emojiview.setText(emoji);
                                                                }
                                                                else if (averageSmilingProbability < 0.1) {
                                                                    moodtype.setText("why are you angry dear?");
                                                                    int  txt = 0x1F621;
                                                                    String emoji = String.valueOf(Character.toChars(txt));
                                                                    emojiview.setText(emoji);
                                                                }
                                                                //    Toast.makeText(MainActivity.this, Float.toString(smilingProbability), Toast.LENGTH_SHORT).show();
                                                                // check if it's the last iteration of the inner loop
                                                            }
                                                            // Toast.makeText(MainActivity.this, Integer.toString(frameCount), Toast.LENGTH_SHORT).show();
                                                            //     Log.i("tyu",Double.toString(smilingProbability));

                                                            // Process the mood value as needed
                                                            //    Toast.makeText(MainActivity.this, Float.toString(smilingProbability), Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                })
                                        .addOnFailureListener(
                                                new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        // Task failed with an exception
                                                        Log.e("TAG", "Error detecting face: " + e.getMessage());
                                                    }
                                                });

                    }

                }

            }

        });

    }
    public static String getEmojiFromString(String emojiString) {

        if (!emojiString.contains("\\u")) {

            return emojiString;
        }
        String emojiEncodedString = "";

        int position = emojiString.indexOf("\\u");

        while (position != -1) {

            if (position != 0) {
                emojiEncodedString += emojiString.substring(0, position);
            }

            String token = emojiString.substring(position + 2, position + 6);
            emojiString = emojiString.substring(position + 6);
            emojiEncodedString += (char) Integer.parseInt(token, 16);
            position = emojiString.indexOf("\\u");
        }
        emojiEncodedString += emojiString;

        return emojiEncodedString;
    }
    private void dispatchTakeVideoIntent () {
            Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE);
            }
        }


        @Override
        protected void onActivityResult ( int requestCode, int resultCode, Intent data){
            super.onActivityResult(requestCode, resultCode, data);
            if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK) {
                videoUri = data.getData();
                Toast.makeText(this, videoUri.toString(), Toast.LENGTH_SHORT).show();
                vview.setVideoURI(videoUri);
                vview.start();


                // Create a FirebaseVisionImage object from the image bytes


            }
        }
    }

