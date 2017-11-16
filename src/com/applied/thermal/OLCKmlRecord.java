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
import de.micromata.opengis.kml.v_2_2_0.Coordinate;
import de.micromata.opengis.kml.v_2_2_0.Document;
import de.micromata.opengis.kml.v_2_2_0.Feature;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import de.micromata.opengis.kml.v_2_2_0.LineString;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import de.micromata.opengis.kml.v_2_2_0.Folder;
import de.micromata.opengis.kml.v_2_2_0.TimeStamp;
import de.micromata.opengis.kml.v_2_2_0.Point;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 *
 * @author James Betker
 */
public class OLCKmlRecord {
    
    public OLCKmlRecord(File aKml) {
        Date date;
        String pilot, airplane;
        Kml kml = Kml.unmarshal(aKml);
        Document doc = (Document)kml.getFeature();
        
        // Attempt to process data out of the description.
        String dateString = extractField(doc.getDescription(), "date:", "&nbsp;");
        try {
            date = (new SimpleDateFormat("dd/MM/yyyy")).parse(dateString);
        } catch(Exception e) {
            System.out.println("Error processing date for file " + aKml.getAbsolutePath());
            e.printStackTrace();
            date = new Date(0);
        }
        pilot = extractField(doc.getDescription(), "pilot:", "&nbsp;");
        airplane = extractField(doc.getDescription(), "airplane:", "&nbsp;");
        System.out.println("Processed KML header data. Date: " + date.toString() + " Pilot: " + pilot + " Airplane: " + airplane);
        
        mFlight = new Flight(date, pilot, airplane);
        
        for(Feature f : doc.getFeature()) {
            // The '#polyline' placemark will generally contain the entire flight path.
            if(f.getStyleUrl() != null && f.getStyleUrl().equals("#polyline")) {
                Placemark p = (Placemark)f;
                LineString ls = (LineString)p.getGeometry();
                FlightFix fix;
                for(Coordinate c : ls.getCoordinates()) {
                    fix = new FlightFix();
                    fix.alt = c.getAltitude();
                    fix.pos = new LatLng(c.getLatitude(), c.getLongitude());
                    mFlight.addFix(fix);
                }
            }
            
            // The fixes folder will contain reference fixes which can be used to place timestamps on the flight path.
            // This will be utilized as follows:
            // 1) Scan the times of two adjacent fixes in the middle of the list.
            // 2) Match these two fixes with flight path points.
            // 3) Count the number of flight path points between the fixes.
            // 4) Repeat (1-3) with the next two fixes. Verify result matches.
            // 5) First flight path point gets the timestamp of the first fix. Adjacent flight path points get
            //    that timestamp + (diff between fixes)/(flight path points between fixes)
            if(f.getName() != null && f.getName().equals("fixes")) {
                Folder folder = (Folder)f;
                int startFix = folder.getFeature().size() / 2;
                int pathCountA = 0;
                while(startFix + 2 < folder.getFeature().size()) { // Breaks internally.
                    Placemark p1 = (Placemark)folder.getFeature().get(startFix);
                    Placemark p2 = (Placemark)folder.getFeature().get(startFix+1);
                    LatLng ll1 = placemarkToLatLng(p1);
                    LatLng ll2 = placemarkToLatLng(p2);
                    int count = getFlightPathCountBetweenFixes(ll1, ll2);
                    if(count != 0) {
                        if(pathCountA == 0) {
                            pathCountA = count;
                        } else {
                            if(count == pathCountA) {
                                // Success, we've found our count.
                                // A few assumptions made here: 
                                // (1) These placemarks have timestamps
                                // (2) The difference is less than 1 minute but more than 1 second.
                                // (3) Fixes were already initialized (e.g. this folder comes after the flight path)
                                long diffMs = (placemarkToDatetime(p2).getTime() - placemarkToDatetime(p1).getTime()) / count;
                                long firstFixTime = placemarkToDatetime((Placemark)folder.getFeature().get(0)).getTime();
                                for(int i = 0; i < mFlight.numFixes(); i++) {
                                    mFlight.fix(i).time = new Date(firstFixTime + diffMs * i);
                                }
                            }
                        }
                    }
                    startFix += 2;
                }
            }
        }
    }
    
    public final String extractField(String desc, String fieldStart, String fieldEnd) {
        String shortDesc = desc.toLowerCase();
        if(shortDesc.contains(fieldStart)) {
            int indexOfStart = shortDesc.indexOf(fieldStart);
            int indexOfBreak = -1;
            if(fieldEnd != null) indexOfBreak = shortDesc.indexOf(fieldEnd, indexOfStart);
            if(indexOfBreak != -1) {
                return desc.substring(indexOfStart + fieldStart.length(), indexOfBreak).trim();
            } else {
                return desc.substring(indexOfStart + fieldStart.length()).trim();
            }
        }
        return "";
    }
    
    /**
     * Fetches the number of points between fixA and fixB.
     * @precond: fixA occurs before fixB.
     * @param fixA
     * @param fixB
     * @return Number of points, 0 if fixA or fixB are not found.
     */
    public final int getFlightPathCountBetweenFixes(LatLng fixA, LatLng fixB) {
        int fixAIndex = -1;
        for(int i = 0; i < mFlight.numFixes(); i++) { 
            if(fixAIndex == -1) {
                if(mFlight.fix(i).pos.equals(fixA)) {
                    fixAIndex = i;
                }
            } else {
                if(mFlight.fix(i).pos.equals(fixB)) {
                    return (i - fixAIndex);
                }
            }
        }
        return 0;
    }
    
    /**
     * Extracts coordinates from a placemark into a LatLng object. Fails if
     * placemark contains more than one set of coordinates (or no coordinates at
     * all).
     * @param p
     * @return 
     */
    public final LatLng placemarkToLatLng(Placemark p) {
        Point ls = (Point)p.getGeometry();
        if(ls.getCoordinates().size() != 1) return null;
        Coordinate c = ls.getCoordinates().get(0);
        return new LatLng(c.getLatitude(), c.getLongitude());
    }
    
    static final SimpleDateFormat placemarkTimeStampFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    static {
        placemarkTimeStampFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
    }
    
    public final Date placemarkToDatetime(Placemark p) {
        TimeStamp stamp = (TimeStamp)p.getTimePrimitive();
        if(stamp != null) {
            //Sample: 2011-04-22T21:51:00Z
            try{
                return placemarkTimeStampFormat.parse(stamp.getWhen());
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        return null;
    }
    
    public Flight getFlight() {
        return mFlight;
    }
    
    Flight mFlight;
}
