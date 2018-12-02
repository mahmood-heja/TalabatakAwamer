package com.talabatakawamer.talabatakawamer.postOnPhp;


import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;


public class JSONParser {

    private InputStream is = null;
    private JSONObject jObj = null;

    // constructor
    JSONParser() {

    }

    // function get json from url
    // by making HTTP POST or GET mehtod
    public JSONObject makeHttpRequest(String url, String method, @Nullable List<NameValuePair> params) {

        // Making HTTP request
        try {
            // check for request method
            if (method.equals("POST")) {
                // request method is POST
                URL Url = new URL(url);

                HttpURLConnection conn = (HttpURLConnection) Url.openConnection();
                conn.setReadTimeout(4000);
                conn.setConnectTimeout(4000);
                conn.setRequestMethod(method);
                conn.setDoInput(true);
                conn.setDoOutput(true);


                OutputStream os = conn.getOutputStream();

                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write(getPostDataString(params));

                writer.flush();
                writer.close();
                os.close();


                is = conn.getInputStream();

            }

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }


        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"), 8);

            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            is.close();
            jObj = new JSONObject(sb.toString());

        } catch (IOException | JSONException e) {
            Log.e("JSONParser", e.getMessage());
        }

        // return JSON String
        return jObj;

    }


    private String getPostDataString(List<NameValuePair> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for (int i = 0; i < params.size(); i++) {
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(params.get(i).getName(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(params.get(i).getValue(), "UTF-8"));
        }

        return result.toString();
    }
}