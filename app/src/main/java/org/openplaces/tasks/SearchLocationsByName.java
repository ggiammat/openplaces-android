package org.openplaces.tasks;

import android.content.Context;
import android.location.Location;
import android.widget.Toast;

import org.openplaces.model.OPLocationInterface;
import org.openplaces.remote.OpenPlacesRemote;
import org.openplaces.search.ResultSet;

import java.util.List;

/**
 * Created by gabriele on 1/8/15.
 */
public class SearchLocationsByName extends OpenPlacesAsyncTask {

    private Context appContext;
    private String name;

    public SearchLocationsByName(String name, Context appContext, OpenPlacesAsyncTaskListener listener) {
        super(listener);
        this.appContext = appContext;
        this.name = name;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        OpenPlacesRemote opr = OpenPlacesRemote.getInstance(this.appContext);

        List<OPLocationInterface> res = opr.getLocationsByName(this.name);
        this.setResult(res, 0);
        return null;
    }
}