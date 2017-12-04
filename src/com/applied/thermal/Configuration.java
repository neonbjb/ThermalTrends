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

import com.applied.thermal.Units.UnitSystem;
import com.applied.thermal.types.ThermalCategory.ThermalSortingField;

/**
 * Defines configuration constants which determine how this application works.
 * All distances are in meters.
 * All speeds are in meters/sec.
 * @author James Betker
 */
public class Configuration {
    public double MaxSpeedInThermal = 17;
    public double MinThermalClimbDistance = 300;
    public double MinThermalClimbRate = 2; // 2 m/s corresponds to approximately 400fpm
    public UnitSystem CurrentUnitSystem = UnitSystem.Feet;
    public ThermalSortingField ColorByField = ThermalSortingField.ThermalStrength;
    public int NumberThermalStyles = 12;
    
    static Configuration instance;
    public static Configuration getConfig() {
        if(instance == null) {
            instance = new Configuration();
        }
        return instance;
    }
}
