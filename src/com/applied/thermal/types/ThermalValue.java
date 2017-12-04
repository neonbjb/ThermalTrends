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
import static com.applied.thermal.types.ThermalCategory.CLIMB_BRACKET_INTERVALS;
import static com.applied.thermal.types.ThermalCategory.STRENGTH_BRACKET_INTERVALS;
import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * A type which holds:
 * 1) A human-readable string which describes a value from a thermal.
 * 2) A way to compare that value with the same value from another thermal.
 */
public class ThermalValue implements Comparable {
    public String title;
    public double numericValue;
    public double interval = 1.;
    public boolean isString = false;
    
    private static Calendar calendar = new GregorianCalendar();
    private static DateFormatSymbols dateFormatSymbols = new DateFormatSymbols();

    /**
     * Constructor to use for values which have a numeric value tied to a string
     * (for instance - months of the year or days of the week).
     * @param aTitle
     * @param aVal 
     */
    public ThermalValue(String aTitle, double aVal) {
        title = aTitle;
    }

    /**
     * Constructor to use for values which are simple numeric values with unity precision.
     * For example, years.
     * @param aVal 
     */
    public ThermalValue(double aVal) {
        title = Double.toString(aVal);
        numericValue = aVal;
    }
    
    /**
     * Constructo to use for values which have a non-unity precision. For example, values
     * which need to be sorted by intervals of ".5".
     * @param aVal
     * @param aInterval 
     */
    public ThermalValue(double aVal, double aInterval) {
        numericValue = bracketRound(aVal, aInterval);
        interval = aInterval;
        title = Double.toString(numericValue);
    }
    
    /**
     * Constructor to use for values which are only strings and have no numeric
     * values attached.
     * @param aVal 
     */
    public ThermalValue(String aVal) {
        title = aVal;
        isString = true;
    }
    
    /**
     * Retrieves a value from a Thermal object for a given field.
     * @param aThermal
     * @param aField
     * @return 
     */
    public static ThermalValue getFieldValue(Thermal aThermal, ThermalCategory.ThermalSortingField aField) {
        calendar.setTime(aThermal.getFirstFix().time);
        switch(aField) {
            case Year:
                return new ThermalValue(calendar.get(Calendar.YEAR));
            case Month:
                int month = calendar.get(Calendar.MONTH);
                return new ThermalValue(dateFormatSymbols.getMonths()[month], month);
            case Glider:
                return new ThermalValue(aThermal.getFlight().getAirplane());
            case ThermalStrength:
                return new ThermalValue(aThermal.getAverageClimbRate(), STRENGTH_BRACKET_INTERVALS);
            case ThermalClimb:
                return new ThermalValue(aThermal.getTotalClimb(), CLIMB_BRACKET_INTERVALS);
        }
        return null;
    }
    
    protected static double bracketRound(double aToRound, double aBrackets) {
        double roundNumber = aToRound / aBrackets;
        roundNumber = Math.round(roundNumber);
        return roundNumber * aBrackets;
    }

    @Override
    public int compareTo(Object o) {
        ThermalValue tv2 = (ThermalValue)o;
        if(isString) {
            return (title.compareTo(tv2.title));
        } else {
            return ((new Double(numericValue)).compareTo(tv2.numericValue));
        }
    }
}
