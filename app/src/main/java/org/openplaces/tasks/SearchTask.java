package org.openplaces.tasks;

import android.content.Context;
import android.widget.Toast;

import org.openplaces.lists.ListManager;
import org.openplaces.remote.OpenPlacesRemote;
import org.openplaces.search.ResultSet;
import org.openplaces.search.SearchQuery;

/**
 * Created by gabriele on 1/8/15.
 */
public class SearchTask extends OpenPlacesAsyncTask {

    private Context appContext;
    private SearchQuery sq;

    public SearchTask(SearchQuery sq, Context appContext, OpenPlacesAsyncTaskListener listener) {
        super(listener);
        this.appContext = appContext;
        this.sq = sq;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        OpenPlacesRemote opr = OpenPlacesRemote.getInstance(this.appContext);
        ResultSet resultSet = opr.search(this.sq);

        this.setResult(resultSet,  resultSet.getStat("errorCode").equals("0") ? 0 : 1);
        return null;
    }

    @Override
    public void taskOnPostExecute() {
        if(this.getTaskStatus() == 0){
//            Toast toast = Toast.makeText(this.appContext, "Starred places successfully loaded", Toast.LENGTH_SHORT);
//            toast.show();
        }
        else {
            Toast toast = Toast.makeText(this.appContext, "ERROR loading starred places: " + ((ResultSet) this.getResult()).getStat("errorMessage"), Toast.LENGTH_SHORT);
            toast.show();
        }
    }
}