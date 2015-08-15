package paprika;

import net.sourceforge.jFuzzyLogic.FIS;
import net.sourceforge.jFuzzyLogic.FunctionBlock;

/**
 * Created by Geoffrey Hecht on 8/5/15.
 */
public class FuzzyTest {
    public static void test(String[] args) {
        // Load from 'FCL' file
        String fileName = "fcl/Blob.fcl";
        FIS fis = FIS.load(fileName, true);

        if (fis == null) {
            System.err.println("Can't load file: '" + fileName + "'");
            System.exit(1);
        }

        // Get default function block
        FunctionBlock fb = fis.getFunctionBlock(null);

        int [] lcom = {25,16,25,25,25,23}; // 15,25
        int [] nom = {40,16,16,18,30,40}; // 15,40
        int [] noa = {15,6,6,7,15,15}; // 5,15

        for (int i = 0; i< lcom.length;i++){
            fb.setVariable("lack_of_cohesion_in_methods", lcom[i]);
            fb.setVariable("number_of_methods", nom[i]);
            fb.setVariable("number_of_attributes", noa[i]);
            fb.evaluate();
            System.out.println("Res ("+lcom[i]+","+nom[i]+","+noa[i]+"): " + fb.getVariable("res").getValue());
        }
    }
}
