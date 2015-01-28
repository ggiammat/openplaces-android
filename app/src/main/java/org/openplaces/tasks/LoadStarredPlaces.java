package org.openplaces.tasks;

import android.content.Context;
import android.widget.Toast;

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

        this.setResult(rs, rs.getStat("errorCode").equals("0") ? 0 : 1);
        return null;
    }

    @Override
    public void taskOnPreExecute() {
        super.taskOnPreExecute();
    }

    @Override
    public void taskOnPostExecute() {
        if(this.getTaskStatus() == 0){
            Toast toast = Toast.makeText(this.appContext, "Lists succesfully loaded", Toast.LENGTH_SHORT);
            toast.show();
        }
        else {
            Toast toast = Toast.makeText(this.appContext, "ERROR loading lists: " + ((ResultSet) this.getResult()).getStat("errorMessage"), Toast.LENGTH_SHORT);
            toast.show();
        }
    }
}
