package org.lenchan139.trafficnews.Class;

/**
 * Created by len on 19/5/2017.
 */

public class MapApiUrlHandler {
    String ApiKey = "AIzaSyCYOFJqYa-ohamM-FEypMtt7YE5DSGXF1U";
    String language = "zh-HK";
    public String mapApiJsonUrl(double lat,double lng){
        return "https://maps.googleapis.com/maps/api/geocode/json?latlng=" +
                lat + "," +  lng +
                "&lang=" +language +
                "&key=" + ApiKey;
    }
}
