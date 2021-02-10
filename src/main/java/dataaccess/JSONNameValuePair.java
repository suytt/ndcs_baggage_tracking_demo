package dataaccess;

public class JSONNameValuePair {
    public String path = null;
    public String value = null;

    public JSONNameValuePair(String JSONPath, String JSONValue) {
        this.path = JSONPath;
        value = JSONValue;
    }
}
