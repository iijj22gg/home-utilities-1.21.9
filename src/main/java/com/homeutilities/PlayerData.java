package com.homeutilities;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class PlayerData {
    private JsonObject homes = new JsonObject();

    public JsonObject getHomes() {
        return homes;
    }

    public void setHomes(String homes) {
        this.homes = JsonParser.parseString(homes).getAsJsonObject();
    }

    public String toString(){
        return homes.toString();
    }
}
