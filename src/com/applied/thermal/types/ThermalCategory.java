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
import java.util.ArrayList;
import java.util.HashMap;

/**
 * An abstract class that represents an organization structure for Thermals.
 * Implementing classes should add the underlying data structures necessary to
 * correlate the category with, for example, a KML folder.
 */
public abstract class ThermalCategory {
    
    /**
     * A field type used for categorization.
     */
    public enum ThermalSortingField {
        Year,               //! Sort by year.
        Month,              //! Sort by month (in textual form).
        Glider,             //! Sort by glider name.
        ThermalStrength,    //! Sort by thermal strength, bracketed.
        ThermalClimb        //! Sort by total distance climbed, bracketed.
    }
    
    public static final double STRENGTH_BRACKET_INTERVALS = .5; //! The intervals, in m/s, for thermal strength.
    public static final double CLIMB_BRACKET_INTERVALS = 200;   //! The intervals, in m, for climbs.

    public ThermalCategory(ThermalSortingField aField, ThermalCategory aParentCategory, String aTitle) {
        field = aField;
        title = aTitle;
        parentCategory = aParentCategory;
        thermals = new ArrayList<>();
        categories = new ArrayList<>();
    }

    /**
     * Called when a thermal is added to this category.
     * @param aThermal 
     */
    public void addThermal(Thermal aThermal) {
        thermals.add(aThermal);
    }

    /**
     * Called when a new sub-category is added to this category.
     * @param aCategory 
     */
    public void addCategory(ThermalCategory aCategory) {
        categories.add(aCategory);
    }

    /**
     * Recursively iterates through all sub-categories to determine the total
     * number of thermals contained by this category.
     * @return 
     */
    public int getTotalThermals() {
        int sum = 0;
        for(ThermalCategory cat : categories) {
            sum += cat.getTotalThermals();
        }
        return sum + thermals.size();
    }

    /**
     * Called when all thermals have been categorized. The category should finish
     * initialization at this point.
     */
    public abstract void finish();

    protected ThermalSortingField field;
    protected ThermalCategory parentCategory;
    protected String title;
    protected ArrayList<Thermal> thermals;
    protected ArrayList<ThermalCategory> categories;
}