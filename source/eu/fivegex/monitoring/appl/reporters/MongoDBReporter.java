package eu.fivegex.monitoring.appl.reporters;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoException;
import com.mongodb.ReadConcern;
import com.mongodb.ServerAddress;
import com.mongodb.WriteConcern;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import eu.reservoir.monitoring.core.AbstractReporter;

import eu.reservoir.monitoring.core.Measurement;
import eu.reservoir.monitoring.core.ProbeValue;
import eu.reservoir.monitoring.core.ProbeValueWithName;
import eu.reservoir.monitoring.core.Reporter;
import eu.reservoir.monitoring.core.Timestamp;
import eu.reservoir.monitoring.distribution.ConsumerMeasurementWithMetadataAndProbeName;
import java.util.concurrent.TimeUnit;

public class MongoDBReporter extends AbstractReporter implements Reporter {
    private final String mongoDBAddress;
    private final int mongoDBPort;
    private final String mongoDBName;
    private final String mongoDBCollectionName;
    
    private MongoDatabase db;
    private MongoClient mongoClient;
    private MongoCollection<Document> collection;

    
    
    public MongoDBReporter(String address, String port, String dbName, String collectionName) throws ReporterException {
        this(address, Integer.valueOf(port), dbName, collectionName);
    }
    
    public MongoDBReporter(String address, int port, String dbName, String collectionName) throws ReporterException {
        super("mongoDB-reporter");
        this.mongoDBAddress = address;
        this.mongoDBPort = port;
        this.mongoDBName = dbName;
        this.mongoDBCollectionName = collectionName;
        
        this.MongoDBConnect();
    }
    
    private void MongoDBConnect() throws ReporterException { 
        System.out.println("Connecting to MongoDB Server...");

        this.mongoClient = new MongoClient(new ServerAddress(mongoDBAddress, mongoDBPort), MongoClientOptions.builder().serverSelectionTimeout(4000).build());
        try {
            this.db=mongoClient.getDatabase(mongoDBName);
            this.collection = db.getCollection(mongoDBCollectionName);
            // this should raise an exception if the above connection failed
            collection.count();
        } catch (Exception e) {
            throw new ReporterException(e);
        }
            
        System.out.println("Connected!");
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
