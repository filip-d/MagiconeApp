package com.magic_foo.magiconeapp;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by filip on 05/12/2015.
 */
public class PlaceMap {

    private List<PlaceLevel> levels;

    public PlaceMap(JSONObject json) throws JSONException {
        JSONArray tempJsonArray = json.getJSONArray("levels");

        levels = new ArrayList<>();
        for (int i = 0; i < tempJsonArray.length(); i++) {
            levels.add(new PlaceLevel(tempJsonArray.getJSONObject(i)));
        }

    }

    public PlaceLevel getLevel(int level) {
        return levels.get(level);
    }

    public int getSize() {
        return levels.size();
    }


}
