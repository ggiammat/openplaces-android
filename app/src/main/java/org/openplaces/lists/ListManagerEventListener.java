package org.openplaces.lists;

import org.openplaces.model.Place;

import java.util.List;

/**
 * Created by ggiammat on 11/19/14.
 */
public interface ListManagerEventListener {

    public void placeAddedToStarredList(Place place, PlaceList list);
    public void placeAddedToAutoList(Place place, PlaceList list);

    public void placeRemovedFromStarredList(Place place, PlaceList list);
    public void placeRemovedFromAutoList(Place place, PlaceList list);
    public void starredListAdded(PlaceList list);

}
