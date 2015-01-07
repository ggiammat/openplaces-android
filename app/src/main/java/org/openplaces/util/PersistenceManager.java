package org.openplaces.util;

import android.os.Environment;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import org.openplaces.MapActivity;
import org.openplaces.model.OPPlaceInterface;
import org.openplaces.model.impl.OPPlaceImpl;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.lang.reflect.Type;

/**
 * Created by gabriele on 11/27/14.
 */
public class PersistenceManager {

    public static class OPPlaceInstanceDeserializer implements JsonDeserializer<OPPlaceInterface> {

        @Override
        public OPPlaceInterface deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            return jsonDeserializationContext.deserialize(jsonElement, OPPlaceImpl.class);
        }
    }


    public static Object gsonDeSerializer(String filename, Type type){
        if(!isExternalStorageReadable()){
            Log.w(MapActivity.LOGTAG, "External Storage not available for reading. Impossible to load data");
            return null;
        }

        File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "open-places");


        try {
            File f = new File(dir, filename);
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.registerTypeAdapter(OPPlaceInterface.class, new OPPlaceInstanceDeserializer());
            Reader fr = new FileReader(f);

            Object obj = gsonBuilder.create().fromJson(fr, type);
            fr.close();


            Log.d(MapActivity.LOGTAG, "Data loaded from " + f.getAbsolutePath());

            return obj;
        }
        catch (Exception e) {
            e.printStackTrace();
            Log.w(MapActivity.LOGTAG, "Loading of data failed: " + e);
            return null;
        }
    }

    public static boolean gsonSerializer(String filename, Object obj){
        if(!isExternalStorageWritable()){
            Log.w(MapActivity.LOGTAG, "External Storage not available for writing. Impossible to execute the write operation");
            return false;
        }

        File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "open-places");
        if(!dir.exists()){
            dir.mkdirs();
        }
        File f = new File(dir, filename);

        GsonBuilder gsonBuilder = new GsonBuilder();
        //gsonBuilder.registerTypeAdapter(OPPlaceInterface.class, new OPPlaceInstanceDeserializer());
        String serialization = gsonBuilder.create().toJson(obj);

        try {
            if(!f.exists()){
                f.createNewFile();
            }
            PrintWriter out = new PrintWriter(f);
            out.print(serialization);
            out.close();
            Log.d(MapActivity.LOGTAG, "data stored to " + f.getAbsolutePath());
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            Log.w(MapActivity.LOGTAG, "Failed to store data to " + f.getAbsolutePath() + ": " + e.getMessage());
            return false;
        }
    }

    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    public static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }
}
