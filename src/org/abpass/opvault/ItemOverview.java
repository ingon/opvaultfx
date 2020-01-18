package org.abpass.opvault;

import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

import org.abpass.json.JsonArrayHandler;
import org.abpass.json.JsonStringHandler;
import org.abpass.json.JsonTypedHandler;

public class ItemOverview {
    static JsonTypedHandler<ItemOverview> newParser() {
        Json<ItemOverview> handler = new Json<ItemOverview>(ItemOverview::new);
        handler.stringProperty("title", (t, o) -> t.title = o);
        handler.stringProperty("ainfo", (t, o) -> t.ainfo = o);
        handler.urlProperty("url", (t, o) -> t.url = o);
        handler.arrayProperty("URLs", new JsonArrayHandler<>(ItemUrl.newParser()), 
            (t, o) -> t.urls = o.stream().map((u) -> u.url).collect(Collectors.toList()));
        handler.arrayProperty("tags", new JsonArrayHandler<String>(new JsonStringHandler()), (t, o) -> t.tags = o);
        handler.numberProperty("ps", (t, o) -> t.ps = o.intValue());
        return handler;
    }
    
    private String title;
    private String ainfo;
    private URL url;
    private List<URL> urls;
    private List<String> tags;
    private Integer ps;

    ItemOverview() {
    }
    
    public String getTitle() {
        return title;
    }
    
    public String getAinfo() {
        return ainfo;
    }
    
    @Override
    public String toString() {
        return String.format("overview [ title=%s, ainfo=%s, url=%s, urls=%s, tags=%s, ps=%s]",
            title, ainfo, url, urls, tags, ps);
    }
}
