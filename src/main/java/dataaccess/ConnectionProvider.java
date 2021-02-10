package dataaccess;

/**
 * Factory interface for instantiating connections to the underling datastore
 */
public interface ConnectionProvider {

    DemoTableDataConnection getConnection(BaseCredentialsProvider credsProvider,
                                          int connectionPoolSize, boolean prepareQueries);
}
