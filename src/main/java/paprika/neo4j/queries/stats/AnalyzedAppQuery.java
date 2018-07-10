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

package paprika.neo4j.queries.stats;

import paprika.neo4j.QueryEngine;
import paprika.neo4j.queries.PaprikaQuery;

public class AnalyzedAppQuery extends PaprikaQuery {

    public static final String KEY = "ANALYZED";

    public AnalyzedAppQuery(QueryEngine engine) {
        super(KEY, engine);
    }

    @Override
    public String getQuery(boolean details) {
        return "MATCH (a:App) RETURN  a.app_key as app_key, a.category as category,a.package as package," +
                " a.version_code as version_code, a.date_analysis as date_analysis,a.number_of_classes as number_of_classes," +
                "a.size as size,a.rating as rating,a.nb_download as nb_download, a.number_of_methods as number_of_methods," +
                " a.number_of_activities as number_of_activities,a.number_of_services as number_of_services," +
                "a.number_of_interfaces as number_of_interfaces,a.number_of_abstract_classes as number_of_abstract_classes," +
                "a.number_of_broadcast_receivers as number_of_broadcast_receivers,a.number_of_content_providers as number_of_content_providers," +
                " a.number_of_variables as number_of_variables, a.number_of_views as number_of_views," +
                " a.number_of_inner_classes as number_of_inner_classes, a.number_of_async_tasks as number_of_async_tasks";
    }

}
