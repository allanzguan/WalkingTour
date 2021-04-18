package com.example.walkingtour;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;

public class Building implements Serializable {

    private String id, address, latitude, longitude, radius, description, fenceColor, image;
    private final int type = Geofence.GEOFENCE_TRANSITION_ENTER;

    Building(String id, String address, String latitude, String longitude, String radius, String description, String fenceColor, String image){
        this.id = id;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.radius = radius;
        this.description = description;
        this.fenceColor = fenceColor;
        this.image = image;
    }

    public String getId() {
        return id;
    }

    public String getAddress() {
        return address;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public float getRadius() {
        return Float.parseFloat(this.radius);
    }

    public String getDescription() {
        return description;
    }

    public String getFenceColor() {
        return fenceColor;
    }

    public String getImage() {
        return image;
    }

    public LatLng getLatLng(){
        return new LatLng(Double.parseDouble(this.latitude), Double.parseDouble(this.longitude));
    }

    public int getType(){ return type;}
}
