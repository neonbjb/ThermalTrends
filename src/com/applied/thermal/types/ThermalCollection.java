/*
 * Copyright 2017 James Betker.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.applied.thermal.types;

import com.applied.thermal.Thermal;
import com.applied.thermal.types.ThermalCategory.ThermalSortingField;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * This class contains an amount of thermals to be processed. It then allows the user
 * to sort these thermals by several fields. These fields are ordered by a list of priorities.
 * @author James Betker
 */
public class ThermalCollection implements Comparator<Thermal>{    
    
    public ThermalCollection() {
        thermals = new ArrayList<Thermal>();
    }
    
    /**
     * Add a thermal to the collection.
     * @param aThermal 
     */
    public void add(Thermal aThermal) {
        thermals.add(aThermal);
    }
    
    /**
     * Recursive function which calls finish() on all categories in the specified list.
     * @param aCats 
     */
    private void finishCategories(List<ThermalCategory> aCats) {
        for(ThermalCategory cat : aCats) {
            cat.finish();
            finishCategories(cat.categories);
        }
    }
    
    /**
     * Generates a hierarchy of categories into which Thermals are inserted. These 
     * categories are determined by aSortingFields.
     * 
     * This function must only be called after all thermals have been added to this
     * Collection.
     * @param creator
     * @param aSortingFields
     * @return 
     */
    public ArrayList<ThermalCategory> generateCategories(ThermalCategoryFactory creator, ThermalSortingField[] aSortingFields) {
        sortingFields = aSortingFields;
        Collections.sort(thermals, this);
        
        ArrayList<ThermalCategory> ret = new ArrayList<>();
        ThermalCategory[] categoryFields = new ThermalCategory[sortingFields.length];
        for(Thermal thermal : thermals) {
            // Determine if new categories need to be generated to support this thermal and do so if necessary.
            for(int i = 0; i < categoryFields.length; i++) {
                final ThermalValue fieldValue = ThermalValue.getFieldValue(thermal, sortingFields[i]);
                if(categoryFields[i] == null || !categoryFields[i].title.equals(fieldValue.title)) {
                    ThermalCategory newCat = creator.createCategory(aSortingFields[i], fieldValue.title, (i == 0) ? null : categoryFields[i-1]);
                    categoryFields[i] = newCat;
                    if(i == 0) {
                        ret.add(newCat);
                    } else {
                        categoryFields[i-1].addCategory(newCat);
                    }
                }
            }
            categoryFields[categoryFields.length-1].addThermal(thermal);
        }
        
        finishCategories(ret);
        
        return ret;
    }

    /**
     * Utilized to sort a list of thermals.
     * @param aThermal1
     * @param aThermal2
     * @return 
     */
    @Override
    public int compare(Thermal aThermal1, Thermal aThermal2) {
        for(ThermalSortingField field : sortingFields) {
            int compare = ThermalValue.getFieldValue(aThermal1, field).compareTo(ThermalValue.getFieldValue(aThermal2, field));
            if(compare == 0) {
                continue;
            } else {
                return compare;
            }
        }
        // If all else fails, fall back to a date comparison.
        return aThermal1.getFirstFix().time.compareTo(aThermal2.getFirstFix().time);
    }
    
    List<Thermal> thermals;
    ThermalSortingField[] sortingFields;
}
