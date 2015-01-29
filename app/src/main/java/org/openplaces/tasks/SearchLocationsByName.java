package org.openplaces.tasks;

import android.content.Context;
import android.location.Location;
import android.widget.Toast;

import org.openplaces.model.OPLocationInterface;
import org.openplaces.remote.OpenPlacesRemote;
import org.openplaces.search.LocationResultSet;
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

        LocationResultSet res = opr.getLocationsByName(this.name);
        this.setResult(res, res.getStats().get("errorCode").equals("0") ? 0 : 1);
        return null;
    }

    @Override
    public void taskOnPostExecute() {
        if(this.getTaskStatus() == 0){
//            Toast toast = Toast.makeText(this.appContext, "Locations successfully loaded", Toast.LENGTH_SHORT);
//            toast.show();
        }
        else {
            Toast toast = Toast.makeText(this.appContext, "ERROR searching location by name: " + ((LocationResultSet) this.getResult()).getStats().get("errorMessage"), Toast.LENGTH_SHORT);
            toast.show();
        }
    }
}