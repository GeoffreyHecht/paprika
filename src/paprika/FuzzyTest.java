/*
 * Paprika - Detection of code smells in Android application
 *     Copyright (C)  2016  Geoffrey Hecht - INRIA - UQAM - University of Lille
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as
 *     published by the Free Software Foundation, either version 3 of the
 *     License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package paprika;

import net.sourceforge.jFuzzyLogic.FIS;
import net.sourceforge.jFuzzyLogic.FunctionBlock;
import net.sourceforge.jFuzzyLogic.plot.JFuzzyChart;

/**
 * Created by Geoffrey Hecht on 8/5/15.
 */
public class FuzzyTest {
    public static void main(String[] args) {
        // Load from 'FCL' file
        String fileName = "fcl/Blob.fcl";
        FIS fis = FIS.load(fileName, true);

        if (fis == null) {
            System.err.println("Can't load file: '" + fileName + "'");
            System.exit(1);
        }

        // Get default function block
        FunctionBlock fb = fis.getFunctionBlock(null);
        JFuzzyChart.get().chart(fb);
        int [] lcom = {26,27,27,28,60,320,26,39}; // 25,40
        int [] nom = {17,17,18,19,17,21,27,22}; // 14.5,22
        int [] noa = {9,9,10,10,17,10,13,13}; // 8.5,13
        JFuzzyChart.get().chart(fb);
        for (int i = 0; i< lcom.length;i++){
            fb.setVariable("lack_of_cohesion_in_methods", lcom[i]);
            fb.setVariable("number_of_methods", nom[i]);
            fb.setVariable("number_of_attributes", noa[i]);
            fb.evaluate();
            JFuzzyChart.get().chart(fb.getVariable("res"),fb.getVariable("res").getDefuzzifier(),true);
            System.out.println("Res ("+lcom[i]+","+nom[i]+","+noa[i]+"): " + fb.getVariable("res").getValue());
        }
    }
}
