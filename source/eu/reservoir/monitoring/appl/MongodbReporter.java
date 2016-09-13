package eu.reservoir.monitoring.appl;

import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import eu.reservoir.monitoring.core.Measurement;
import eu.reservoir.monitoring.core.Reporter;
import eu.reservoir.monitoring.distribution.ConsumerMeasurementWithMetadataAndProbeName;

public class MongodbReporter implements Reporter {
	private static MongoDatabase db;
	private static MongoClient mongoClient;
	private static MongoCollection<Document> collection;

	@Override
	public void report(Measurement m) {
		System.err.println("connect to mongoDB..................");
		mongoClient = new MongoClient( "172.16.1.5" , 27017 );
		this.mongoClient = mongoClient;
		System.out.println("connected");
		db = mongoClient.getDatabase("test");
		String probeName = ((ConsumerMeasurementWithMetadataAndProbeName)m).getProbeName();
		System.out.println("************"+probeName+"**************");
		
		Bson doc1 = new Document("$set",new Document(probeName+"."+String.valueOf(System.currentTimeMillis()),new Document().append(probeName, m.getValues().get(0).getValue())));
		db.getCollection("cs").updateOne(new Document("_id", m.getServiceID().getUUID().toString()), doc1);
		
		System.out.println("------------------------------------------------------------------------------");
		System.out.println("service Id:" +m.getServiceID()+"\t values: "+m.getValues().get(0).getValue() );
		System.out.println("------------------------------------------------------------------------------");

	}


}
