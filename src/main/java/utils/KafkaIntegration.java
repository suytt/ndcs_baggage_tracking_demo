package utils;

import oracle.nosql.driver.NoSQLHandle;
import oracle.nosql.driver.NoSQLHandleConfig;
import oracle.nosql.driver.NoSQLHandleFactory;
import oracle.nosql.driver.iam.SignatureProvider;
import oracle.nosql.driver.ops.PutRequest;
import oracle.nosql.driver.ops.PutResult;
import oracle.nosql.driver.values.JsonOptions;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

public class KafkaIntegration {

    private static NoSQLHandle noSQLServiceHandle;

    public KafkaIntegration() {
        try {
            noSQLServiceHandle = connectToNDCS ("https://nosql.sa-santiago-1.oci.oraclecloud.com");
            runConsumer("Your Kafka Host goes here", "The topic to listen on goes here");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This method will loop indefinitely listening for incoming message on the Kafka topic.
     * A couple of thing to note about this code:
     *  1.  It assumes that the topic is the name of the table in the NoSQL cloud service.  If
     *      you want to store the record in a different table then change the table name in the
     *      PutRequest.
     *  2.  It assumes that the message is a JSON string that matches the schema of the NoSQL
     *  table.   In other words, all of the attributes in the JSON string match the attributes in
     *  the NoSQL table.
     *
     * @param kafkaHost The bootstrap host name of where to contact Kafka
     * @param topic The Kafka topic to subscribe to
     *
     * @throws InterruptedException
     */
    public void runConsumer(String kafkaHost,  String topic) throws Exception {

        final Consumer<String, String> consumer = createConsumer(kafkaHost, topic);

        while (true) {
            try {
                final ConsumerRecords<String, String> consumerRecords =
                        consumer.poll(Duration.ofMinutes(30));
                if (consumerRecords.count() == 0) {
                    continue;
                }
                consumerRecords.forEach(record -> {

                    PutRequest putRequest = new PutRequest()
                            .setValueFromJson(record.value(), new JsonOptions())
                            .setTableName(topic);

                    PutResult putRes = noSQLServiceHandle.put(putRequest);

                });
                consumer.commitAsync();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
    }

    /**
     * Creates a Kafka Consumer.
     *
     * @param host The host where a node in the Kafka cluster is running
     * @param topic The topic to subscribe to.
     * @return
     */
    private Consumer<String, String> createConsumer(String host, String topic) {
        String bootstrap_server = host + ":9092";
        final Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrap_server);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "KafkaConsumerKvStoreCode");

        final Consumer<String, String> consumer =
                new KafkaConsumer<String, String>(
                        props,
                        new StringDeserializer(),
                        new StringDeserializer());
        // Subscribe to the topic
        consumer.subscribe(Collections.singletonList(topic));
        return consumer;
    }


    /**
     * Create a connection to the NoSQL Database cloud service
     *
     * @param regionalEndpoint One of the URLs to the NoSQL cloud service running in a region.
     *                         Use one of the URLs from the list found here: https://docs.oracle.com/en/cloud/paas/nosql-cloud/csnsd/data-regions-and-associated-service-urls.html
     * @return A handle to the NoSQL cloud service
     */
    private static NoSQLHandle connectToNDCS(String regionalEndpoint) {
        NoSQLHandleConfig config = new NoSQLHandleConfig(regionalEndpoint).setConnectionPoolSize(5);

        SignatureProvider authProvider = new SignatureProvider(
                "The OCID of your tenancy goes here",
                "The OCID of your user goes here,
                "The fingerprint of your key goes here",
                "The fully qualified path to your private key file goes here",
                "The pass phrase for your PK (if any) goes here");
        config.setAuthorizationProvider(authProvider);
        config.setDefaultCompartment("The compartment your table lives in goes here.  It can be a" +
                " fully qualfied name like root.myorg.mycompartment");
        return(NoSQLHandleFactory.createNoSQLHandle(config));
    }
}
