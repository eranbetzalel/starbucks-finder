package eb.starbucksfinder.web;

import ch.hsr.geohash.GeoHash;
import com.google.appengine.labs.repackaged.org.json.JSONArray;
import com.google.appengine.labs.repackaged.org.json.JSONException;
import com.google.appengine.labs.repackaged.org.json.JSONObject;
import com.google.code.geocoder.Geocoder;
import com.google.code.geocoder.GeocoderRequestBuilder;
import com.google.code.geocoder.model.GeocodeResponse;
import com.google.code.geocoder.model.GeocoderRequest;
import com.google.code.geocoder.model.GeocoderResult;
import com.google.code.geocoder.model.LatLng;
import eb.starbucksfinder.data.StarbucksLocationsRepository;
import eb.starbucksfinder.models.StarbucksLocation;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class StarbucksFinderServlet extends HttpServlet {
    private StarbucksLocationsRepository _starbucksLocationsRepository;

    public void init() {
        _starbucksLocationsRepository = new StarbucksLocationsRepository();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        JSONObject jsonResult = new JSONObject();
        PrintWriter out = res.getWriter();

        res.setContentType("application/jsonResult");
        res.setCharacterEncoding("utf-8");

        String address = req.getParameter("address");

        if(address == null) {
            try {
                jsonResult.put("error", "Address field is empty.");
                out.print(jsonResult.toString());

                return;
            } catch (JSONException e) {
                e.printStackTrace();

                throw new ServletException(e);
            }
        }

        LatLng addressLocation = GetAddressLatLng(address);

        if(addressLocation == null) {
            try {
                jsonResult.put("error", "Could not find address coordinates");
                out.print(jsonResult.toString());

                return;
            } catch (JSONException e) {
                e.printStackTrace();

                throw new ServletException(e);
            }
        }

        String addressGeoHash = GetAddressGeoHash(addressLocation);

        List<StarbucksLocation> starbucksLocations =
            _starbucksLocationsRepository.GetNearbyStarbucksByGeohash(addressGeoHash);

        starbucksLocations = FilterBy5KmDistance(addressLocation, starbucksLocations);

        JSONArray locations = new JSONArray();

        for(StarbucksLocation starbucksLocation : starbucksLocations) {
            try {
                locations.put(starbucksLocation.toJSON());
            } catch (JSONException e) {
                e.printStackTrace();

                throw new ServletException(e);
            }
        }

        try {
            jsonResult.put("locations", locations);
        } catch (JSONException e) {
            e.printStackTrace();

            throw new ServletException(e);
        }

        out.print(jsonResult.toString());
    }

    private List<StarbucksLocation> FilterBy5KmDistance(
            LatLng addressLocation, List<StarbucksLocation> starbucksLocations) {
        ArrayList<StarbucksLocation> filteredStarbucksLocations = new ArrayList<StarbucksLocation>();

        for(StarbucksLocation starbucksLocation : starbucksLocations) {
            Double distance =
                GetDistanceBetweenPoints(
                    addressLocation.getLat().doubleValue(),
                    addressLocation.getLng().doubleValue(),
                    starbucksLocation.getLat(),
                    starbucksLocation.getLng());

            if(distance <= 5) {
                filteredStarbucksLocations.add(starbucksLocation);
            }
        }

        return filteredStarbucksLocations;
    }

    private LatLng GetAddressLatLng(String address)
            throws IOException, ServletException {
        Geocoder geocoder = new Geocoder();

        GeocoderRequest geocoderRequest =
            new GeocoderRequestBuilder()
                .setAddress(address)
                .setLanguage("en")
                .getGeocoderRequest();

        GeocodeResponse geocoderResponse = geocoder.geocode(geocoderRequest);

        List<GeocoderResult> geocoderResults = geocoderResponse.getResults();

        if(geocoderResults.isEmpty()) {
            return null;
        }

        GeocoderResult firstResult = geocoderResults.iterator().next();

        return firstResult.getGeometry().getLocation();
    }

    private String GetAddressGeoHash(LatLng latLng) {
        // 4 chars bounding box size = 39.1km x 19.5km
        GeoHash geoHash =
            GeoHash.withCharacterPrecision(
                latLng.getLat().doubleValue(),
                latLng.getLng().doubleValue(),
                4);

        return geoHash.toBase32();
    }

    public static double GetDistanceBetweenPoints(double lat1, double lng1, double lat2, double lng2) {
        double earthRadius = 3958.75;
        double dLat = Math.toRadians(lat2-lat1);
        double dLng = Math.toRadians(lng2-lng1);
        double sindLat = Math.sin(dLat / 2);
        double sindLng = Math.sin(dLng / 2);
        double a = Math.pow(sindLat, 2) + Math.pow(sindLng, 2)
                * Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2));
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double dist = earthRadius * c;

        return dist;
    }
}
