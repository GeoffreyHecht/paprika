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

package paprika.launcher;

import net.dongliu.apk.parser.ApkFile;
import net.dongliu.apk.parser.bean.ApkMeta;
import paprika.entities.PaprikaApp;
import paprika.entities.PaprikaAppBuilder;
import soot.Scene;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import static paprika.launcher.Argument.*;

public class PaprikaAppCreator {

    private PaprikaAppBuilder builder;
    private String apkPath;
    private PaprikaArgParser argParser;

    public PaprikaAppCreator(PaprikaArgParser argParser, String apkPath) {
        this.apkPath = apkPath;
        this.argParser = argParser;
        this.builder = new PaprikaAppBuilder();
    }

    public void readAppInfo() throws IOException, NoSuchAlgorithmException {
        builder.name(argParser.getArg(NAME_ARG))
                .pack(argParser.getArg(PACKAGE_ARG))
                .date(argParser.getArg(DATE_ARG))
                .size(argParser.getIntArg(SIZE_ARG))
                .developer(argParser.getArg(DEVELOPER_ARG))
                .category(argParser.getArg(CATEGORY_ARG))
                .price(argParser.getArg(PRICE_ARG))
                .rating(argParser.getDoubleArg(RATING_ARG))
                .nbDownload(argParser.getIntArg(NB_DOWNLOAD_ARG))
                .versionCode(argParser.getArg(VERSION_CODE_ARG))
                .versionName(argParser.getArg(VERSION_NAME_ARG))
                .sdkVersion(argParser.getIntArg(SDK_VERSION_ARG))
                .targetSdkVersion(argParser.getIntArg(TARGET_SDK_VERSION_ARG));
        if (!argParser.isFolderMode()) {
            builder.key(argParser.getSha());
        } else {
            builder.name("");
            builder.key(argParser.computeSha256(apkPath));
            builder.pack("");
        }
    }

    /**
     * Run only after Soot has been given the apk path.
     */
    public void fetchMissingAppInfo() throws IOException {
        if (!builder.hasTargetSDK()) {
            builder.targetSdkVersion(Scene.v().getAndroidAPIVersion());
        }
        if (!builder.hasPackage()) {
            try (ApkFile apkFile = new ApkFile(new File(apkPath))) {
                ApkMeta apkMeta = apkFile.getApkMeta();
                builder.pack(apkMeta.getPackageName());
            }
        }
        if (!builder.hasName()) {
            String filename = new File(apkPath).getName();
            if (filename.endsWith(".apk")) {
                filename = filename.substring(0, filename.length() - 4);
            }
            builder.name(filename);
        }
    }

    /**
     * Run only after the app name has already been read from its filename.
     */
    public void addApkProperties(ApkPropertiesParser propsParser) {
        if (!propsParser.hasProperties(builder.getName())) {
            return;
        }
        for (Argument arg : Argument.PROPS_ARGS) {
            arg.insertAppProperty(propsParser, builder);
        }
        // Name has to be last since the field is used to fetch the other properties
        Argument.NAME_ARG.insertAppProperty(propsParser, builder);
    }

    public PaprikaApp createApp() {
        return builder.create();
    }

}
