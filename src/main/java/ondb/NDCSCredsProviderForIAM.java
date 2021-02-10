package ondb;

import com.fasterxml.jackson.databind.ObjectMapper;
import dataaccess.BaseCredentialsProvider;

import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.net.URLEncoder;
import java.util.Map;

/**
 * For authenticating with the production NDCS cloud service. Several places need to be edited to add data for your
 * cloud account and credentials.  The following docs will tell you how to find this data:
 *  http://www.oracle.com/webfolder/technetwork/tutorials/obe/cloud/ans/creating_table_in_ans/creating_table_in_ans.html
 *
 * The places in this class that need to be edited:
 *  ENTITLEMENT_ID - This is the cloud account that has entitlement to NDCS.  Basically, any account
 *                   that this code is running as will have entitlement to NDCS.
 *  IDCS_URL - The URL to your IDCS identity domain
 *  IDCS credentials in getOAuthCredentials - Your application ID and application secret
 *  IDCS credentials in getUserCredentials - The credentials that you use to log into your cloud account
 */
public class NDCSCredsProviderForIAM implements BaseCredentialsProvider {

    public static final String  ENV_VAR_REGION_URI = "region_uri";
    public static final String  ENV_VAR_TENANT_OCID = "tenant_OCID";
    public static final String  ENV_VAR_USER_OCID = "user_OCID";
    public static final String  ENV_VAR_FINGERPRINT = "fingerprint";
    public static final String  ENV_VAR_PATH_TO_PK_FILE = "path_to_signing_key_file";
    public static final String  ENV_VAR_SK_PASSWORD = "signing_key_password";
    public static final String  ENV_VAR_COMPARTMENT = "compartment";

    private static String refreshToken = null;

    private String regionalURI = null;
    private String tenantOCID = null;
    private String userOCID = null;
    private String fingerprint = null;
    private String pathToSKFile = null;
    private char[] skPassword = null;
    private String compartment = null;

    public static String getRefreshToken() {
        return refreshToken;
    }

    public static void setRefreshToken(String refreshToken) {
        NDCSCredsProviderForIAM.refreshToken = refreshToken;
    }

    public String getRegionalURI() {
        return regionalURI;
    }

    public void setRegionalURI(String regionalURI) {
        this.regionalURI = regionalURI;
    }

    public String getTenantOCID() {
        return tenantOCID;
    }

    public void setTenantOCID(String tenantOCID) {
        this.tenantOCID = tenantOCID;
    }

    public String getUserOCID() {
        return userOCID;
    }

    public void setUserOCID(String userOCID) {
        this.userOCID = userOCID;
    }

    public String getFingerprint() {
        return fingerprint;
    }

    public void setFingerprint(String fingerprint) {
        this.fingerprint = fingerprint;
    }

    public String getPathToSKFile() {
        return pathToSKFile;
    }

    public void setPathToSKFile(String pathToSKFile) {
        this.pathToSKFile = pathToSKFile;
    }

    public char[] getSkPassword() {
        return skPassword;
    }

    public void setSkPassword(char[] skPassword) {
        this.skPassword = skPassword;
    }

    public String getCompartment() {
        return compartment;
    }

    public void setCompartment(String compartment) {
        this.compartment = compartment;
    }

