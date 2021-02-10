package dataaccess;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.RateLimiter;
import ondb.NDCSConnectionProvider;
import ondb.NDCSCredsProviderForIAM;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.LineNumberReader;
import java.security.Security;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Loads a flat directory of JSON files containing demo baggage tracking data
 * into the demo table.  This is a temporary class that is only needed until the
 * Oracle NoSQL Database import/export tools upports the cloud service API for loading
 */
public class DataLoader {

    private static final String ARG_DATA_FILES_DIR = "-d";
    private static final String ARG_CREDENTIALS_FILES = "-c";
    private static final String ARG_WHACK_TABLE_BEFORE_LOADING = "-w";
    private static final String ARG_TIMEOUT_VAL = "-t";
    private static final int DEFAULT_NUM_FILES_PER_THREAD = 20;

    private static int timeOutInSecs = 5;
    private int primaryKey;
    private RateLimiter rateLimiter;
    private static File dataDirectory = null;
    private static boolean whackTable = false;
    private static int numCompletedThreads = 0;
    private static Object workCompletionSema = new Object();

    public static void main(String args[]) {
        String dataDir = null;
        String credsFile = null;

        int i = 0;
        while (i < args.length) {
            if ((args[i] != null) && (args[i].equalsIgnoreCase(ARG_TIMEOUT_VAL))) {
                if (args.length > ++i) {
                    try {
                        timeOutInSecs = Integer.parseInt(args[i++]);
                    } catch (NumberFormatException e) {
                        usageAndExit("Expected to find a valid integer specified after " +
                                ARG_TIMEOUT_VAL, false);
                    }
                } else {
                    usageAndExit("Expected to find a valid integer specified after " +
                            ARG_TIMEOUT_VAL, false);
                }
            } else if ((args[i] != null) && (args[i].equalsIgnoreCase(ARG_DATA_FILES_DIR))) {
                if (args.length > ++i) {
                    dataDir = args[i++];
                } else {
                    usageAndExit("Expected to find template file specified after " +
                            ARG_DATA_FILES_DIR, false);
                }
            } else if ((args[i] != null) && (args[i].equalsIgnoreCase(ARG_CREDENTIALS_FILES))) {
                if (args.length > ++i) {
                    credsFile = args[i++];
                } else {
                    usageAndExit("Expected to find a path to a credentials file specified after " +
                            ARG_CREDENTIALS_FILES, false);
                }
            } else if ((args[i] != null) && (args[i].equalsIgnoreCase(ARG_WHACK_TABLE_BEFORE_LOADING))) {
                whackTable = true;
                i++;
            } else {
                usageAndExit("Unrecognized option " + args[i], true);
            }
        }
        if (dataDir == null) {
            usageAndExit("You must supply a directory for data loading using " +
                    ARG_DATA_FILES_DIR, false);
        }

        dataDirectory = new File(dataDir);
        if (!dataDirectory.isDirectory()) {
            usageAndExit(dataDir + " is not a directory", false);
        }

        if (credsFile == null) {
            usageAndExit("You must supply a file containing credential information using " +
                    ARG_CREDENTIALS_FILES, false);
        }

        File credentials = new File(credsFile);
        if (!credentials.exists()) {
            usageAndExit("The file you supplied using " +
                    ARG_CREDENTIALS_FILES + " does not exist", false);
        }
        DemoTableDataConnection handle = null;

        Security.addProvider(new BouncyCastleProvider());
        try {
            handle = new NDCSConnectionProvider().getConnection(
                    NDCSCredsProviderForIAM.getCredsFromFile(credentials), getNumThreadsToSpawn(), false);
            if (whackTable) {
                handle.dropDemoTable();
            }

            handle.createDemoTable();
            //  Get the provisioned throughput just in case the table had already been created
            //  outside of this code and it has different limits
            ProvisionedCapacity capacity = handle.getProvisionedThroughput();
            System.out.println("Rate limiting the loading of data into " + DemoTable.TABLE_NAME +
                    " to " + capacity.getWriteUnits() / 2 + " inserts per second");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }

        DataLoader loader = new DataLoader();

        try {
            loader.load(dataDirectory, handle);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }

    private void createTable(DemoTableDataConnection conn) throws Exception {
        conn.createDemoTable();
    }

    private void load(File jsonFilesDirectory, DemoTableDataConnection conn)
            throws Exception {
        //  First get the configured througput for the table.  We don't want to start getting throttling
        //  exceptions by throwing more work at the table than it is configured to handle
        ProvisionedCapacity throughput = conn.getProvisionedThroughput();
        rateLimiter = RateLimiter.create((throughput.getWriteUnits() / 2));

        long writeUnits = throughput.getWriteUnits();
        File[] jsonFiles = jsonFilesDirectory.listFiles(new JsonFileFilter());

        // Create a pool of threads to process files in parallel.
        // DEFAULT_NUM_FILES_PER_THREAD files per thread
        int numThreads = getNumThreadsToSpawn();

        //  And we'll use the main therad to process any remainder
        int numFilesForMainThread = jsonFiles.length % DEFAULT_NUM_FILES_PER_THREAD;

        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        int currFilesIndex = 0;
        for (int i = 0; i < numThreads; i++) {
            File[] chunkOFiles = new File[DEFAULT_NUM_FILES_PER_THREAD];
            System.arraycopy(jsonFiles, currFilesIndex, chunkOFiles,
                    0, DEFAULT_NUM_FILES_PER_THREAD);
            currFilesIndex += DEFAULT_NUM_FILES_PER_THREAD;
            Runnable fileWorker = new FileProcessor("FileProcessor" + i,
                    chunkOFiles, conn, this);
            executor.execute(fileWorker);
        }

        //  Process the remaining files in this thread (main)
        if (numFilesForMainThread != 0) {
            File[] chunkOFiles = new File[numFilesForMainThread];
            System.arraycopy(jsonFiles, currFilesIndex, chunkOFiles,
                    0, numFilesForMainThread);
            FileProcessor processor = new FileProcessor("main",
                    chunkOFiles, conn, this);
            processor.run();
        }
        if (getNumCompletedThreads() != getNumThreadsToSpawn()) {
            synchronized (workCompletionSema) {
                workCompletionSema.wait();
            }
        }
        System.exit(0);

    }

    private static int getNumThreadsToSpawn() {
        return (dataDirectory.listFiles(new JsonFileFilter()).length / DEFAULT_NUM_FILES_PER_THREAD);
    }

    synchronized int getNextUniquePK() {
        return (++primaryKey);
    }

    void incNumCompletedThreads() {
        synchronized(workCompletionSema) {
            numCompletedThreads++;
            if (numCompletedThreads == getNumThreadsToSpawn()) {
                workCompletionSema.notify();
            }
        }
    }

    synchronized int getNumCompletedThreads() {
        return(numCompletedThreads);
    }

    private static void usageAndExit(String message, boolean printUsageInfo) {
        System.out.println(message);
        if (printUsageInfo) {
            System.out.println("dataaccess.DataLoader ");
            System.out.println("\t " + ARG_DATA_FILES_DIR + " directory of JSON data file to load");
            System.out.println("\t " + ARG_CREDENTIALS_FILES + " path to a file containing credentials to log into NoSQL cloud service");
            System.out.println("\t [" + ARG_WHACK_TABLE_BEFORE_LOADING + "] drop athe demo table before re-creaitng it");
        }
        System.exit(-1);
    }

    static class JsonFileFilter implements FilenameFilter {
        @Override
        public boolean accept(File dir, String name) {
            return(name.indexOf(".json") != -1);
        }
    }

    class FileProcessor implements Runnable {
        String threadName = null;
        File[] myFiles = null;
        DemoTableDataConnection datastoreHandle = null;
        DataLoader dataLoader = null;

        FileProcessor(String name, File[] files, DemoTableDataConnection conn,
                      DataLoader loader) {
            threadName = name;
            myFiles = files;
            dataLoader = loader;
            datastoreHandle = conn;
        }

        @Override
        public void run() {
            try {
                for (File file : myFiles) {
                    LineNumberReader reader = new LineNumberReader(new FileReader(file));
                    StringBuffer jsonStr = new StringBuffer();
                    String line = null;

                    while ((line = reader.readLine()) != null) {
                        jsonStr.append(line);
                    }

                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode json = mapper.readTree(jsonStr.toString());
                    rateLimiter.acquire();

                    datastoreHandle.writeOneRecord(json);
                    System.out.println("Thread " + "wrote pk " + json.get("ticketNo").asText() +
                            " for file " + file.getName());
                }
                incNumCompletedThreads();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}
