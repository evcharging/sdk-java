package com.evchargings.union;

import org.json.JSONObject;

/**
 * internal use only.
 */
public interface IFromJSON {
    void initWithJson(JSONObject jsonObject);
    JSONObject toJson();
}
