package ondb;

import oracle.nosql.driver.values.MapValue;

public class DemoCRUDResult {
    MapValue result;
    long queryTimeInMillis;

    public DemoCRUDResult(long time, MapValue res) {
        this.queryTimeInMillis = time;
        this.result = res;
    }

    public MapValue getResult() {
        return result;
    }

    public void setResult(MapValue result) {
        this.result = result;
    }

    public long getQueryTimeInMillis() {
        return queryTimeInMillis;
    }

    public void setQueryTimeInMillis(long queryTimeInMillis) {
        this.queryTimeInMillis = queryTimeInMillis;
    }
}
