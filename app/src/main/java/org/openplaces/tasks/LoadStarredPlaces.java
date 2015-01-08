package org.openplaces.tasks;

import android.content.Context;

import org.openplaces.lists.ListManager;
import org.openplaces.remote.OpenPlacesRemote;
import org.openplaces.search.ResultSet;

/**
 * Created by gabriele on 1/7/15.
 */
public class LoadStarredPlaces extends OpenPlacesAsyncTask {

    private Context appContext;

    public LoadStarredPlaces(Context appContext, OpenPlacesAsyncTaskListener listener) {
        super(listener);
        this.appContext = appContext;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        ListManager lm = ListManager.getInstance(this.appContext);
        OpenPlacesRemote opr = OpenPlacesRemote.getInstance(this.appContext);
        ResultSet rs = opr.getPlacesByTypesAndIds(lm.getAllStarredPlaces());

        this.setResult(rs, 0);
        return null;
    }
}
