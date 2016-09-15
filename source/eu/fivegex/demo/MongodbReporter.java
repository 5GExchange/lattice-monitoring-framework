package eu.fivegex.demo;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;

import eu.reservoir.monitoring.core.Measurement;
import eu.reservoir.monitoring.core.Reporter;
import eu.reservoir.monitoring.core.Timestamp;
import eu.reservoir.monitoring.distribution.ConsumerMeasurementWithMetadataAndProbeName;

public class MongodbReporter implements Reporter {
    private final MongoDatabase db;
    private final MongoClient mongoClient;
    private final String mongoDBAddress;
    private final int mongoDBPort;
    //private MongoCollection<Document> collection;

    
    public MongodbReporter(String address, int port, String dbName) {
        this.mongoDBAddress = address;
        this.mongoDBPort = port;
        System.out.println("connecting to MongoDB Server...");
        this.mongoClient = new MongoClient(mongoDBAddress, mongoDBPort);
        System.out.println("connected!");
        this.db=mongoClient.getDatabase(dbName);
    }
    
    @Override
    public void report(Measurement m) {
            /*
            System.err.println("connect to mongoDB..................");
            mongoClient = new MongoClient( "10.100.0.10" , 27017 );
            this.mongoClient = mongoClient;
            System.out.println("connected");
            db = mongoClient.getDatabase("test");
            */
            String probeName = ((ConsumerMeasurementWithMetadataAndProbeName)m).getProbeName();
            
            Timestamp t = ((ConsumerMeasurementWithMetadataAndProbeName)m).getTimestamp();
            
            System.out.println("************"+probeName+"**************");

            // I think we should use the measurement timestamp rather than the current time -> t.toString rather than System.currentTimeMillis()
            Bson doc1 = new Document("$set",new Document(probeName+"."+String.valueOf(System.currentTimeMillis()),new Document().append(probeName,m.getValues().get(0).getValue())));
            db.getCollection("cs").updateOne(new Document("_id", m.getServiceID().getUUID().toString()), doc1);

            //this should not be needed any more as the MongodBConsumer has now also a PrintReporter
            System.out.println("------------------------------------------------------------------------------");
            System.out.println("service Id:" +m.getServiceID()+"\t values: "+m.getValues().get(0).getValue() );
            System.out.println("------------------------------------------------------------------------------");

    }


}
