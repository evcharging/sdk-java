package com.evchargings.union;

import java.util.Collection;
import org.json.JSONArray;

/**
 * This provides some public utils
 */
public class Utils {
    public static JSONArray makeJSONArray(Object[] source) {
        JSONArray array = new JSONArray();
        IFromJSON[] os = (IFromJSON[])source;
        for (IFromJSON o : os) {
            array.put(o.toJson());
        }
        return array;
    }
    public static JSONArray makeJSONArray(Collection<? extends IFromJSON> source) {
        JSONArray array = new JSONArray();
        for (IFromJSON o : source) {
            array.put(o.toJson());
        }
        return array;
    }
}
