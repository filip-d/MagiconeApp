package com.magic_foo.magiconeapp;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by filip on 05/12/2015.
 */
public class Place {

    private String sound;
    private String description;
    private String imageUrl;
    private String direction;
    private int distance;

    public Place(JSONObject json) throws JSONException {

        sound = json.getString("sound");
        description = json.getString("description");
        imageUrl = json.getString("image");
        direction = json.getString("direction");
        distance = json.getInt("distance");

    }

    public String getSound() {
        return sound;
    }

    public void setSound(String sound) {
        this.sound = sound;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }
}
