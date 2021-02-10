package ondb;

import dataaccess.BaseCredentialsProvider;
import dataaccess.ConnectionProvider;
import dataaccess.DemoTableDataConnection;
import oracle.nosql.driver.NoSQLHandleConfig;
import oracle.nosql.driver.NoSQLHandleFactory;
import oracle.nosql.driver.iam.SignatureProvider;
import oracle.nosql.driver.kv.StoreAccessTokenProvider;
//import oracle.nosql.driver.idcs.DefaultAccessTokenProvider;

import java.io.File;
import java.net.URL;


/**
 * Factory class for creating connections of Oracle NoSQL Database Cloud Service
 */
public class NDCSConnectionProvider implements ConnectionProvider {
    public DemoTableDataConnection getConnection(BaseCredentialsProvider credsProvider,
                                                 int connectopPoolSize,
                                                 boolean prepareQueries) {
        try {

            NDCSCredsProviderForIAM creds = (NDCSCredsProviderForIAM) credsProvider;
            URL serviceURL = new URL("https", creds.getRegionalURI(), 443, "/");
            NoSQLHandleConfig config = new NoSQLHandleConfig(serviceURL);
            config.setConnectionPoolSize(connectopPoolSize);

            SignatureProvider authProvider = new SignatureProvider(creds.getTenantOCID(),
                    creds.getUserOCID(),
                    creds.getFingerprint(),
                    new File(creds.getPathToSKFile()),
                    creds.getSkPassword());
            config.setRequestTimeout(15000);
            config.setAuthorizationProvider(authProvider);
            config.configureDefaultRetryHandler(1, 10);
            config.setDefaultCompartment(creds.getCompartment());
            return(new NDCSConnection(NoSQLHandleFactory.createNoSQLHandle(config), prepareQueries));

        } catch (Throwable t) {
            t.printStackTrace();
            return(null);
        }
    }
}
