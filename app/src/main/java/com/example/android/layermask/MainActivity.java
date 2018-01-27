package com.example.android.layermask;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.example.android.layermask.views.CustomImageView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //get the ImageView
        final CustomImageView mImageView = (CustomImageView) findViewById(R.id.imageMask);
    }
}
