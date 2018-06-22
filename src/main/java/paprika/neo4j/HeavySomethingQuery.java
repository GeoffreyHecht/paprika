package paprika.neo4j;

public abstract class HeavySomethingQuery extends FuzzyQuery {

    public HeavySomethingQuery(QueryEngine queryEngine) {
        super(queryEngine);
        fclFile = fclFolder + "HeavySomething.fcl";
    }

}
