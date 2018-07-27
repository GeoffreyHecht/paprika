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

package paprika.launcher.arg;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparsers;
import paprika.launcher.PaprikaMode;
import paprika.launcher.PaprikaStarter;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static paprika.launcher.PaprikaMode.ANALYSE_MODE;
import static paprika.launcher.arg.Argument.*;

public class PaprikaArgParser {

    private static final String JSON_PROPS_FILENAME = "apk-properties.json";
    private static final String SUB_PARSER = "sub_command";
    private static final String DATE_REGEX =
            "^([0-9]{4})-([0-1][0-9])-([0-3][0-9])\\s([0-1][0-9]|[2][0-3]):([0-5][0-9]):([0-5][0-9]).([0-9]*)$";

    private ArgumentParser parser;
    private Namespace res;

    public PaprikaArgParser() {
        parser = ArgumentParsers.newFor("paprika").build();
        Subparsers subparsers = parser.addSubparsers().dest(SUB_PARSER);
        for (PaprikaMode mode : PaprikaMode.values()) {
            mode.setupAllArgs(subparsers);
        }
    }

    public void parseArgs(String[] args) throws ArgumentParserException, PaprikaArgException {
        res = parser.parseArgs(args);
        if (isAnalyseMode() && res.get(UNSAFE_ARG.toString()) == null) {
            checkArgs();
        }
    }

    private boolean isAnalyseMode() {
        return res.getString(SUB_PARSER).equals(ANALYSE_MODE.toString());
    }

    public void checkArgs() throws PaprikaArgException {
        if (!res.getString(DATE_ARG.toString()).matches(DATE_REGEX)) {
            throw new PaprikaArgException("Date should be formatted : yyyy-mm-dd hh:mm:ss.S");
        }
    }

    public String computeSha256(String path) throws IOException, NoSuchAlgorithmException {
        byte[] buffer = new byte[2048];
        MessageDigest digest = MessageDigest.getInstance("SHA-256");

        try (InputStream is = new FileInputStream(path)) {
            while (true) {
                int readBytes = is.read(buffer);
                if (readBytes > 0)
                    digest.update(buffer, 0, readBytes);
                else
                    break;
            }
        }
        byte[] hashValue = digest.digest();
        StringBuilder sb = new StringBuilder();
        for (byte aHashValue : hashValue) {
            sb.append(Integer.toString((aHashValue & 0xff) + 0x100, 16).substring(1));
        }
        return sb.toString();
    }

    public PaprikaStarter getSelectedStarter(PrintStream out) {
        PaprikaMode selectedMode = PaprikaMode.getMode(res.getString(SUB_PARSER));
        if (selectedMode == null) {
            out.println(res.getString(SUB_PARSER) + " is not a valid Paprika argument.");
            return null;
        }
        return selectedMode.getStarter(this, out);
    }

    public void handleError(ArgumentParserException e) {
        // All subparsers error handling are the same
        ANALYSE_MODE.getSubparser().handleError(e);
    }

    public String getArg(Argument arg) {
        return res.getString(arg.toString());
    }

    public int getIntArg(Argument arg) {
        return res.getInt(arg.toString());
    }

    public double getDoubleArg(Argument arg) {
        return res.getDouble(arg.toString());
    }

    public boolean getFlagArg(Argument arg) {
        return res.getBoolean(arg.toString());
    }

    public String getSha() throws IOException, NoSuchAlgorithmException {
        if (res.getString(KEY_ARG.toString()) == null) {
            return computeSha256(res.getString(APK_ARG.toString()));
        } else {
            return res.getString(KEY_ARG.toString());
        }
    }

    public List<String> getAppsPaths() {
        List<String> apps = new ArrayList<>();
        File apkFolder = new File(res.getString(APK_ARG.toString()));
        if (!apkFolder.isDirectory()) {
            return Collections.singletonList("");
        } else {
            searchApkInFolder(apkFolder, apps);
        }
        return apps;
    }

    private void searchApkInFolder(File folder, List<String> appsPath) {
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    searchApkInFolder(file, appsPath);
                }
                if (file.getName().endsWith(".apk")) {
                    appsPath.add(file.getPath());
                }
            }
        }
    }

    public List<String> getAppsFilenames(List<String> appsPaths) {
        return appsPaths.stream()
                .map((item) -> removeAPKExtension(new File(item).getName()))
                .collect(Collectors.toList());
    }

    private String removeAPKExtension(String original) {
        return original.substring(0, original.length() - 4);
    }

    public boolean isFolderMode() {
        File apkFolder = new File(res.getString(APK_ARG.toString()));
        return apkFolder.isDirectory();
    }


}
