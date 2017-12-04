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
import com.applied.thermal.types.ThermalValue;
import de.micromata.opengis.kml.v_2_2_0.Document;
import de.micromata.opengis.kml.v_2_2_0.Folder;
import de.micromata.opengis.kml.v_2_2_0.Style;

class KmlFolderCategory extends ThermalCategory {
    public Folder folder;
    public Folder trajFolder;
    
    static final String THERMAL_STYLE_NAME = "style_thermal_";
    static final String THERMAL_TRAJ_STYLE_NAME = "style_thermal_traj_";
    public static void exportThermalStyleToKml(Document kmlDoc) {
        // Color thermals by their categorization.
        for(int i = 0; i < 12; i++) {
            Style style = kmlDoc.createAndAddStyle();
            style.withId(THERMAL_STYLE_NAME + i);
            int color = 0xffffff / 12 * i;
            style.createAndSetLineStyle().withColor(colorString(color)).withWidth(2);
            style.createAndSetPolyStyle().withColor("7fffffff");
            
            // styles for thermal trajectories
            style = kmlDoc.createAndAddStyle();
            style.withId(THERMAL_TRAJ_STYLE_NAME + i);
            style.createAndSetLineStyle().withColor(colorString(color)).withWidth(4);
            style.createAndSetPolyStyle().withColor("7fffffff");
        }
    }
    
    private static String colorString(int aColor) {
        String color = Integer.toHexString(aColor);
        while(color.length() < 6) {
            color = "0" + color;
        }
        return "ff" + color;
    }

    public KmlFolderCategory(ThermalCategory.ThermalSortingField aField, ThermalCategory aParentCategory, String aTitle, Document aKmlDoc) {
        super(aField, aParentCategory, aTitle);
        folder = aKmlDoc.createAndAddFolder();
        initialize();
    }

    public KmlFolderCategory(ThermalCategory.ThermalSortingField aField, ThermalCategory aParentCategory, String aTitle, Folder aFolder) {
        super(aField, aParentCategory, aTitle);
        folder = aFolder.createAndAddFolder();
        initialize();
    }
    
    private void initialize() {
        trajFolder = folder.createAndAddFolder();
        trajFolder.withName("Thermal Trajectories");
        
        if(field == Configuration.getConfig().ColorByField) {
            
        }
    }
    
    protected int getStyleNumber(Thermal aThermal) {
        KmlFolderCategory styleCat = null;
        KmlFolderCategory curCat = this;
        while(styleCat == null && curCat != null) {
            if(Configuration.getConfig().ColorByField == curCat.field) {
                styleCat = curCat;
            }
            curCat = (KmlFolderCategory)curCat.parentCategory;
        }
        
        if(styleCat == null) return 0;
        ThermalValue val = ThermalValue.getFieldValue(aThermal, styleCat.field);
        if(val.isString) {
            // Strings cannot have styles (yet).
            return 0;
        } else {
            long valAsLong = (long)(val.numericValue / val.interval);
            return (int)(valAsLong % (long)Configuration.getConfig().NumberThermalStyles);
        }
    }
    
    protected String getStyle(Thermal aThermal) {
        return THERMAL_STYLE_NAME + getStyleNumber(aThermal);
    }
    
    protected String getTrajStyle(Thermal aThermal) {
        return THERMAL_STYLE_NAME + getStyleNumber(aThermal);
    }

    @Override
    public void addThermal(Thermal aThermal) {
        super.addThermal(aThermal);

        aThermal.exportToKml(folder, trajFolder, getStyle(aThermal), getTrajStyle(aThermal));
    }

    @Override
    public void finish() {
        folder.withName(title + " (" + getTotalThermals() + ")").withOpen(false);
    }
}
