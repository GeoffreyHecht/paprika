package paprika.neo4jBolt;

import java.net.InetAddress;

import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.exceptions.ServiceUnavailableException;

public class DriverBolt {
	
	private static String port="7687";
	private static String user="neo4j";
	private static String pwd="paprika";

	
	private static Driver driver = GraphDatabase.driver("bolt://" + getHostName() + ":"+port,
			AuthTokens.basic(user, pwd));
	
	private DriverBolt(){
	}
	

	/**
	 * Prend le nom du container neo4j-praprika et renvoie son adresse.
	 * 
	 * @return
	 */
	private static String getHostName() {
		try {
			String str = InetAddress.getByName("neo4j-paprika").getHostAddress();
			return str;
		} catch (final Exception e) {
			return "localhost";
		}
	}
	public static void setValue(String port,String user,String pwd){
		DriverBolt.port=port;
		DriverBolt.user=user;
		DriverBolt.pwd=pwd;
	}

	public static Session getSession() {
		Session session = null;

		try {
			session = driver.session();
		} catch (ServiceUnavailableException e) {
			driver.close();
			driver = GraphDatabase.driver("bolt://" + getHostName() + ":7687", AuthTokens.basic("neo4j", "paprika"));
			session = driver.session();
		}
		return session;
	}
	
}
