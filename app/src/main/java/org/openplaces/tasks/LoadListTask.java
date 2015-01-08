package org.openplaces.tasks;

import android.content.Context;
import android.util.Log;

import org.openplaces.MapActivity;
import org.openplaces.lists.ListManager;
import org.openplaces.lists.PlaceList;
import org.openplaces.lists.PlaceListItem;
import org.openplaces.remote.OpenPlacesRemote;
import org.openplaces.search.ResultSet;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by gabriele on 1/7/15.
 */
public class LoadListTask extends OpenPlacesAsyncTask {

    private PlaceList list;
    private Context appContext;

    public LoadListTask(PlaceList list, Context appContext, OpenPlacesAsyncTaskListener listener) {
        super(listener);
        this.list = list;
        this.appContext = appContext;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        Log.d(MapActivity.LOGTAG, "Getting places in starred list " + list);
        OpenPlacesRemote opr = OpenPlacesRemote.getInstance(this.appContext);
        ListManager lm = ListManager.getInstance(this.appContext);
        ResultSet rs;
        if(list == null){
            rs = opr.getPlacesByTypesAndIds(lm.getAllStarredPlaces());
        }
        else {
            Set<String> places = new HashSet<String>();
            for(PlaceListItem item: list.getPlacesInList()){
                places.add(item.getOsmType()+":"+item.getOsmId());
            }
            rs = opr.getPlacesByTypesAndIds(places);
        }

        this.setResult(rs, 0);
        return null;
    }
}
