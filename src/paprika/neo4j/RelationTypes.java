package paprika.neo4j;

import org.neo4j.graphdb.RelationshipType;

/**
 * Created by Geoffrey Hecht on 05/06/14.
 */
public enum RelationTypes implements RelationshipType {
    APP_OWNS_CLASS,
    CLASS_OWNS_METHOD,
    CLASS_OWNS_VARIABLE,
    IMPLEMENTS,
    EXTENDS,
    CALLS,
    USES,
    HAS
}
