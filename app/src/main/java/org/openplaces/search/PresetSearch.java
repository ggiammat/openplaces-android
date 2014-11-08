package org.openplaces.search;

import org.openplaces.model.OPTagsFilter;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by gabriele on 11/7/14.
 */
public class PresetSearch {

    private OPTagsFilter filters;
    private String name;
    private Set<String> otherNames;

    public PresetSearch(String name, OPTagsFilter filters){
        this.name = name;
        this.filters = filters;
        this.otherNames = new HashSet<String>();
        this.otherNames.add(this.name);
    }

    public String getName(){
        return this.name;
    }

    public void addOtherName(String name){
        this.otherNames.add(name);
    }

    public String getFirstMatchingName(String text){
        for(String name: this.otherNames){
            if(name.toLowerCase().contains(text.toLowerCase())){
                return name;
            }
        }
        return null;
    }

}
