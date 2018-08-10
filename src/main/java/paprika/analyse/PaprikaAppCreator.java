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

package paprika.analyse;

import net.dongliu.apk.parser.ApkFile;
import net.dongliu.apk.parser.bean.ApkMeta;
import paprika.analyse.entities.PaprikaApp;
import paprika.analyse.entities.PaprikaAppBuilder;
import paprika.launcher.arg.Argument;
import paprika.launcher.arg.PaprikaArgParser;
import soot.Scene;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import static paprika.launcher.arg.Argument.*;

/**
 * Fetches information various sources to create a PaprikaApp with the correct attributes.
 * These sources include:
 * - The arguments given to Paprika
 * - The json properties file
 * - The information fetched from the filename of the application and its manifest
 */
public class PaprikaAppCreator {

    private PaprikaAppBuilder builder;
    private String apkPath;
    private PaprikaArgParser argParser;

    /**
     * Constructor.
     *
     * @param argParser the parser of the Paprika arguments
     * @param apkPath   the path to the apk of the application
     */
    public PaprikaAppCreator(PaprikaArgParser argParser, String apkPath) {
        this.apkPath = apkPath;
        this.argParser = argParser;
        this.builder = new PaprikaAppBuilder();
    }

    /**
     * Reads the application attributes from the arguments given to Paprika.
     * Overrides existing attributes if they exist (but this should usually be called first).
     * Also computes the SHA-256 of the application apk, used as an unique key.
     *
     * @throws IOException              if failing to open the apk file
     * @throws NoSuchAlgorithmException if failing to compute the SHA-256 of the apk file contents
     */
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
     * Read missing attributes of the PaprikaApp necessary for analysis if not found.
     * This includes:
     * - The target SDK, read from the manifest
     * - The application main package, read from te manifest
     * - The application name, defaulting to the apk filename
     * <p>
     * Run only after Soot has been given the apk path.
     *
     * @throws IOException if the apk cannot be found, or if failing to read its manifest
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
     * Read an app properties from the json properties file.
     * This overrides the existing PaprikaApp attributes.
     * <p>
     * Run only after the app name has already been read from its filename.
     */
    public void addApkProperties(ApkPropertiesParser propsParser) {
        if (!propsParser.hasProperties(builder.getName())) {
            return;
        }
        for (Argument arg : Argument.ANALYSE_PROPS_ARGS) {
            arg.insertAppProperty(propsParser, builder);
        }
        // Name has to be last since the field is used to fetch the other properties
        Argument.NAME_ARG.insertAppProperty(propsParser, builder);
    }

    public PaprikaApp createApp() {
        return builder.create();
    }

}
