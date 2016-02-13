package org.cyclopsgroup.jmxterm.cmd;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.management.JMException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.commons.collections.map.ListOrderedMap;
import org.cyclopsgroup.jcli.annotation.Cli;
import org.cyclopsgroup.jmxterm.Command;
import org.cyclopsgroup.jmxterm.Session;
import org.cyclopsgroup.jmxterm.io.ValueOutputFormat;

/**
 * Get value of MBean attribute(s)
 *
 * @author <a href="mailto:jiaqi.guo@gmail.com">Jiaqi Guo</a>
 */
@Cli(name = "c3p0connections", description = "Display Liferay cache statuses", note = "* stands for all attributes. eg. get Attribute1 Attribute2 or get *")
public class C3P0ConnectionsCommand extends Command {
	private boolean singleLine = false;

	private String delimiter = "";

	private boolean simpleFormat = true;

	public static List<String> getC3P0Beans(Session session, String domainName)
			throws MalformedObjectNameException, IOException {
		ObjectName queryName = null;
		if (domainName != null) {
			queryName = new ObjectName("com.mchange.v2.c3p0:*");
		}
		Set<ObjectName> names = session.getConnection().getServerConnection().queryNames(queryName, null);
		List<String> results = new ArrayList<String>(names.size());
		for (ObjectName name : names) {
			String canonicalName = name.getCanonicalName();

			if (canonicalName.endsWith("type=PooledDataSource")) {
				results.add(name.getCanonicalName());
			}

		}

		return results;
	}

	@SuppressWarnings("unchecked")
	private void displayPoolInformation() throws IOException, JMException {
		Session session = getSession();
		
		String domain = "com.mchange.v2.c3p0";
		
		String[] fields = new String[] {"bean","minPoolSize","maxPoolSize", "numHelperThreads", "numConnections", "numBusyConnections", "numIdleConnections"};
		
		List<String[]> stringsList = new ArrayList<String[]>();
		for ( String bean : getC3P0Beans(session, domain) ) {
			stringsList.add(getPoolInformation(session, domain, bean, fields));
		}
		Collections.sort(stringsList, new Comparator<String[]>() {

			@Override
			public int compare(String[] o1, String[] o2) {
				for (int i=0; i<2 ; i++) {
					int result = o1[i].compareTo(o2[i]);
					if (result!=0) {
						return result;
					}
				}
				return 0;
			}
		});
		
		//String format = "%s\t%s\t%s\t%s\t%s\t";
		
		session.output.print(String.format("%1$-130s ", fields[0]));
		
		for (int i=1; i < fields.length ; i++ ) {
			session.output.print(String.format("%1$" + fields[i].length() + "s ", fields[i]));
		}
		session.output.println("");
		
		for(String[] s : stringsList) {
			session.output.print(String.format("%1$-130s ", s[0]));
			for (int i=1; i < s.length ; i++ ) {
				session.output.print(String.format("%1$" + fields[i].length() + "s ", s[i]));
			}
			session.output.println("");
		}
	}
	
	private String[] getPoolInformation(Session session,String domain, String bean, String[] fields) throws IOException, JMException {
		
		String[] cachesStats = new String[fields.length];
		String beanName = BeanCommand.getBeanName(bean, domain, session);
		ObjectName name = new ObjectName(beanName);
		boolean showDescription = false;
		boolean showQuotationMarks = false;
		
		ValueOutputFormat format = new ValueOutputFormat(2, showDescription, showQuotationMarks);
		
		MBeanServerConnection con = session.getConnection().getServerConnection();
		
		cachesStats[0] = bean;
		
		for (int i=1 ; i < fields.length ; i++) {
			Object object = con.getAttribute(name, fields[i]);
		
			cachesStats[i] =  String.valueOf(object);
		}
		
		return cachesStats;
/*		
		for (MBeanAttributeInfo ai : ais) {
			attributeNames.put(ai.getName(), ai);
		}
		


		
		for (Map.Entry<String, MBeanAttributeInfo> entry : attributeNames.entrySet()) {
			String attributeName = entry.getKey();
			MBeanAttributeInfo i = entry.getValue();
			if (i.isReadable()) {
				Object result = con.getAttribute(name, attributeName);
		
				if (simpleFormat) {
					format.printValue(session.output, result);
				} else {
					format.printExpression(session.output, attributeName, result, i.getDescription());
				}
						
				
				session.output.
				session.output.print(delimiter);
				if (!singleLine) {
					session.output.println("");
				}
			} else {
				session.output.printMessage(i.getName() + " is not readable");
			}
		}	
		*/
	}

	
	
	/**
	 * @inheritDoc
	 */
	@Override
	public void execute() throws JMException, IOException {
		displayPoolInformation();
	}
}
