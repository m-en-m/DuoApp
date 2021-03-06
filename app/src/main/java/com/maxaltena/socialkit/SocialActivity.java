package com.maxaltena.socialkit;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

public class SocialActivity extends AppCompatActivity {
    private static final String TAG = "SOCIALACTIVITY";
    private String username;
    private String platformLink;
    private String platformName;
    private String platformImage;
    private String currentUser;
    private String socialId;
    private ImageView mImage;
    private DocumentReference socialRef;
    private EditText mUsernameEditText;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_social);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        //Declare view vars
        mUsernameEditText = (EditText)findViewById(R.id.editText);
        mImage = (ImageView)findViewById(R.id.imageView);

        //Get intent vars
        Intent social = getIntent();
        username = social.getStringExtra("Username");
        platformLink = social.getStringExtra("Platform Link");
        platformName = social.getStringExtra("Platform Name");
        platformImage = social.getStringExtra("Platform Image");
        socialId = social.getStringExtra("ID");
        currentUser = social.getStringExtra("Current User");

        //Firestore vars
        socialRef = db.collection("users").document(currentUser).collection("socials").document(socialId);

        loadDataToView();
    }

    private void loadDataToView(){
        mUsernameEditText.setText(username);
        Objects.requireNonNull(getSupportActionBar()).setTitle(platformName);
        Glide.with(this).load(platformImage).into(mImage);
    }

    public void updateUsername(View v){
        String usernameToUpdate = mUsernameEditText.getText().toString();
        socialRef.update("username", usernameToUpdate)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully updated!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error updating document", e);
                    }
                });
        Toast.makeText(SocialActivity.this, "Profile updated!", Toast.LENGTH_SHORT).show();
        username = mUsernameEditText.getText().toString();
    }

    public void deleteSocial(View v){
        new AlertDialog.Builder(this)
                .setTitle("Warning")
                .setMessage("Do you really want to delete " + platformName)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        socialRef
                                .delete()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d(TAG, "DocumentSnapshot successfully deleted!");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.w(TAG, "Error deleting document", e);
                                    }
                                });
                        Toast.makeText(SocialActivity.this, "Successfully deleted", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .setNegativeButton(android.R.string.no, null).show();
    }

    public void openSocial(View v){
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(platformLink+username));
        startActivity(i);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.top_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case R.id.camera_menu:
                // Camera
                startActivity(new Intent(this, CameraActivity.class));
                return true;
            case R.id.settings_menu:
                // Settings
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            case R.id.sign_out_menu:
                // Sign out
                AuthUI.getInstance().signOut(this);
                return true;
            default:
                // Back
                startActivity(new Intent(this, MainActivity.class));
                return super.onOptionsItemSelected(item);
        }
    }
}
