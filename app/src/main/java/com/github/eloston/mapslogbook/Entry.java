package com.github.eloston.mapslogbook;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.UUID;
import java.net.URL;
import java.net.MalformedURLException;

// Implements an Entry, which represents a saved place or route

public class Entry implements Serializable {
    // JSON representation keys
    private static final String KEY_NAME = "entry_name";
    private static final String KEY_COLOR = "entry_color";
    private static final String KEY_IDENTIFIER = "entry_identifier";
    private static final String KEY_TYPE = "entry_type";
    private static final String KEY_URL = "entry_url";
    private static final String KEY_NOTES = "entry_notes";

    // Entry types
    public static final int TYPE_AUTODETECT = -1; // Must be resolved to another type
    public static final int TYPE_PLACE = 0;
    public static final int TYPE_ROUTE = 1;

    // Serializable member data
    private String name;
    private int color;
    private UUID identifier;
    private int type;
    private URL url;
    private String notes;

    // Construct a new Entry based on a JSON representation
    public Entry(JSONObject jsonObject) throws JSONException, MalformedURLException {
        this.name = jsonObject.getString(KEY_NAME);
        this.color = jsonObject.getInt(KEY_COLOR);
        this.identifier = UUID.fromString(jsonObject.getString(KEY_IDENTIFIER));
        this.type = jsonObject.getInt(KEY_TYPE);
        if (this.type != this.TYPE_PLACE && this.type != this.TYPE_ROUTE) {
            throw new JSONException("Invalid entry type: " + this.type);
        }
        String urlValue = jsonObject.getString(KEY_URL);
        if (urlValue == null) {
            this.url = null;
        } else {
            this.url = new URL(urlValue);
        }
        this.notes = jsonObject.getString(KEY_NOTES);
    }

    // Construct a new, empty Entry
    public Entry() {
        this.name = new String();
        this.color = 1677725;
        this.identifier = UUID.randomUUID();
        this.type = this.TYPE_AUTODETECT;
        this.url = null;
        this.notes = new String();
    }

    // Construct JSON representation of current state
    public JSONObject toJSON() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(KEY_NAME, this.name);
        jsonObject.put(KEY_COLOR, this.color);
        jsonObject.put(KEY_IDENTIFIER, this.identifier.toString());
        jsonObject.put(KEY_TYPE, this.type);
        if (this.url == null) {
            jsonObject.put(KEY_URL, null);
        } else {
            jsonObject.put(KEY_URL, this.url.toString());
        }
        jsonObject.put(KEY_NOTES, this.notes);

        return jsonObject;
    }

    // Getters and setters below

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getColor() {
        return this.color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public UUID getIdentifier() {
        return this.identifier;
    }

    public URL getURL() {
        return this.url;
    }

    public void setURL(URL url) {
        this.url = url;
    }

    public String getNotes() {
        return this.notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}

