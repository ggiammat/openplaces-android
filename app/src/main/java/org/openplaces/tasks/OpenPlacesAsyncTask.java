package org.openplaces.tasks;

import android.os.AsyncTask;
import android.support.annotation.NonNull;

import org.openplaces.search.ResultSet;

/**
 * Created by gabriele on 1/7/15.
 */
public abstract class OpenPlacesAsyncTask extends AsyncTask<Void, Void, Void> {

    public interface OpenPlacesAsyncTaskListener {
        public void taskStarted();
        public void taskFinished(Object result, int status);
    }

    private OpenPlacesAsyncTaskListener listener;

    public Object getResult() {
        return result;
    }

    public int getTaskStatus() {
        return status;
    }

    private Object result;
    private int status = -100;

    public OpenPlacesAsyncTask(OpenPlacesAsyncTaskListener listener){
        this.listener = listener;
    }

    protected void setResult(Object result, int status){
        this.result = result;
        this.status = status;
    }

    @Override
    protected void onPreExecute() {
        this.taskOnPreExecute();
        this.listener.taskStarted();
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        this.taskOnPostExecute();
        this.listener.taskFinished(this.result, this.status);
    }

    public void taskOnPreExecute(){

    }
    public void taskOnPostExecute(){

    }

}
