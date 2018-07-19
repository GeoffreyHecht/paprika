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

package paprika.entities;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Geoffrey Hecht on 20/05/14.
 */
public class PaprikaApp extends Entity {

    // Neo4J attributes names
    public static final String APP_KEY = "app_key";
    public static final String NAME = "name";
    public static final String CATEGORY = "category";
    public static final String PACKAGE = "package";
    public static final String DEVELOPER = "developer";
    public static final String RATING = "rating";
    public static final String NB_DOWN = "nb_download";
    public static final String DATE_DOWN = "date_download";
    public static final String VERSION_CODE = "version_code";
    public static final String VERSION_NAME = "version_name";
    public static final String SDK = "sdk";
    public static final String TARGET_SDK = "target_sdk";
    public static final String DATE_ANALYSIS = "date_analysis";
    public static final String SIZE = "size";
    public static final String PRICE = "price";

    public static final int NO_SDK = -1;

    private double rating;
    private String date;
    private String pack; //Package
    private int size;
    private String developer;
    private String category;
    private String price;
    private String key;
    private int nbDownload;
    private String versionCode;
    private String versionName;
    private int sdkVersion;
    private int targetSdkVersion;
    private List<PaprikaClass> paprikaClasses;
    private List<PaprikaExternalClass> paprikaExternalClasses;

    private PaprikaApp(String name, String key, String pack, String date, int size, String developer,
                       String category, String price, double rating, int nbDownload, String versionCode,
                       String versionName, int sdkVersion, int targetSdkVersion) {
        this.name = name;
        this.key = key;
        this.pack = pack;
        this.date = date;
        this.size = size;
        this.developer = developer;
        this.category = category;
        this.price = price;
        this.rating = rating;
        this.nbDownload = nbDownload;
        this.paprikaClasses = new ArrayList<>();
        this.paprikaExternalClasses = new ArrayList<>();
        this.versionCode = versionCode;
        this.versionName = versionName;
        this.sdkVersion = sdkVersion;
        this.targetSdkVersion = targetSdkVersion;
    }


    public List<PaprikaExternalClass> getPaprikaExternalClasses() {
        return paprikaExternalClasses;
    }

    public static PaprikaApp createPaprikaApp(String name, String key, String pack, String date,
                                              int size, String dev, String cat, String price,
                                              double rating, int nbDownload, String versionCode,
                                              String versionName, int sdkVersion, int targetSdkVersion) {
        return new PaprikaApp(name, key, pack, date, size, dev, cat, price, rating, nbDownload,
                versionCode, versionName, sdkVersion, targetSdkVersion);
    }

    public List<PaprikaClass> getPaprikaClasses() {
        return paprikaClasses;
    }

    public void addPaprikaExternalClass(PaprikaExternalClass paprikaExternalClass) {
        paprikaExternalClasses.add(paprikaExternalClass);
    }

    public void addPaprikaClass(PaprikaClass paprikaClass) {
        paprikaClasses.add(paprikaClass);
    }

    public double getRating() {
        return rating;
    }

    public String getDate() {
        return date;
    }

    public String getPackage() {
        return pack;
    }

    public int getSize() {
        return size;
    }

    public String getDeveloper() {
        return developer;
    }

    public String getCategory() {
        return category;
    }

    public String getPrice() {
        return price;
    }

    public String getKey() {
        return key;
    }

    public int getNbDownload() {
        return nbDownload;
    }

    public String getVersionCode() {
        return versionCode;
    }

    public String getVersionName() {
        return versionName;
    }

    public int getSdkVersion() {
        return sdkVersion;
    }

    public int getTargetSdkVersion() {
        return targetSdkVersion;
    }

    public void setTargetSdkVersion(int targetSdkVersion) {
        this.targetSdkVersion = targetSdkVersion;
    }

    public void setPackage(String pack) {
        this.pack = pack;
    }

    public boolean hasTargetSDK() {
        return targetSdkVersion != NO_SDK;
    }

    public boolean hasPackage() {
        return !"".equals(pack);
    }

    public boolean hasName() {
        return !"".equals(name);
    }
}