    public String toJsonString() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        return(mapper.writeValueAsString(this));

    }

    public static NDCSCredsProviderForIAM getCredsFromEnvironment() throws Exception {
        Map<String,String> vars = System.getenv();

        String tenantOCID = vars.get(ENV_VAR_TENANT_OCID);
        if (tenantOCID == null)
            throw new Exception("Expected to find " + ENV_VAR_TENANT_OCID +
                    " environment variable");
        String userOCID = vars.get(ENV_VAR_USER_OCID);
        if (userOCID == null) {
            throw new Exception("Expected to find " + ENV_VAR_USER_OCID +
                    " environment variable");
        }
        String fingerprint = vars.get(ENV_VAR_FINGERPRINT);
        if (fingerprint == null) {
            throw new Exception("Expected to find " + ENV_VAR_FINGERPRINT +
                    " environment variable");
        }
        String pathToSKFile = vars.get(ENV_VAR_PATH_TO_PK_FILE);
        if (pathToSKFile == null) {
            throw new Exception("Expected to find " + ENV_VAR_PATH_TO_PK_FILE +
                    " environment variable");
        } else {
            File f = new File(pathToSKFile);
            if (!f.exists()) {
                throw new Exception("Could not find file " +
                        pathToSKFile + " specified by parameter " + ENV_VAR_PATH_TO_PK_FILE +
                        " environment variable");
            }
        }

        String skPassPhrase = vars.get(ENV_VAR_SK_PASSWORD);
        if (skPassPhrase == null) {
            throw new Exception("Expected to find " + ENV_VAR_SK_PASSWORD +
                    " environment variable");
        }

        String uri = vars.get(ENV_VAR_REGION_URI);
        if (uri == null) {
            throw new Exception("Expected to find " + ENV_VAR_REGION_URI +
                    " environment variable");
        }

        String compartment = vars.get(ENV_VAR_COMPARTMENT);
        if (compartment == null) {
            throw new Exception("Expected to find " + ENV_VAR_COMPARTMENT +
                    " environment variable");
        }

        NDCSCredsProviderForIAM ret = new NDCSCredsProviderForIAM();
        ret.setRegionalURI(uri);
        ret.setTenantOCID(tenantOCID);
        ret.setUserOCID(userOCID);
        ret.setFingerprint(fingerprint);
        ret.setSkPassword(skPassPhrase.toCharArray());
        ret.setPathToSKFile(pathToSKFile);
        ret.setCompartment(compartment);
        return (ret);
    }

    public static NDCSCredsProviderForIAM getCredsFromFile(File credentials) throws Exception {

        NDCSCredsProviderForIAM ret = new NDCSCredsProviderForIAM();

        LineNumberReader reader = new LineNumberReader(new FileReader(credentials));
        String line = null;
        while ((line = reader.readLine()) != null) {
            String[] parms = line.split("=");
            if (parms[0].equalsIgnoreCase(ENV_VAR_TENANT_OCID)) {
                ret.setTenantOCID(parms[1]);
            } else if (parms[0].equalsIgnoreCase(ENV_VAR_USER_OCID)) {
                ret.setUserOCID(parms[1]);
            } else if (parms[0].equalsIgnoreCase(ENV_VAR_FINGERPRINT)) {
                ret.setFingerprint(parms[1]);
            } else if (parms[0].equalsIgnoreCase(ENV_VAR_SK_PASSWORD)) {
                ret.setSkPassword(parms[1].toCharArray());
            } else if (parms[0].equalsIgnoreCase(ENV_VAR_PATH_TO_PK_FILE)) {
                ret.setPathToSKFile(parms[1]);
            } else if (parms[0].equalsIgnoreCase(ENV_VAR_REGION_URI)) {
                ret.setRegionalURI(parms[1]);
            } else if (parms[0].equalsIgnoreCase(ENV_VAR_COMPARTMENT)) {
                ret.setCompartment(parms[1]);
            }
        }


        if (ret.getTenantOCID() == null) {
            throw new Exception("Expected to find " + ENV_VAR_TENANT_OCID +
                    " in credentials file");
        }
        if (ret.getUserOCID() == null) {
            throw new Exception("Expected to find " + ENV_VAR_USER_OCID +
                    " in credentials file");
        }
        if (ret.getFingerprint() == null) {
            throw new Exception("Expected to find " + ENV_VAR_FINGERPRINT +
                    " in credentials file");
        }
        if (ret.getPathToSKFile() == null) {
            throw new Exception("Expected to find " + ENV_VAR_PATH_TO_PK_FILE +
                    " in credentials file");
        }
        if (ret.getSkPassword() == null) {
            throw new Exception("Expected to find " + ENV_VAR_SK_PASSWORD +
                    " in credentials file");
        }

        if (ret.regionalURI == null) {
            throw new Exception("Expected to find " + ENV_VAR_REGION_URI +
                    " in credentials file");
        }

        if (ret.compartment == null) {
            throw new Exception("Expected to find " + ENV_VAR_COMPARTMENT +
                    " in credentials file");
        }

        return (ret);
    }

    public String toString() {
       StringBuilder str = new StringBuilder();
        str.append("\t URI = " + getRegionalURI() + "\n")
                .append("\t Tenant OCID = " + getTenantOCID() + "\n")
                .append("\t User OCID = " + getUserOCID() + "\n")
                .append("\t Fingerprint = " + getFingerprint() + "\n")
                .append("\t Path to PK Signing File = " + getPathToSKFile() + "\n")
                .append("\t PK Signing Password = " + getSkPassword() + "\n" +
                        "\t Compartment = " + getCompartment());
        return(str.toString());
    }

}
