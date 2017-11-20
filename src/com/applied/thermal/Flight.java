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

import java.util.ArrayList;
import java.util.Date;

/**
 *
 * @author James Betker
 */
public class Flight {
    public Flight(Date aDate, String aPilot, String aAirplane) {
        date = aDate;
        pilot = aPilot;
        airplane = aAirplane;
        fixes = new ArrayList<>(1000);
    }
    
    public void addFix(FlightFix fix) {
        fixes.add(fix);
    }
    
    public FlightFix fix(int i) {
        return fixes.get(i);
    }
    
    public int numFixes() {
        return fixes.size();
    }
    
    int fixesPerMinute = -1;
    public int getFixesPerMinute() {
        // This only needs to be computed once for a flight.
        if(fixesPerMinute != -1) {
            return fixesPerMinute;
        }
        
        if(fixes.size() < 21) {
            System.err.println("getFixesPerMinute - Not enough fixes given.");
        }
        
        // Compute this by taking the average time distance between the mid-point fix and 10+ that.
        int midFix = fixes.size() / 2;
        Date start = fixes.get(midFix).time;
        Date end = fixes.get(midFix + 10).time;
        double gap = (double)(end.getTime() - start.getTime()) / 1000. / 60.;
        fixesPerMinute = (int)(10. / gap);
        return fixesPerMinute;
    }
    
    private boolean checkThermalRateRequirements(int startFixIndex) {
        FlightFix fixA = fixes.get(startFixIndex);
        FlightFix fixMinuteAfterA = fixes.get(startFixIndex + getFixesPerMinute());
        double interfixSpeed = fixMinuteAfterA.getSpeedSince(fixA);
        double altGap = fixMinuteAfterA.alt - fixA.alt;
        return ((interfixSpeed < Configuration.getConfig().MaxSpeedInThermal) &&
                (altGap > Configuration.getConfig().MinThermalClimbRate));
    }
    
    public void computeThermalFixes() {
        if(thermals != null) {
            thermals.clear();
        } else {
            thermals = new ArrayList<>();
        }
        
        ArrayList<FlightFix> thermalFixes = new ArrayList<>(20);
        for(int i = 0; i < (fixes.size() - getFixesPerMinute()); i++) {
            // A thermal is detected by finding a subset of contiguous fixes that meets these requirements:
            // 1) At least 1 minute long.
            // 2) A maximum speed for any segment of the subset as defined in Configuration.MaxSpeedInThermal which is never exceeded during a 1-minute period. 
            // 3) A climb of at least Configuration.MinThermalClimbDistance.
            // 4) A climb rate that never falls below Configuration.MinThermalClimbRate over a 1-minute period.
            // An assumption can be made that the periodicity of the fixes is regular - e.g. the algorithm should find how many fixes consist of a 1-minute period and
            // use that for the rest of the thermal finding procedure.
            
            // Check that conditions (2) and (4) apply for the fix 1 minute in advance of the current one.
            if(checkThermalRateRequirements(i)) {
                // We've satisfied everything but requirement 2. Keep searching forward from i until reqs 2 or 4 fail.
                int thermalStartIndex = i++;
                FlightFix startFix = fixes.get(thermalStartIndex);
                while(i < (fixes.size() - getFixesPerMinute()) && checkThermalRateRequirements(i)) {
                    i++;
                }
                if(i >= (fixes.size() - getFixesPerMinute())) {
                    break;
                }
                int thermalEndIndex = i + getFixesPerMinute();
                FlightFix endFix = fixes.get(thermalEndIndex);
                double altGain = endFix.alt - startFix.alt;
                if(altGain > Configuration.getConfig().MinThermalClimbDistance) {
                    // We've got a thermal!
                    Logger.log("Thermal found for " + pilot +  "-" + airplane + " starting at " + startFix.toString() + " ending at " + endFix.toString());
                    thermals.add(new Thermal(this, new ArrayList<>(fixes.subList(thermalStartIndex, thermalEndIndex))));
                }
            }
        }
    }
    
    Date date;
    String pilot;
    String airplane;    
    ArrayList<FlightFix> fixes;
    ArrayList<Thermal> thermals;
}




