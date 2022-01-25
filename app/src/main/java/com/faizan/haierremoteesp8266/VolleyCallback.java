package com.faizan.haierremoteesp8266;

import org.json.JSONObject;

//https://stackoverflow.com/questions/33535435/how-to-create-a-proper-volley-listener-for-cross-class-volley-method-calling
public interface VolleyCallback {
        void onError(String message);
        void onResponse(JSONObject response);
    }