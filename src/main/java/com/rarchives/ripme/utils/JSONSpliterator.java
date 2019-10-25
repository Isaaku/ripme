package com.rarchives.ripme.utils;

import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.json.JSONArray;
import org.json.JSONObject;

public class JSONSpliterator extends Spliterators.AbstractSpliterator<JSONObject> {
    private JSONArray array;
    private int index = 0;

    public JSONSpliterator(JSONArray array) {
        super(array.length(), SIZED | ORDERED);
        this.array = array;
    }

    @Override
    public boolean tryAdvance(Consumer<? super JSONObject> action) {
        if (index == array.length()) {
            return false;
        }
        action.accept(array.getJSONObject(index++));
        return true;
    }

    public static Stream<JSONObject> getStreamOfJsonArray(JSONArray array) {
        return StreamSupport.stream(new JSONSpliterator(array), false);
    }
}
