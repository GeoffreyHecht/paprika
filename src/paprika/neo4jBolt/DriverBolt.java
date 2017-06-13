package paprika.neo4jBolt;

import java.net.InetAddress;

import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.exceptions.ServiceUnavailableException;

public class DriverBolt {

	private static String port = "7687";
	private static String user = "neo4j";
	private static String pwd = "paprika";
	private static String containerDocker = "neo4j-paprika";
	private static String hostname = null;

	private static Driver driver = null;

	private DriverBolt() {
	}

	/**
	 * Take the docker container name, the localhost or just a hostname.
	 * 
	 * @return
	 */
	private static String getHostName() {
		if (hostname != null)
			return hostname;

		try {
			String str = InetAddress.getByName(containerDocker).getHostAddress();
			return str;
		} catch (final Exception e) {
			return "localhost";
		}
	}

	/**
	 * Equivalent to a constructor
	 * 
	 * @param port
	 * @param user
	 * @param pwd
	 */
	public static void setValue(String port, String user, String pwd) {
		if (port != null)
			DriverBolt.port = port;
		if (user != null)
			DriverBolt.user = user;
		if (pwd != null)
			DriverBolt.pwd = pwd;
	}

	/**
	 * Put a dockerContainer and delete the hostname.
	 * 
	 * @param nameContainer
	 */
	public static void setDockerNeo4j(String nameContainer) {
		if (nameContainer != null)
			DriverBolt.containerDocker = nameContainer;
		DriverBolt.hostname = null;
	}
	/**
	 * Put a hostname and delete the name container.
	 * 
	 * @param nameContainer
	 */
	public static void setHostName(String hostname) {
		if (hostname != null)
			DriverBolt.hostname = hostname;
		DriverBolt.containerDocker = null;
	}

	public static void updateDriver() {
		if (DriverBolt.driver != null)
			DriverBolt.driver.close();
		DriverBolt.driver = GraphDatabase.driver("bolt://" + getHostName() + ":" + port, AuthTokens.basic(user, pwd));
	}

	public static Session getSession() {
		Session session = null;
		if (DriverBolt.driver == null)
			DriverBolt.updateDriver();
		try {
			session = DriverBolt.driver.session();
		} catch (ServiceUnavailableException e) {
			DriverBolt.updateDriver();
			session = DriverBolt.driver.session();
		}
		return session;
	}

}
