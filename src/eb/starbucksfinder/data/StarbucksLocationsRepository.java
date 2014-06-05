package eb.starbucksfinder.data;

import com.google.appengine.api.datastore.*;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import eb.starbucksfinder.models.StarbucksLocation;

import java.util.ArrayList;
import java.util.List;

public class StarbucksLocationsRepository {
    private DatastoreService _datastoreService;

    public StarbucksLocationsRepository() {
        _datastoreService = DatastoreServiceFactory.getDatastoreService();
    }

    public List<StarbucksLocation> GetNearbyStarbucksByGeohash(String geoHash) {
        Query locationQuery = new Query("StarbucksLocation");

        locationQuery.setFilter(
            new Query.FilterPredicate("geoHash4", Query.FilterOperator.EQUAL, geoHash));

        List<Entity> entities =
            _datastoreService.prepare(locationQuery).asList(FetchOptions.Builder.withLimit(50));

        ArrayList<StarbucksLocation> starbucksLocations = new ArrayList<StarbucksLocation>();

        for (Entity entity : entities) {
            starbucksLocations.add(StarbucksLocation.fromEntity(entity));
        }

        return starbucksLocations;
    }

    public void SaveStarbucksLocations(List<StarbucksLocation> starbucksLocations) {
        ArrayList<Entity> entities = new ArrayList<Entity>();

        for(StarbucksLocation starbucksLocation : starbucksLocations) {
            entities.add(starbucksLocation.toEntity());
        }

        DeleteAllRecords();

        _datastoreService.put(entities);
    }

    private void DeleteAllRecords() {
        Query deleteAll = new Query("StarbucksLocation");

        Iterable<Entity> entities = _datastoreService.prepare(deleteAll).asIterable();

        for (Entity entity : entities) {
            _datastoreService.delete(entity.getKey());
        }
    }
}
