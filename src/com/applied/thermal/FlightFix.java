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
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author James Betker
 */
public class FlightFix {
    public LatLng pos;
    public double alt;
    public Date time;
    
    /**
     * Computes a speed by determining the distance between the two fixes and
     * dividing out the time it took to traverse that distance.
     * @param otherFix
     * @return Speed in meters / second.
     */
    public double getSpeedSince(FlightFix otherFix) {
        return LatLngTool.distance(pos, otherFix.pos, LengthUnit.METER) / ((double)(time.getTime() - otherFix.time.getTime()) / 1000.);
    }
    
    static SimpleDateFormat format = new SimpleDateFormat("H:mm:ss");
    
    public String toString() { 
        return format.format(time);
    }
}