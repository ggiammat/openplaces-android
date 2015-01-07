package org.openplaces.search;

import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.PopupWindow;

import org.openplaces.MapActivity;
import org.openplaces.R;
import org.openplaces.model.OPLocationInterface;
import org.openplaces.model.OPPlaceInterface;
import org.openplaces.places.Place;
import org.openplaces.search.suggestions.SuggestionItem;
import org.openplaces.widgets.OPChipsEditText;

import java.util.List;


public class SearchSuggestionsPopup {

    private OPChipsEditText searchET;
    private Context context;
    private PopupWindow popup;

    private SearchController searchController;
    private SearchSuggestionsAdapter suggestionsAdapter;

    public SearchSuggestionsPopup(Context context, SearchController searchController){
        this.searchController = searchController;
        this.searchET = searchController.getQueryET();
        this.context = context;

        this.suggestionsAdapter = new SearchSuggestionsAdapter(context, this.searchController);

        this.searchET.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(popup == null || !popup.isShowing()){
                    show();
                }
            }
        });

        this.searchET.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if(hasFocus && (popup == null || !popup.isShowing())){
                    show();
                }
            }
        });

        this.searchET.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
               if(keyCode == KeyEvent.KEYCODE_BACK && popup.isShowing()){
                   popup.dismiss();
                   return true;
               }

               return false;
            }
        });


        this.searchController.addListener(new SearchController.SearchQueryListener() {
            @Override
            public void freeTextQueryChanged(String freeTextQuery) {
                suggestionsAdapter.getFilter().filter(freeTextQuery);
            }

            @Override
            public void searchStarted(SearchQuery sq) {
                popup.dismiss();
            }

            @Override
            public void searchEnded(SearchQuery sq, ResultSet rs) {

            }

            @Override
            public void newLocationsAvailable(List<OPLocationInterface> locs) {

            }

            @Override
            public void newPlacesAvailable(List<Place> places) {

            }

        });

    }


    private void show(){
        LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.popup_search_suggestions, null);


        popup = new PopupWindow(layout, 300, 470, true);
        popup.setOutsideTouchable(true);
        popup.setTouchable(true);
        popup.setWidth(WindowManager.LayoutParams.FILL_PARENT);

        popup.setFocusable(false);
        popup.showAsDropDown(this.searchET);
        popup.setInputMethodMode(PopupWindow.INPUT_METHOD_NOT_NEEDED);
        popup.showAtLocation(layout, Gravity.CENTER, 0, 0);


        final ListView tv = (ListView) layout.findViewById(R.id.suggestionsList);

        tv.setAdapter(this.suggestionsAdapter);
        this.searchController.updateAroundLocations();
        this.searchController.updateStarredPlaces();

        tv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                SuggestionItem item = (SuggestionItem) suggestionsAdapter.getItem(i);
                Log.d(MapActivity.LOGTAG, "clicked: " + item);
                item.onItemClicked();
            }
        });
    }

}
