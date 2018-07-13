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

package paprika.neo4j.queries.antipatterns.fuzzy;

import net.sourceforge.jFuzzyLogic.FIS;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import paprika.neo4j.QueryEngine;
import paprika.neo4j.queries.PaprikaQuery;
import paprika.neo4j.queries.QueryPropertiesReader;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Geoffrey Hecht on 17/08/15.
 */
public abstract class FuzzyQuery extends PaprikaQuery {

    private static final String fclFolder = "/fcl/";
    private String fclFile;

    public FuzzyQuery(String name, QueryEngine queryEngine, String fclFile) {
        super(name, queryEngine);
        this.fclFile = fclFile;
    }

    public void executeFuzzy(boolean details) throws IOException {
        try (Transaction ignored = graphDatabaseService.beginTx()) {
            String query = getFuzzyQuery(details);
            Result result = graphDatabaseService.execute(query);
            List<String> columns = new ArrayList<>(result.columns());
            columns.add("fuzzy_value");
            queryEngine.fuzzyResultToCSV(getFuzzyResult(result, getFcl()), columns, super.getCSVSuffix());
        }
    }

    private FIS getFcl() throws FileNotFoundException {
        File fcf = new File(fclFile);
        // We look if the file is in a directory otherwise we look inside the jar
        if (fcf.exists() && !fcf.isDirectory()) {
            return FIS.load(injectProperties(new FileInputStream(fclFile)), false);
        } else {
            return FIS.load(injectProperties(getClass().getResourceAsStream(fclFolder + fclFile)), false);
        }
    }

    private InputStream injectProperties(InputStream original) {
        String function = new BufferedReader(new InputStreamReader(original))
                .lines().collect(Collectors.joining("\n"));
        function = QueryPropertiesReader.replaceProperties(function);
        return new ByteArrayInputStream(function.getBytes());
    }

    public abstract String getFuzzyQuery(boolean details);

    public abstract List<Map<String, Object>> getFuzzyResult(Result result, FIS fis);

    @Override
    protected String getCSVSuffix() {
        return "_" + queryName + "_NO_FUZZY.csv";
    }

    @Override
    public void run(boolean details) throws IOException {
        executeFuzzy(details);
    }

}
