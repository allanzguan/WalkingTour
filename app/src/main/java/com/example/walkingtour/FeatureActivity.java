package com.example.walkingtour;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class FeatureActivity extends AppCompatActivity {

    TextView buildingName, buildingAddress, buildDescription;
    ImageView buildingImage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feature);


        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.drawable.home_image);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayUseLogoEnabled(true);

        Building b = (Building) getIntent().getSerializableExtra("BUILDING");

        buildingName = findViewById(R.id.buildingName);
        buildingName.setText(b.getId());

        buildingAddress = findViewById(R.id.buildingAddress);
        buildingAddress.setText(b.getAddress());

        buildDescription = findViewById(R.id.buildingDescription);
        buildDescription.setText(b.getDescription());
        buildDescription.setMovementMethod(new ScrollingMovementMethod());

        buildingImage = findViewById(R.id.buildingImage);

        if(!b.getImage().isEmpty()){
            Picasso.get().load(b.getImage())
                    .error(R.drawable.logo)

                    .fit().centerInside()
                    .into(buildingImage);
        }


    }
}