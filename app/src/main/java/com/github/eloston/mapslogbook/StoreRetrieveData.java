package com.github.eloston.mapslogbook;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.util.ArrayList;

public class StoreRetrieveData {
    private Context context;
    private String fileName;

    public StoreRetrieveData(Context context, String filename) {
        this.context = context;
        this.fileName = filename;
    }

    public static JSONArray toJSONArray(ArrayList<Entry> entries) throws JSONException {
        JSONArray jsonArray = new JSONArray();
        for(Entry entry : entries){
            JSONObject jsonObject = entry.toJSON();
            jsonArray.put(jsonObject);
        }
        return  jsonArray;
    }

    public void saveToFile(ArrayList<Entry> entries) throws JSONException, IOException {
        FileOutputStream fileOutputStream;
        OutputStreamWriter outputStreamWriter;
        fileOutputStream = this.context.openFileOutput(this.fileName, Context.MODE_PRIVATE);
        outputStreamWriter = new OutputStreamWriter(fileOutputStream);
        outputStreamWriter.write(toJSONArray(entries).toString());
        outputStreamWriter.close();
        fileOutputStream.close();
    }

    public ArrayList<Entry> loadFromFile() throws IOException, JSONException, MalformedURLException {
        ArrayList<Entry> entries = new ArrayList<>();
        BufferedReader bufferedReader = null;
        FileInputStream fileInputStream = null;
        try {
            fileInputStream =  this.context.openFileInput(this.fileName);
            StringBuilder builder = new StringBuilder();
            String line;
            bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));
            while ((line = bufferedReader.readLine()) != null) {
                builder.append(line);
            }

            JSONArray jsonArray = (JSONArray)new JSONTokener(builder.toString()).nextValue();
            for (int i = 0; i < jsonArray.length(); i++) {
                Entry entry = new Entry(jsonArray.getJSONObject(i));
                entries.add(entry);
            }
        } catch (FileNotFoundException fnfe) {
            // File does not exist on first run
        } finally {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (fileInputStream != null) {
                fileInputStream.close();
            }
        }
        return entries;
    }
}
