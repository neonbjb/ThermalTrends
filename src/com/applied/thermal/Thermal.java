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

import com.javadocmd.simplelatlng.LatLng;
import java.util.ArrayList;

import de.micromata.opengis.kml.v_2_2_0.*;

/**
 *
 * @author James Betker
 */
public class Thermal {
    
    public Thermal(Flight aFlight) {
        flight = aFlight;
        fixesInThermal = new ArrayList<>(100);
    }
    
    public Thermal(Flight aFlight, ArrayList<FlightFix> fixes) {
        flight = aFlight;
        fixesInThermal = fixes;
    }
    
    public void addFix(FlightFix fix) { 
        computed = false;
        fixesInThermal.add(fix);
    }
    
    public void compute() {
        if(fixesInThermal.isEmpty()) return;
        
        FlightFix firstFix = fixesInThermal.get(0);
        name = firstFix.toString();
        
        // Fetch min and max heights
        FlightFix minHeightFix = null;
        FlightFix maxHeightFix = null;
        for(int i = 0; i < fixesInThermal.size(); i++) {
            FlightFix fix = fixesInThermal.get(i);
            if(minHeightFix == null || fix.alt < minHeightFix.alt) {
                minHeightFix = fix;
            }
            if(maxHeightFix == null || fix.alt > maxHeightFix.alt) {
                maxHeightFix = fix;
            }
        }
        minHeight = minHeightFix.alt;
        maxHeight = maxHeightFix.alt;
        avgClimbRate = (maxHeight - minHeight) / ((double)(maxHeightFix.time.getTime() - minHeightFix.time.getTime()) / 1000.);
        
        computed = true;
    }
    
    static final String THERMAL_STYLE_NAME = "style_thermal_";
    public static void exportThermalStyleToKml(Document kmlDoc) {
        // Color thermals by month.
        for(int i = 0; i < 12; i++) {
            Style style = kmlDoc.createAndAddStyle();
            style.withId(THERMAL_STYLE_NAME + i);
            int color = 0xffffff / 12 * i;
            style.createAndSetLineStyle().withColor("ff" + Integer.toHexString(color)).withWidth(2);
            style.createAndSetPolyStyle().withColor("7fffffff");
        }
    }
    
    public void exportToKml(Folder kmlFolder) {
        if(!computed) {
            compute();
        }
        
	Placemark placemark = kmlFolder.createAndAddPlacemark();
	// use the style for each continent
	placemark.withName(name)
	    .withStyleUrl("#" + THERMAL_STYLE_NAME + fixesInThermal.get(0).time.getMonth())
            .withDescription("Date: " + fixesInThermal.get(0).time.toLocaleString() + 
                             "\nPilot: " + flight.pilot + 
                             "\nGlider: " + flight.airplane + 
                             "\nMin Height(m): " + minHeight + 
                             "\nMax Height(m): " + maxHeight +
                             "\nAverage Climb Rate (m/s): " + avgClimbRate);
        LineString line = placemark.createAndSetLineString();
        line.setAltitudeMode(AltitudeMode.ABSOLUTE);
        for(FlightFix fix : fixesInThermal) {
            line.addToCoordinates(fix.pos.getLongitude(), fix.pos.getLatitude(), fix.alt);
        }
    }
    
    Flight flight;
    ArrayList<FlightFix> fixesInThermal;
    
    // Computed values.
    boolean computed = false;
    String name;
    double minHeight;
    double maxHeight;
    double maxClimbRate300Meters;
    double avgClimbRate;
    
}
