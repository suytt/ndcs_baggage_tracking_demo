package ondb;

import oracle.nosql.driver.values.MapValue;
import java.util.List;

public class DemoQueryResult {
    long queryTimeInMillis;
    List<MapValue> queryResults;

    public DemoQueryResult(long timeMillis, List<MapValue> res) {
        this.queryTimeInMillis = timeMillis;
        this.queryResults = res;
    }

    public long getQueryTimeInMillis() {
        return queryTimeInMillis;
    }

    public void setQueryTimeInMillis(long queryTimeInMillis) {
        this.queryTimeInMillis = queryTimeInMillis;
    }

    public List<MapValue> getQueryResults() {
        return queryResults;
    }

    public void setQueryResults(List<MapValue> queryResults) {
        this.queryResults = queryResults;
    }
}
