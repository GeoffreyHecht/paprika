package paprika.neo4j;

import org.neo4j.graphdb.RelationshipType;

/**
 * Created by Geoffrey Hecht on 05/06/14.
 */
public enum RelationTypes implements RelationshipType {
    APP_OWNS_CLASS,
    CLASS_OWNS_METHOD,
    APP_OWNS_METRIC,
    CLASS_OWNS_METRIC,
    METHOD_OWNS_METRIC,
    EXTENDS
}
