package eb.starbucksfinder.models;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.labs.repackaged.org.json.JSONException;
import com.google.appengine.labs.repackaged.org.json.JSONObject;

public class StarbucksLocation {
    private final String _city;
    private final String _address;
    private final Double _lat;
    private final Double _lng;
    private final String _geoHash4;

    public StarbucksLocation(String city, String address, Double lat, Double lng, String geoHash4) {
        _city = city;
        _address = address;
        _lat = lat;
        _lng = lng;
        _geoHash4 = geoHash4;
    }

    public String getCity() {
        return _city;
    }

    public String getAddress() {
        return _address;
    }

    public Double getLat() {
        return _lat;
    }

    public Double getLng() {
        return _lng;
    }

    public String getGeoHash4() {
        return _geoHash4;
    }

    public JSONObject toJSON()
            throws JSONException {
        JSONObject json = new JSONObject();

        json.put("city", _city);
        json.put("address", _address);

        return json;
    }

    public Entity toEntity() {
        Entity starbucksLocation = new Entity("StarbucksLocation");

        starbucksLocation.setProperty("city", _city);
        starbucksLocation.setProperty("address", _address);
        starbucksLocation.setProperty("lat", _lat);
        starbucksLocation.setProperty("lng", _lng);
        starbucksLocation.setProperty("geoHash4", _geoHash4);

        return starbucksLocation;
    }

    public static StarbucksLocation fromEntity(Entity entity) {
        String city = entity.getProperty("city").toString();
        String address = entity.getProperty("address").toString();
        Double lat = (Double)entity.getProperty("lat");
        Double lng = (Double)entity.getProperty("lng");
        String geoHash4 = entity.getProperty("geoHash4").toString();

        return new StarbucksLocation(city, address, lat, lng, geoHash4);
    }
}