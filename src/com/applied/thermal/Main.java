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

import java.io.File;
import de.micromata.opengis.kml.v_2_2_0.*;

/**
 *
 * @author James Betker
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("Starting up..");
        final Kml outputKml = new Kml();
        Document doc = outputKml.createAndSetDocument().withName("Thermals").withOpen(true);
        Thermal.exportThermalStyleToKml(doc);
        Folder thermalFolder = doc.createAndAddFolder();
        thermalFolder.withName("Thermals").withOpen(true);
        
        File dataFolder = new File("data");
        for(File file : dataFolder.listFiles()) {
            System.out.println("Processing " + file.getName());
            if(file.getName().endsWith(".kml")) {
                OLCKmlRecord record = new OLCKmlRecord(file);
                record.getFlight().computeThermalFixes();
                for(Thermal thermal : record.getFlight().thermals) {
                    thermal.exportToKml(thermalFolder);
                }
            }
        }

        try {
            outputKml.marshal(new File("thermalOutput.kml"));
        } catch(Exception e) {
            e.printStackTrace();
        }
        
        System.out.println("Complete.");
    }
}
