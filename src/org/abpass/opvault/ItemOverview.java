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
        
        handler.numberProperty("pbe", (t, o) -> t.pbe = o.doubleValue());
        handler.booleanProperty("pgrng", (t, o) -> t.pgrng = o);
        
        return handler;
    }
    
    private String title;
    private String ainfo;
    private String url;
    private List<ItemOverviewUrl> urls;
    private List<String> tags;
    private Integer ps;
    
    private Boolean pgrng;
    private Double pbe;

    ItemOverview() {
    }
    
    public String getTitle() {
        return title;
    }
    
    public String getAinfo() {
        return ainfo;
    }
    
    public String getUrl() {
        return url;
    }
    
    public List<ItemOverviewUrl> getUrls() {
        return urls;
    }
    
    public List<String> getTags() {
        return tags;
    }
    
    public Integer getPs() {
        return ps;
    }
    
    public Boolean getPgrng() {
        return pgrng;
    }
    
    public Double getPbe() {
        return pbe;
    }
    
    public static class ItemOverviewUrl {
        static JsonTypedHandler<ItemOverviewUrl> newParser() {
            var handler = new Json<ItemOverviewUrl>(ItemOverviewUrl::new);
            
            handler.stringProperty("u", (t, o) -> t.u = o);
            handler.stringProperty("l", (t, o) -> t.l = o);
            
            return handler;
        }
        
        private String u;
        private String l;
        
        public String getU() {
            return u;
        }
        
        public String getL() {
            return l;
        }
    }
}

