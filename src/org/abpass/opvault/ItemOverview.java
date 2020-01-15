package org.abpass.opvault;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ItemOverview {
    public final Map<String, Object> data;

    public ItemOverview(Map<String, Object> data) {
        this.data = data;
    }

    public String getTitle() {
        return (String) data.get("title");
    }
    
    public List<URL> getURLs() {
        var raw = (List<Object>) data.get("URLs");
        if (raw == null) {
            return Collections.emptyList();
        }
        return raw.stream()
                .map((o) -> (Map<String, Object>)o)
                .map((js) -> {
                    try {
                        var s = (String) js.get("u");
                        return new URL(s);
                    } catch (MalformedURLException e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList());
    }
    
    public URL getURL() {
        var raw = (String) data.get("url");
        if (raw == null) {
            return null;
        }
        try {
            return new URL(raw);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
    
    public String getAInfo() {
        return (String) data.get("ainfo");
    }

    public List<String> getTags() {
        var raw = (List<Object>) data.get("tags");
        if (raw == null) {
            return Collections.emptyList();
        }
        return raw.stream()
                .map((o) -> (String)o)
                .collect(Collectors.toList());
    }

    public Integer getPs() {
        var n = (Number) data.get("ps");
        if (n == null) {
            return null;
        }
        return n.intValue();
    }
    
    @Override
    public String toString() {
        return String.format("overview [ title=%s, url=%s, ainfo=%s, ps=%s, urls=%s, tags=%s]",
            getTitle(), getURL(), getAInfo(), getPs(), getURLs(), getTags());
    }
}
