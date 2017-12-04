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

import com.applied.thermal.FlightFix;
import com.javadocmd.simplelatlng.LatLng;
import com.javadocmd.simplelatlng.LatLngTool;
import com.javadocmd.simplelatlng.util.LengthUnit;

public class DriftGradientVector {
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