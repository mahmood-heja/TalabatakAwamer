package com.talabatakawamer.talabatakawamer.postOnPhp;


import android.os.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PhpTask extends AsyncTask<String, String, JSONObject> {


    //ArrayList to carry your values
    private List<NameValuePair> data ;
    private PhpTaskFinished taskFinshed;


    public PhpTask(List<NameValuePair> valuePairs ) {

        data= new ArrayList<>();

        //here we sit the key "msg" to carry your value which you passed on thread execute.
        data =valuePairs;
    }

    @Override
    protected JSONObject doInBackground(String... urls) {
        JSONParser jParser = new JSONParser();

        try {

            //url of Php file on host
            String url=urls[0];

            //Then create the JSONObject to make the rquest and pass the data(ArrayList).


            return jParser.makeHttpRequest(url, "POST", data);
        } catch (Exception e) {
            /* Any Exception here */
        }

        return null;

    }

    @Override
    protected void onPostExecute(JSONObject Json) {
        // This After the thread did it's operate
        //if(Json!=null)
        try {
            taskFinshed.onFinished(Json);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


    public void onPhpTaskFinished(PhpTaskFinished Finished)
    {
        taskFinshed=Finished;
    }


    public interface PhpTaskFinished {

        void onFinished(JSONObject result) throws JSONException;
    }
}



