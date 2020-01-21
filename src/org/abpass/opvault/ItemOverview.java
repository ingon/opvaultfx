package org.abpass.opvault;

import java.util.List;

import org.json.zero.hl.JsonStringHandler;
import org.json.zero.hl.JsonTypedHandler;

public class ItemOverview {
    static JsonTypedHandler<ItemOverview> newParser() {
        Json<ItemOverview> handler = new Json<ItemOverview>(ItemOverview::new);
        handler.stringProperty("title", (t, o) -> t.title = o);
        handler.stringProperty("ainfo", (t, o) -> t.ainfo = o);
        
        handler.stringProperty("url", (t, o) -> t.url = o);
        
        handler.arrayProperty("URLs", ItemOverviewUrl.newParser(), (t, o) -> t.urls = o);
        handler.arrayProperty("tags", new JsonStringHandler(), (t, o) -> t.tags = o);

        handler.numberProperty("ps", (t, o) -> t.ps = o.intValue());
        
        // TODO
        handler.valueProperty("pbe", (t, o) -> System.out.println("pbe: " + o));
        handler.valueProperty("pgrng", (t, o) -> System.out.println("pgrng: " + o));
        
        return handler;
    }
    
    private String title;
    private String ainfo;
    private String url;
    private List<ItemOverviewUrl> urls;
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

    static class ItemOverviewUrl {
        static JsonTypedHandler<ItemOverviewUrl> newParser() {
            var handler = new Json<ItemOverviewUrl>(ItemOverviewUrl::new);
            
            handler.stringProperty("u", (t, o) -> t.u = o);
            handler.stringProperty("l", (t, o) -> t.l = o);
            
            return handler;
        }
        
        String u;
        String l;
    }
}

