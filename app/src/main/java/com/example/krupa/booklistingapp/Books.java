package com.example.krupa.booklistingapp;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class Books implements Parcelable{
    private String rTitle;
    private String rSubtitle;
    private ArrayList<String> rAuthors;
    private String rPublishedDate;

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(rTitle);
        out.writeString(rSubtitle);
        out.writeStringList(rAuthors);
        out.writeString(rPublishedDate);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    private Books(Parcel in) {
        rTitle = in.readString();
        rSubtitle = in.readString();
        in.readStringList(rAuthors);
        rPublishedDate = in.readString();
    }

    public Books(JSONObject object) {
        try {
            JSONObject volumeInfo = object.getJSONObject("volumeInfo");

            this.rTitle = volumeInfo.getString("title");
            this.rSubtitle = volumeInfo.getString("subtitle");
            this.rAuthors = new ArrayList<String>();

            JSONArray jsonArrayAuthors = volumeInfo.getJSONArray("authors");
            if (jsonArrayAuthors != null) {
                for (int i=0;i<jsonArrayAuthors.length();i++){
                    rAuthors.add(jsonArrayAuthors.get(i).toString());
                }
            }

            this.rPublishedDate = volumeInfo.getString("publishedDate");

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getTitle() { return rTitle; }
    public String getSubtitle() { return rSubtitle; }
    public String getPublishedDate() { return rPublishedDate; }
    public String getAuthors() {
        if (rAuthors == null || rAuthors.size() == 0) return "";
        return TextUtils.join(", ", rAuthors);
    }

    // Factory method to convert an array of JSON objects into a list of objects
    public static ArrayList<Books> fromJson(JSONArray jsonObjects) {
        ArrayList<Books> users = new ArrayList<Books>();
        for (int i = 0; i < jsonObjects.length(); i++) {
            try {
                users.add(new Books(jsonObjects.getJSONObject(i)));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return users;
    }
}
