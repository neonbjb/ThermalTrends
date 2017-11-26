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
import com.javadocmd.simplelatlng.LatLngTool;
import com.javadocmd.simplelatlng.util.LengthUnit;
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
    static final String THERMAL_TRAJ_STYLE_NAME = "style_thermal_traj_";
    public static void exportThermalStyleToKml(Document kmlDoc) {
        // Color thermals by month.
        for(int i = 0; i < 12; i++) {
            Style style = kmlDoc.createAndAddStyle();
            style.withId(THERMAL_STYLE_NAME + i);
            int color = 0xffffff / 12 * i;
            style.createAndSetLineStyle().withColor("ff" + Integer.toHexString(color)).withWidth(2);
            style.createAndSetPolyStyle().withColor("7fffffff");
            
            // styles for thermal trajectories
            style = kmlDoc.createAndAddStyle();
            style.withId(THERMAL_TRAJ_STYLE_NAME + i);
            style.createAndSetLineStyle().withColor("ff" + Integer.toHexString(color)).withWidth(4);
            style.createAndSetPolyStyle().withColor("7fffffff");
        }
    }
    
    public void exportToKml(Folder kmlFolder, Folder trajFolder) {
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
        
        placemark = trajFolder.createAndAddPlacemark();
	// use the style for each continent
	placemark.withName(name + "_ThermalTrajectory")
	    .withStyleUrl("#" + THERMAL_TRAJ_STYLE_NAME + fixesInThermal.get(0).time.getMonth());
        placemark.withVisibility(false);
        line = placemark.createAndSetLineString();
        line.setAltitudeMode(AltitudeMode.ABSOLUTE);
        LatLng bottom = getGroundPosition();
        line.addToCoordinates(bottom.getLongitude(), bottom.getLatitude(), 0);
        LatLng top = getHighestPoint();
        line.addToCoordinates(top.getLongitude(), top.getLatitude(), fixesInThermal.get(fixesInThermal.size()-1).alt);
    }
    
    static class ShiftVector {
        public double magnitude; //! Magnitude of shift in meters(lateral)/meters(altitude)
        public double heading; //! Velocity heading in degrees.
        
        public LatLng extrapolateFixTo(FlightFix fix, double altitudeTo) {
            double altitudeDiff = altitudeTo - fix.alt;
            double metersLateral = magnitude * altitudeDiff;
            double head = heading;
            if(metersLateral < 0) {
                metersLateral = -metersLateral;
                head = (heading + 180) % 360;
            }
            return LatLngTool.travel(fix.pos, head, metersLateral, LengthUnit.METER);
        }
    }
    
    /**
     * Generates an average shift vector for the entire thermal. This is currently
     * crudely calculated by getting the lateral distance from the top fix and bottom
     * fix and dividing out the altitude change.
     * @return 
     */
    public ShiftVector getAverageShift() {
        ShiftVector ret = new ShiftVector();
        double distanceShift = LatLngTool.distance(fixesInThermal.get(fixesInThermal.size() - 1).pos, fixesInThermal.get(0).pos, LengthUnit.METER);
        double altShift = fixesInThermal.get(fixesInThermal.size() - 1).alt - fixesInThermal.get(0).alt;
        
        ret.heading = LatLngTool.initialBearing(fixesInThermal.get(0).pos, fixesInThermal.get(fixesInThermal.size() - 1).pos);
        ret.magnitude = distanceShift / altShift;
        
        return ret;
    }
    
    /**
     * Generates a linear plot of lateral shifting for the thermal across the
     * ground and returns the intercept point using that average velocity, starting
     * at the base of lift and extrapolated to the top of lift.
     * @return 
     */
    public LatLng getHighestPoint() {
        return getAverageShift().extrapolateFixTo(fixesInThermal.get(0), fixesInThermal.get(fixesInThermal.size()-1).alt);
    }
    
    /**
     * Generates a linear plot of lateral shifting for the thermal across the
     * ground and returns the intercept point using that average velocity, starting
     * at the base of lift and extrapolated to 0 MSL.
     * @return 
     */
    public LatLng getGroundPosition() {
        return getAverageShift().extrapolateFixTo(fixesInThermal.get(0), 0);
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
