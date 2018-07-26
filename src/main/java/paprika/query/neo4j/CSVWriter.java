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

package paprika.query.neo4j;

import org.neo4j.graphdb.Result;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CSVWriter {

    private String csvPrefix;

    public CSVWriter(String csvPrefix) {
        this.csvPrefix = csvPrefix;
    }

    public void resultToCSV(Result result, String csvSuffix) throws IOException {
        String name = csvPrefix + csvSuffix;
        BufferedWriter writer = new BufferedWriter(new FileWriter(name));
        List<String> columns = result.columns();
        writeColumnLabels(columns, writer);
        while (result.hasNext()) {
            writeRowValues(result.next(), columns, writer);
        }
        writer.close();
    }

    private void writeColumnLabels(List<String> columns, BufferedWriter writer) throws IOException {
        for (int i = 0; i < columns.size() - 1; i++) {
            writer.write(columns.get(i));
            writer.write(',');
        }
        writer.write(columns.get(columns.size() - 1));
        writer.newLine();
    }

    private void writeRowValues(Map<String, Object> row, List<String> columns, BufferedWriter writer)
            throws IOException {
        Object val;
        for (int i = 0; i < columns.size() - 1; i++) {
            val = row.get(columns.get(i));
            if (val != null) {
                writer.write(val.toString());
                writer.write(',');
            }
        }
        val = row.get(columns.get(columns.size() - 1));
        if (val != null) {
            writer.write(val.toString());
        }
        writer.newLine();
    }

    public void fuzzyResultToCSV(List<Map<String, Object>> rows, List<String> columns, String csvSuffix)
            throws IOException {
        String name = csvPrefix + csvSuffix;
        BufferedWriter writer = new BufferedWriter(new FileWriter(name));
        writeColumnLabels(columns, writer);
        for (Map<String, Object> row : rows) {
            writeRowValues(row, columns, writer);
        }
        writer.close();
    }

    public void statsToCSV(Map<String, Double> stats, String csvSuffix) throws IOException {
        String name = csvPrefix + csvSuffix;
        FileWriter fw = new FileWriter(name);
        BufferedWriter writer = new BufferedWriter(fw);
        Set<String> keys = stats.keySet();
        for (String key : keys) {
            writer.write(key);
            writer.write(',');
        }
        writer.newLine();
        for (String key : keys) {
            writer.write(String.valueOf(stats.get(key)));
            writer.write(',');
        }
        writer.close();
        fw.close();
    }

}
