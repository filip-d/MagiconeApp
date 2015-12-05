package com.magic_foo.magiconeapp;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by filip on 05/12/2015.
 */
public class PlaceLevel {

    private List<Place> places;

    public PlaceLevel(JSONObject json) throws JSONException {
        JSONArray tempJsonArray = json.getJSONArray("places");
        places = new ArrayList<>();
        for (int i = 0; i < tempJsonArray.length(); i++) {
            places.add(new Place(tempJsonArray.getJSONObject(i)));
        }
    }

    public List<Place> getPlaces() {
        return places;
    }

    public Place getItem(int location) {
        return places.get(location);
    }

}
