package eu.fivegex.demo.mongodb;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import eu.reservoir.monitoring.core.AbstractReporter;

import eu.reservoir.monitoring.core.Measurement;
import eu.reservoir.monitoring.core.ProbeValue;
import eu.reservoir.monitoring.core.ProbeValueWithName;
import eu.reservoir.monitoring.core.Reporter;
import eu.reservoir.monitoring.core.Timestamp;
import eu.reservoir.monitoring.distribution.ConsumerMeasurementWithMetadataAndProbeName;

public class MongodbReporter extends AbstractReporter implements Reporter {
    private final MongoDatabase db;
    private final MongoClient mongoClient;
    private final String mongoDBAddress;
    private final int mongoDBPort;
    private final MongoCollection<Document> collection;

    
    public MongodbReporter(String address, int port, String dbName, String collectionName) {
        super("mongoDB-reporter");
        this.mongoDBAddress = address;
        this.mongoDBPort = port;
        System.out.println("connecting to MongoDB Server...");
        this.mongoClient = new MongoClient(mongoDBAddress, mongoDBPort);
        System.out.println("connected!");
        this.db=mongoClient.getDatabase(dbName);
        this.collection = db.getCollection(collectionName);
    }
    
    @Override
    public void report(Measurement m) {
            String probeName = ((ConsumerMeasurementWithMetadataAndProbeName)m).getProbeName();
            
            Timestamp t = ((ConsumerMeasurementWithMetadataAndProbeName)m).getTimestamp();
            
            Document attributes = new Document();
            for (ProbeValue attribute : m.getValues()) {
                attributes.append(((ProbeValueWithName)attribute).getName(), attribute.getValue());
            }
            
            Bson doc1 = new Document("$set",
                                    new Document(probeName + "." + t.toString(),       
                                    // first arg was probeName      
                                    // new Document().append(((ProbeValueWithName)m.getValues().get(0)).getName(), m.getValues().get(0).getValue())));
                                    attributes));
            
            collection.updateOne(new Document("_id", m.getServiceID().toString()), doc1);
    }


}
