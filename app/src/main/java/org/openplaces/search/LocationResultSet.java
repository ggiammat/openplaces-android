package org.openplaces.search;

import org.openplaces.model.OPLocationInterface;
import org.openplaces.places.Place;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ggiammat on 1/29/15.
 */
public class LocationResultSet {

    private List<OPLocationInterface> locations;

    public Map<String, String> getStats() {
        return stats;
    }

    private Map<String, String> stats;

    public LocationResultSet(){
        this.locations = new ArrayList<OPLocationInterface>();
        this.stats = new HashMap<String, String>();
        this.addStat("errorCode", "0");
    }

    public void addStat(String name, String value){
        this.stats.put(name, value);
    }

    public List<OPLocationInterface> getLocations() {
        return locations;
    }

    public void setLocations(List<OPLocationInterface> locations) {
        this.locations = locations;
    }


}
