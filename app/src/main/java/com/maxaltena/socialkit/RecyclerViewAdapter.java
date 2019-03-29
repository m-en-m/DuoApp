package com.maxaltena.socialkit;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
    private static final String TAG = "RecyclerViewAdapter";
    private  final String PLATFORM_IMAGE = "Platform Image";
    private  final String USERNAME = "Username";

    private ArrayList<String> mUsernames = new ArrayList<>();
    private ArrayList<String> mImages = new ArrayList<>();
    private ArrayList<String> mPlatformNames = new ArrayList<>();
    private ArrayList<String> mPlatformLinks = new ArrayList<>();
    private ArrayList<String> mPlatformImages = new ArrayList<>();
    private Context mContext;


    public RecyclerViewAdapter(Context mContext, ArrayList<String> mUsernames, ArrayList<String> mImages,  ArrayList<String> mPlatformNames,  ArrayList<String> mPlatformLinks) {
        this.mUsernames = mUsernames;
        this.mImages = mImages;
        this.mContext = mContext;
        this.mPlatformNames = mPlatformNames;
        this.mPlatformLinks = mPlatformLinks;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_listitem, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        Log.d(TAG, "onBindViewHolder: called");

        Glide.with(mContext)
                .asBitmap()
                .load(mImages.get(position))
                .into(holder.image);

        holder.username.setText(mUsernames.get(position));

        holder.parentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent socialIntent = new Intent (v.getContext(), SocialActivity.class);
                socialIntent.putExtra(PLATFORM_IMAGE, mImages.get(position));
                socialIntent.putExtra(USERNAME, mUsernames.get(position));
                v.getContext().startActivity(socialIntent);
                Log.d(TAG, "onClick : Clicked on:" + mUsernames.get(position));
            }

        });
    }

    @Override
    public int getItemCount() {
        return mUsernames.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView image;
        TextView username;
        RelativeLayout parentLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image);
            username = itemView.findViewById(R.id.username);
            parentLayout = itemView.findViewById(R.id.social_card);
        }
    }
}
