package com.example.walkingtour;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class TourPathRunnable implements Runnable{

    private FenceManager fm;
    private MapsActivity ma;
    TourPathRunnable(FenceManager fm, MapsActivity ma){
        this.fm = fm;
        this.ma = ma;
    }

    public void run(){
        Log.d("JSONOBJJJ", "DOWNLOADING");
        HttpURLConnection connection = null;
        BufferedReader reader = null;
        try{
            String urlString = "http://www.christopherhield.com/data/WalkingTourContent.json";
//            Uri.Builder buildURL = Uri.parse(urlString).buildUpon();
//            String urlToUse = buildURL.build().toString();
            URL url = new URL(urlString);
            Log.d("JSONOBJJJ", "run: Full URL: " + url);

            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            connection.setRequestProperty("Accept", "application/json");
            connection.connect();


            int responseCode = connection.getResponseCode();
            Log.d("JSONOBJJJ", "reponse: " + responseCode);


            final StringBuilder sb = new StringBuilder();

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                while (null != (line = reader.readLine())) {
                    sb.append(line);
                }
                Log.d("JSONOBJJJ", "run: " + sb.toString());

                try{
                    Log.d("JSONOBJJJ", "try: " );
                    JSONObject jo = new JSONObject(sb.toString());
                    JSONArray fences = jo.getJSONArray("fences");
                    JSONArray path = jo.getJSONArray("path");
                    Log.d("JSONOBJJJ", fences.toString());
                    Log.d("JSONOBJJJ", path.toString());

                    parseBuilding(fences);
                    parsePath(path);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } else {
                Log.d("JSONOBJJJ", "else: " + sb.toString());
                reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                String line;
                while (null != (line = reader.readLine())) {
                    sb.append(line);
                }
//                mainActivity.apiKeyError();
            }




        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private void parsePath(JSONArray p){
        JSONArray path = p;
        ArrayList<LatLng> pathList = new ArrayList<>();

        for(int i = 0; i < path.length(); i++){
            try {
                String pair = path.getString(i);
                List<String> coord = Arrays.asList(pair.split("\\s*,\\s*"));

//                Log.d("PATHHH", coord.get(0) + "   +   " + coord.get(1));
                pathList.add(new LatLng(Double.parseDouble(coord.get(1)), Double.parseDouble(coord.get(0))) );
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        Log.d("PATHHH", "runnable   " + pathList.size());
        ma.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ma.addPath(pathList);
            }
        });
    }

    private void parseBuilding(JSONArray f){
        JSONArray fences = f;
        HashMap<String, Building> buildingList = new HashMap<String, Building>();
        for(int i = 0; i < fences.length(); i++){
            try {
                JSONObject b = fences.getJSONObject(i);
                Building building = new Building(b.getString("id"),
                        b.getString("address"),
                        b.getString("latitude"),
                        b.getString("longitude"),
                        b.getString("radius"),
                        b.getString("description"),
                        b.getString("fenceColor"),
                        b.getString("image"));
                buildingList.put(building.getId(), building);


            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        Log.d("FENCEEE", "in download " + buildingList.size());
        fm.setBuildingList(buildingList);

        ma.setBuildingList(buildingList);


        ma.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ma.addFence();
            }
        });
    }
}
