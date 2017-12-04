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

import com.applied.thermal.types.ThermalCategory;
import com.applied.thermal.types.ThermalCategory.ThermalSortingField;
import com.applied.thermal.types.ThermalCategoryFactory;
import com.applied.thermal.types.ThermalCollection;
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
        KmlFolderCategory.exportThermalStyleToKml(doc);
        
        ThermalCollection collection = new ThermalCollection();
        File dataFolder = new File("testdata");
        for(File file : dataFolder.listFiles()) {
            System.out.println("Processing " + file.getName());
            if(file.getName().endsWith(".kml")) {
                OLCKmlRecord record = new OLCKmlRecord(file);
                record.getFlight().computeThermalFixes();
                for(Thermal thermal : record.getFlight().thermals) {
                    collection.add(thermal);
                }
            }
        }
        
        ThermalSortingField[] fields = { ThermalSortingField.ThermalStrength, ThermalSortingField.Month };
        collection.generateCategories(new ThermalCategoryFactory() {
            @Override
            public ThermalCategory createCategory(ThermalSortingField aField, String aTitle, ThermalCategory aParentCategory) {
                if(aParentCategory == null) {
                    return new KmlFolderCategory(aField, aParentCategory, aTitle, doc);
                } else {
                    return new KmlFolderCategory(aField, aParentCategory, aTitle, ((KmlFolderCategory)aParentCategory).folder);
                }
                
            }
        }, fields);

        try {
            outputKml.marshal(new File("thermalOutput.kml"));
        } catch(Exception e) {
            e.printStackTrace();
        }
        
        System.out.println("Complete.");
    }
}
