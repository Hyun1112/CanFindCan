package com.example.capstone1;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClient;
import com.amazonaws.services.rekognition.model.DetectTextRequest;
import com.amazonaws.services.rekognition.model.DetectTextResult;
import com.amazonaws.services.rekognition.model.Image;
import com.amazonaws.services.rekognition.model.S3Object;
import com.amazonaws.services.rekognition.model.TextDetection;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class RekogActivity extends AppCompatActivity {

    private final static String TAG = "CANFINDCAN";
    public final static String PREFS_NAME = "MyPrefsFile";

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_TAKE_PHOTO = 1;

    String mCurrentPhotoPath;

    private Button btn_back, btn_capture;
    private TextView text_display;
    private FrameLayout capture;

    File img;

    AmazonS3 s3;
    TransferUtility transferUtility;

    CognitoCachingCredentialsProvider credentialsProvider;

    private TextToSpeech tts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rekog);

        btn_back = (Button) findViewById(R.id.back);
        // btn_capture = (Button) findViewById(R.id.capture);
        text_display = (TextView) findViewById(R.id.text_display);
        capture = (FrameLayout) findViewById(R.id.capture);

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);

        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {    // TextToSpeech ??????
            @Override
            public void onInit(int i) {
                if(i == TextToSpeech.SUCCESS) {
                    tts.setLanguage(Locale.KOREAN);
                }
            }
        });

        // tts.speak("?????? ????????? ????????? ????????? ?????? ?????? ????????? ????????? ??? ?????? ??????????????????.", TextToSpeech.QUEUE_FLUSH, null);

        // Amazon Cognito ?????? ????????? ?????????
        credentialsProvider = new CognitoCachingCredentialsProvider(
                getApplicationContext(),
                "ap-northeast-2:a8ccf645-68db-47cd-9575-79bd9a28fbee", // ?????? ?????? ??? ID
                Regions.AP_NORTHEAST_2 // ??????
        );

        s3 = new AmazonS3Client(credentialsProvider);
        transferUtility = new TransferUtility(s3, getApplicationContext());

        s3.setRegion(Region.getRegion(Regions.AP_NORTHEAST_2));
        s3.setEndpoint("s3.ap-northeast-2.amazonaws.com");


        // ????????? ??????
        capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });

        // back ??????
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //?????? ??? ?????? ???????????? ??????
                onBackPressed();
            }
        });
    }

    private void dispatchTakePictureIntent() {

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {

            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Log.e("ERROR", ex.getMessage(), ex);
                tts.speak("?????? ????????? ??????????????????. ?????? ??????????????????.", TextToSpeech.QUEUE_FLUSH, null);
            }

            if (photoFile != null) {
                img = photoFile;
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.capstone1.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }else{
                System.out.println("~~~~~~~~~~~~~re-capture~~~~~~~~~~~");
                // tts.speak("?????? ????????? ??????????????????. ?????? ??????????????????.", TextToSpeech.QUEUE_FLUSH, null);
            }

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            upload();

        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        Log.i(TAG, "Creating image file");
        //String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "can_images";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        //img = image;
        Log.i(TAG, "Image created and returned");
        return image;
    }


    public void upload() {

        String imgName = "uploadImg";

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("curImg", imgName);
        editor.commit();
        if (img == null) {
            Log.i("ERROR", "The file is empty");
        } else {
            TransferObserver observer = transferUtility.upload(
                    "can-find-can",     // The bucket to upload to
                    "upload_test.jpg",    // The key for the uploaded object
                    img,
                    //new File("Android/data/kr.co.company.canfindcan/files/Pictures/"+img), // ????????? ????????? ????????????,, /The file where the data to upload exists
                    CannedAccessControlList.PublicRead
            );
            observer.setTransferListener(new TransferListener() {
                public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                    Log.i(TAG, "progress changed");
                }

                public void onStateChanged(int id, TransferState state) {
                    if (state == TransferState.COMPLETED) {
                        Log.i(TAG, "state changed");
                        DetectText();

                    }
                }

                public void onError(int id, Exception ex) {
                    Log.e("ERROR", ex.getMessage(), ex);
                    tts.speak("????????? ??????????????????. ?????? ????????? ??????????????????.", TextToSpeech.QUEUE_FLUSH, null);
                }
            });
        }
    }


    public void DetectText() {

        String photo = "upload_test.jpg";
        String bucket = "can-find-can";

        //AmazonRekognition rekognitionClient = AmazonRekognitionClient.builder();
        //AWSCredentials credential = new BasicAWSCredentials(accesskey,secretkey);

        AWSCredentials cre = new AWSCredentials() { // ??????
            @Override
            public String getAWSAccessKeyId() {
                return "AKIATQUCDCZ4LADYTQMY";
            }

            @Override
            public String getAWSSecretKey() {
                return "sUqZa89p8A4ZxhGFZ4AxPtB9Fb3qzc5Fp1x7WAX8";
            }
        };

        AmazonRekognition rekognitionClient = new AmazonRekognitionClient(cre);
        rekognitionClient.setRegion(Region.getRegion(Regions.AP_NORTHEAST_2));
        //rekognitionClient.setEndpoint("https://rekognition.ap-northeast-2.amazonaws.com");

        DetectTextRequest request = new DetectTextRequest()
                .withImage(new Image()
                        .withS3Object(new S3Object()
                                .withName(photo)
                                .withBucket(bucket)));

        String res =""; // ????????? ?????????

        try { // DetectText ??????
            AsyncTask<DetectTextRequest,Void,String> asyncTask = new AsyncTask<DetectTextRequest, Void, String>() {
                @Override
                protected String doInBackground(DetectTextRequest... detectTextRequests) {

                    DetectTextResult result = rekognitionClient.detectText(request);
                    List<TextDetection> textDetections = result.getTextDetections();

                    System.out.println("Detected lines and words for " + photo);

                    String res="";

                    for (TextDetection text: textDetections) {

                        System.out.println("Detected: " + text.getDetectedText());
                        if (text.getId() == 0) {
                            res = text.getDetectedText();
                            break;
                        }

                    }
                    return res;
                }
            };

            res = asyncTask.execute(request).get();


        }catch (Exception e){
            System.out.println("FAIL----------------------------------------------------"+e);
            tts.speak("???????????? ????????? ??????????????????. ?????? ????????? ??????????????????.", TextToSpeech.QUEUE_FLUSH, null);
        }

        System.out.println("----------------------------" + res);
        // text_display.setText(res);
        System.out.println(res.length());

        String result = "0000???  00???  00???";
        String resNum = res.replaceAll("[^0-9]", "");

        String yyyy, mm, dd;

        if (resNum.length() == 6) { // ????????? ??????
            yyyy = "20" + resNum.substring(0, 2);
            mm = resNum.substring(2, 4);
            dd = resNum.substring(4, 6);

            result = yyyy + "??? " + mm + "??? " + dd + "???";
        }
        else if (resNum.length() == 8) { // ????????? ??????
            yyyy = resNum.substring(0, 4);
            mm = resNum.substring(4, 6);
            dd = resNum.substring(6, 8);

            result = yyyy + "??? " + mm + "??? " + dd + "??? ";
        }
        else { // ?????? ??? ??????
            result = "???????????? ????????? ??????????????????.\n?????? ????????? ??????????????????.";
        }

        text_display.setText(result);
        tts.speak(result, TextToSpeech.QUEUE_FLUSH, null);


    }

}
