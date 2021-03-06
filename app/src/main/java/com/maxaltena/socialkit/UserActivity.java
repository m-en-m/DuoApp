package com.maxaltena.socialkit;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import javax.annotation.Nullable;

public class UserActivity extends AppCompatActivity {
    private static final String TAG = "User Activity ";

    private HashMap<String, ArrayList<String>> platformhashmap = new HashMap<String, ArrayList<String>>();
    private ArrayList<String> platformData = new ArrayList<>();
    final ArrayList<String> socialArray = new ArrayList<String>();
    private ArrayList<String> mUsernames = new ArrayList<>();
    private ArrayList<String> mImageUrls = new ArrayList<>();
    private ArrayList<String> mPlatformNames = new ArrayList<>();
    private ArrayList<String> mPlatformLinks = new ArrayList<>();

    // References
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        Objects.requireNonNull(getSupportActionBar()).setTitle(Global.lookedUpName);

        initAllPlatforms();
    }

    private void initAllPlatforms() {
        db.collection("platforms")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                ArrayList<String> platformInfo = new ArrayList<>();
                                if(!document.getId().isEmpty()){platformInfo.add(document.getId()); }
                                if(!document.get("image").toString().isEmpty()){ platformInfo.add(document.get("image").toString()); }
                                if(!document.get("link").toString().isEmpty()){ platformInfo.add(document.get("link").toString()); }
                                if(!document.get("name").toString().isEmpty()){ platformInfo.add(document.get("name").toString()); }
                                Log.d(TAG, "Error getting documents: ", task.getException());
                                MakeHashMap(platformInfo);
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                        getUID();
                    }
                });
    }
    private void MakeHashMap(ArrayList<String> array) {
        platformhashmap.put(array.get(0), array);
    }

    protected void getUID(){
        db.collection("users")
                .whereEqualTo("username", Global.lookedUpUsername)
                .addSnapshotListener(this, new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        //Check if something went wrong
                        if (e !=  null){
                            Log.d(TAG, e.toString());
                            return;
                        }

                        boolean userExists = false;

                        for (DocumentChange dc : queryDocumentSnapshots.getDocumentChanges()){
                            DocumentSnapshot documentSnapshot = dc.getDocument();
                            userExists = true;
                            String uid = documentSnapshot.getId();
                            Global.lookedUpName = documentSnapshot.get("name").toString();
                            Objects.requireNonNull(getSupportActionBar()).setTitle(Global.lookedUpName);
                            getSocials(uid);
                        }

                        if(!userExists){
                            startActivity(new Intent(UserActivity.this, MainActivity.class));
                            Toast.makeText(UserActivity.this, "Username does not exist!", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    protected void getSocials(String uid) {
        db.collection("users")
                .document(uid)
                .collection("socials")
                .orderBy("platform")
                .addSnapshotListener(this, new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        //Check if something went wrong
                        if (e !=  null){
                            Log.d(TAG, e.toString());
                            return;
                        }
                        for (DocumentChange dc : queryDocumentSnapshots.getDocumentChanges()){
                            DocumentSnapshot documentSnapshot = dc.getDocument();
                            Social social = documentSnapshot.toObject(Social.class);
                            final String platformPath = documentSnapshot.getDocumentReference("platform").getPath();
                            String[] platformPathParts = platformPath.split("/");
                            String platform = platformPathParts[1];
                            socialArray.clear();
                            switch (dc.getType()){
                                case ADDED:
                                    socialArray.add(social.getUsername());
                                    socialArray.add(documentSnapshot.getId());
                                    initImageBitmaps(socialArray, platform);
                                    break;
                                case MODIFIED:
                                case REMOVED:
                                    break;
                            }
                        }
                    }
                });
    }
    private void initImageBitmaps(ArrayList<String> socialArray, String platform){
        platformData.clear();

        if(platformhashmap.containsKey(platform)){
            platformData = platformhashmap.get(platform);
        } else { return; }

        if(!socialArray.get(0).isEmpty()){
            mUsernames.add(socialArray.get(0));
        } else {
            mUsernames.add("Username add error");
        }

        if(!platformData.get(1).isEmpty()){
            mImageUrls.add(platformData.get(1));
        } else {
            mImageUrls.add("Image add error");
        }

        if(!platformData.get(2).isEmpty()){
            mPlatformLinks.add(platformData.get(2));
        } else {
            mPlatformLinks.add("Platform link add error");
        }

        if(!platformData.get(3).isEmpty()){
            mPlatformNames.add(platformData.get(3));
        } else {
            mPlatformNames.add("Platform name add error");
        }

        if(!socialArray.get(0).isEmpty()){
            platformData.add(socialArray.get(0));
        } else {
            platformData.add("Platform name add error");
        }

        initRecyclerView();
    }

    private void initRecyclerView(){
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        RecyclerViewAdapterUser adapter = new RecyclerViewAdapterUser(this, mUsernames, mImageUrls, mPlatformNames, mPlatformLinks);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        startActivity(new Intent(UserActivity.this, MainActivity.class));
        finish();
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
