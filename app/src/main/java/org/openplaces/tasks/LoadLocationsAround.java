package org.openplaces.tasks;

import android.content.Context;
import android.location.Location;
import android.widget.Toast;

import org.openplaces.lists.ListManager;
import org.openplaces.model.OPLocationInterface;
import org.openplaces.remote.OpenPlacesRemote;
import org.openplaces.search.ResultSet;

import java.util.List;

/**
 * Created by gabriele on 1/8/15.
 */
public class LoadLocationsAround extends OpenPlacesAsyncTask {

    private Context appContext;
    private Location point;

    public LoadLocationsAround(Location point, Context appContext, OpenPlacesAsyncTaskListener listener) {
        super(listener);
        this.appContext = appContext;
        this.point = point;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        OpenPlacesRemote opr = OpenPlacesRemote.getInstance(this.appContext);
        opr.updateKnownLocationsAround(this.point);

        //TODO: this returns all locations not only the ones around.
        List<OPLocationInterface> res = opr.getKnownLocations();
        this.setResult(res, 0);
        return null;
    }

}