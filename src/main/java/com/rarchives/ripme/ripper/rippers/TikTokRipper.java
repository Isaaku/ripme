package com.rarchives.ripme.ripper.rippers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.rarchives.ripme.ripper.AbstractJSONRipper;
import com.rarchives.ripme.utils.Http;
import com.rarchives.ripme.utils.JSONSpliterator;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class TikTokRipper extends AbstractJSONRipper {
    private HashMap<String, String> urlNames = new HashMap<>();
    private Pattern urlPattern = Pattern.compile("^https?://www.tiktok.com/[@]([a-zA-Z0-9_-]*)/?");
    private Pattern namePattern = Pattern.compile("^https?://www.tiktok.com/[@]([a-zA-Z0-9_-]*)/video/([10-9]*)?");

    public TikTokRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    protected String getDomain() {
        return "www.tiktok.com";
    }

    @Override
    public String getHost() {
        return "tiktok";
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Matcher m = urlPattern.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }
        throw new MalformedURLException("Expected TikTok.com got " + url + " instead");
    }

    @Override
    protected JSONObject getFirstPage() throws IOException {
        Document document = Http.url(url).get();
        for (Element script : document.select("script")) {
            String scriptText = script.data();
            if (scriptText.startsWith("window.__INIT_PROPS__")) {
                String jsonText = scriptText.replaceAll("[^{]*([{].*})[^}]*", "$1");
                return new JSONObject(jsonText).getJSONObject("/@:uniqueId");
            }
        }
        return null;
    }

    @Override
    protected List<String> getURLsFromJSON(JSONObject json) {
        JSONArray itemsArray = json.getJSONArray("itemList");
        if (itemsArray.length() > 0)
            return JSONSpliterator.getStreamOfJsonArray(itemsArray).map(item -> {
                Matcher m = namePattern.matcher(item.getString("url"));
                if (m.matches())
                    urlNames.put(item.getString("contentUrl"), m.group(2));
                return item.getString("contentUrl");
            }).collect(Collectors.toList());
        return null;
    }

    @Override
    protected void downloadURL(URL url, int index) {
        addURLToDownload(url, getPrefix(index), "", null, null, urlNames.get(url.toExternalForm()), null, true);
    }

    @Override
    protected JSONObject getNextPage(JSONObject doc) throws IOException {
        /*
/share/item/list?
secUid=MS4wLjABAAAAPblO-zQCwjGKJinjW7uuvFT0e-2qrvtVCbpVPqSzsfCdASzwZ70DP7yal5SUgWxw (userData/secUid)
&id=62836546508197888 (userData/UserId)
&type=1
&count=48
&minCursor=0
&maxCursor=1567632832000
&shareUid=
&_signature=1ry-sAAgEBDh0KirVdPieNa8v6AAIsX
         */
        return super.getNextPage(doc);
    }

}