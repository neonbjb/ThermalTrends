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
package com.applied.thermal;

import java.text.NumberFormat;

/**
 *
 * @author James Betker
 */
public class Units {
    public enum UnitSystem {
        Metric,  //! Corresponds to meters, m/s vertical and KPH
        Feet,    //! Corresponds to feet, feet per minute and MPH
        Knots    //! Corresponds to feet, knots vertical and knots horizontal
    }
    
    static NumberFormat numberFormat = NumberFormat.getInstance();
    static{
        numberFormat.setMaximumFractionDigits(2);
    }
    
    public static String climb(double aRateOfClimb) {
        switch(Configuration.getConfig().CurrentUnitSystem) {
            case Metric:
                numberFormat.format(aRateOfClimb);
            case Feet:
                numberFormat.format(aRateOfClimb * 196.85);
            case Knots:
                numberFormat.format(aRateOfClimb * 1.9438406064);
        }
        return "";
    }
    
    public static String height(double aHeight) {
        switch(Configuration.getConfig().CurrentUnitSystem) {
            case Metric:
                numberFormat.format(aHeight);
            case Feet:
                numberFormat.format(aHeight * 3.28084);
            case Knots:
                numberFormat.format(aHeight * 3.28084);
        }
        return "";
    }
    
    public static String speed(double aSpeed) {
        switch(Configuration.getConfig().CurrentUnitSystem) {
            case Metric:
                numberFormat.format(aSpeed * 3.6000059687997);
            case Feet:
                numberFormat.format(aSpeed * 2.23694);
            case Knots:
                numberFormat.format(aSpeed * 1.9438406064);
        }
        return "";
    }
}
