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

package paprika.analyse.entities;

import static paprika.analyse.entities.PaprikaApp.NO_SDK;

@SuppressWarnings("UnusedReturnValue")
public class PaprikaAppBuilder {

    private String name;
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
    private int sdkVersion = NO_SDK;
    private int targetSdkVersion = NO_SDK;

    public String getName() {
        return name;
    }

    public PaprikaAppBuilder name(String name) {
        this.name = name;
        return this;
    }

    public PaprikaAppBuilder rating(double rating) {
        this.rating = rating;
        return this;
    }

    public PaprikaAppBuilder date(String date) {
        this.date = date;
        return this;
    }

    public PaprikaAppBuilder pack(String pack) {
        this.pack = pack;
        return this;
    }

    public PaprikaAppBuilder size(int size) {
        this.size = size;
        return this;
    }

    public PaprikaAppBuilder developer(String developer) {
        this.developer = developer;
        return this;
    }

    public PaprikaAppBuilder category(String category) {
        this.category = category;
        return this;
    }

    public PaprikaAppBuilder price(String price) {
        this.price = price;
        return this;
    }

    public PaprikaAppBuilder key(String key) {
        this.key = key;
        return this;
    }

    public PaprikaAppBuilder nbDownload(int nbDownload) {
        this.nbDownload = nbDownload;
        return this;
    }

    public PaprikaAppBuilder versionCode(String versionCode) {
        this.versionCode = versionCode;
        return this;
    }

    public PaprikaAppBuilder versionName(String versionName) {
        this.versionName = versionName;
        return this;
    }

    public PaprikaAppBuilder sdkVersion(int sdkVersion) {
        this.sdkVersion = sdkVersion;
        return this;
    }

    public PaprikaAppBuilder targetSdkVersion(int targetSdkVersion) {
        this.targetSdkVersion = targetSdkVersion;
        return this;
    }

    public boolean hasSDK() {
        return sdkVersion != NO_SDK;
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

    public PaprikaApp create() {
        return new PaprikaApp(name, key, pack, date, size, developer,
                category, price, rating, nbDownload, versionCode,
                versionName, sdkVersion, targetSdkVersion);
    }
}
