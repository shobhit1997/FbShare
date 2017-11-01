package com.example.dell.fbshare;

import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private TextView info;
    private LoginButton loginButton;
    private CallbackManager callbackManager;
    private int PICK_IMAGE_REQUEST = 1;
    Bitmap bi;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent i=getIntent();
        String action=i.getAction();
        String type=i.getType();
        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {

            } else if (type.startsWith("image/")) {
                Uri photoUri = (Uri) i.getParcelableExtra(Intent.EXTRA_STREAM);
                if (photoUri != null) {
                    // Update UI to reflect image being shared
                    convertIntoBitmap(photoUri);
                }// Handle single image being sent
            }
        }

        callbackManager = CallbackManager.Factory.create();
        info = (TextView) findViewById(R.id.info);
        loginButton = (LoginButton) findViewById(R.id.login_button);
        // loginButton.setReadPermissions(Arrays.asList("user_status"));
        loginButton.setPublishPermissions(Arrays.asList("publish_actions"));
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {


                info.setText(
                        "User ID: "
                                + loginResult.getAccessToken().getUserId()
                                + "\n" +
                                "Auth Token: "
                                + loginResult.getAccessToken().getToken()
                );
                //String accessToken=loginResult.getAccessToken().getToken();


            }

            @Override
            public void onCancel() {

                info.setText("Login attempt canceled.");

            }

            @Override
            public void onError(FacebookException e) {

                info.setText("Login attempt failed." + e.getMessage());

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {

            Uri photoUri = data.getData();

            convertIntoBitmap(photoUri);

        }
    }

    public void convertIntoBitmap(Uri photoUri)
    {
        try {
            ContentResolver cr = getContentResolver();
            InputStream fis = cr.openInputStream(photoUri);
            bi = BitmapFactory.decodeStream(fis);
            ImageView imageView = (ImageView) findViewById(R.id.imageView);
            imageView.setImageBitmap(bi);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }
    public void choose(View view) {
        Intent intent = new Intent();
// Show only images, no videos or anything else
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
// Always show the chooser (if there are multiple options available)
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    public void post(View view) {
        postToFacebook();
        /*try {
            byte[] imageData = null;


            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bi.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            imageData = baos.toByteArray();

            Bundle params = new Bundle();
           // params.putString("method", "photos.upload");
            params.putByteArray("picture", imageData);


            new GraphRequest(
                    AccessToken.getCurrentAccessToken(),
                    "/me/photos",
                    params,
                    HttpMethod.POST,
                    new GraphRequest.Callback() {
                        public void onCompleted(GraphResponse response) {

                        }
                    }
            ).executeAsync();
        }catch (Exception e )
        {
            Log.i("Error",e.getMessage());
        }*/


    }

    public void postToFacebook() {

        String path = "me";
        AccessToken at = AccessToken.getCurrentAccessToken();
        Bundle parameters = new Bundle();
        byte[] imageData = null;


        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bi.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        imageData = baos.toByteArray();


        // params.putString("method", "photos.upload");
        parameters.putByteArray("picture", imageData);
        HttpMethod method = HttpMethod.POST;
        GraphRequest.Callback cb = new GraphRequest.Callback() {

            @Override
            public void onCompleted(GraphResponse graphResponse) {

                //check graphResponse for success or failure
                if (graphResponse.getError() == null) {
                    Toast.makeText(MainActivity.this, "Successfully posted to Facebook", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Facebook: There was an error, Please Try Again "+graphResponse.getError().getErrorMessage(), Toast.LENGTH_SHORT).show();

                }
            }
        };

        GraphRequest request = new GraphRequest(at, path, parameters, method, cb);
        request.setParameters(parameters);
        request.executeAsync();


    }

}

