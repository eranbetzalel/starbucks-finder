package eb.starbucksfinder.web;

import ch.hsr.geohash.GeoHash;
import eb.starbucksfinder.data.StarbucksLocationsRepository;

import eb.starbucksfinder.models.StarbucksLocation;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import java.io.*;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class UploadStarbucksLocationsServlet extends HttpServlet {
    private StarbucksLocationsRepository _starbucksLocationsRepository;

    public void init() {
        _starbucksLocationsRepository = new StarbucksLocationsRepository();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        try {
            ServletFileUpload upload = new ServletFileUpload();
            res.setContentType("text/plain");

            FileItemIterator iterator = upload.getItemIterator(req);

            while (iterator.hasNext()) {
                FileItemStream item = iterator.next();
                InputStream stream = item.openStream();

                if (!item.isFormField()) {
                    String line;
                    BufferedReader br = new BufferedReader(new InputStreamReader(stream));
                    ArrayList<StarbucksLocation> starbucksLocations = new ArrayList<StarbucksLocation>();

                    //  Read CSV header line
                    br.readLine();

                    while((line = br.readLine()) != null) {
                        String[] lineParts = line.split(",");

                        String address = lineParts[3].trim();
                        String city = lineParts[2].trim();
                        Double lat = new Double(lineParts[1].trim());
                        Double lng = new Double(lineParts[0].trim());

                        GeoHash geoHash4 = GeoHash.withCharacterPrecision(lat, lng, 4);

                        starbucksLocations.add(
                            new StarbucksLocation(city, address, lat, lng, geoHash4.toBase32()));
                    }

                    _starbucksLocationsRepository.SaveStarbucksLocations(starbucksLocations);
                }
            }
        } catch (Exception ex) {
            throw new ServletException(ex);
        }
    }
}
