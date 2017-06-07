package org.cyclopsgroup.jmxterm.cmd;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.management.AttributeNotFoundException;
import javax.management.JMException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.commons.collections.map.ListOrderedMap;
import org.cyclopsgroup.jcli.annotation.Argument;
import org.cyclopsgroup.jcli.annotation.Cli;
import org.cyclopsgroup.jcli.annotation.Option;
import org.cyclopsgroup.jmxterm.Command;
import org.cyclopsgroup.jmxterm.Session;
import org.cyclopsgroup.jmxterm.io.ValueOutputFormat;

/**
 * Get value of MBean attribute(s)
 *
 * @author <a href="mailto:jiaqi.guo@gmail.com">Jiaqi Guo</a>
 */
@Cli(name = "liferaycaches", description = "Display Liferay cache statuses", note = "* stands for all attributes. eg. get Attribute1 Attribute2 or get *")
public class LiferayCachesCommand extends Command {
	private boolean singleLine = false;

	private String delimiter = "";

	private boolean simpleFormat = true;
	
	private String format = "list";
	
	private String serverName = "serverName";	

	private int misspercentage = 0;
	
	public static List<String> getLiferayStatisticBeans(Session session, String domainName)
			throws MalformedObjectNameException, IOException {
		ObjectName queryName = null;
		if (domainName != null) {
			queryName = new ObjectName("net.sf.ehcache:*");
		}
		Set<ObjectName> names = session.getConnection().getServerConnection().queryNames(queryName, null);
		List<String> results = new ArrayList<String>(names.size());
		for (ObjectName name : names) {
			String canonicalName = name.getCanonicalName();

			if (canonicalName.endsWith("CacheStatistics")) {
				results.add(name.getCanonicalName());
			}

		}

		return results;
	}

	@SuppressWarnings("unchecked")
	private void displayLiferayCaches() throws IOException, JMException {
		Session session = getSession();
		
		String domain = "net.sf.ehcache";
		
		String[] fields = new String[] {"CacheType","Bean","ObjectCount","CacheHits","CacheMisses", "CacheMissPercentage"};
		
		List<String[]> stringsList = new ArrayList<String[]>();
		for ( String bean : getLiferayStatisticBeans(session, domain) ) {
			try {
			stringsList.add(displayCache(session, domain, bean, fields));
			} catch (AttributeNotFoundException anfe) {
				
			}
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
		
		if (this.format!=null && "sql".equals(this.format)) {
			//String format = "%s\t%s\t%s\t%s\t%s\t";
			String printformat = "INSERT INTO CacheSizes VALUES (\"%s\",\"%s\",\"%s\",%s,%s,%s,%s);";
			
			
			for(String[] s : stringsList) {
				session.output.println(String.format(printformat, this.serverName, s[0], s[1],s[2],s[3],s[4],s[5]));
			}
		}
		else {
			String printformat = "%1$-27s %2$-130s %3$12s %4$12s %5$12s %6$20s";
			
			session.output.println(String.format(printformat, fields[0], fields[1],fields[2],fields[3],fields[4], fields[5]));
			for(String[] s : stringsList) {
				
				int misspercentage = Integer.valueOf(s[5]).intValue();
				
				if (misspercentage >= this.misspercentage) {
					session.output.println(String.format(printformat,s[0], s[1],s[2],s[3],s[4],s[5]));
				}
			}			
		}
	}
	
	private String[] displayCache(Session session,String domain, String bean, String[] fields) throws IOException, JMException {
		
		String[] cachesStats = new String[fields.length];
		String beanName = BeanCommand.getBeanName(bean, domain, session);
		ObjectName name = new ObjectName(beanName);
		String tmp = bean.substring("net.sf.ehcache:CacheManager=".length());
		String cacheType = tmp.substring(0, tmp.indexOf(','));
		String cacheBean = tmp.substring(tmp.indexOf(",name=") + 6);
		cacheBean = cacheBean.substring(0, cacheBean.length() - ",type=CacheStatistics".length());
		boolean showDescription = false;
		boolean showQuotationMarks = false;
		
		ValueOutputFormat format = new ValueOutputFormat(2, showDescription, showQuotationMarks);
		
		MBeanServerConnection con = session.getConnection().getServerConnection();
		
		cachesStats[0] = cacheType;
		cachesStats[1] = cacheBean;
		
		for (int i=2 ; i < fields.length ; i++) {
			Object object = con.getAttribute(name, fields[i]);
			if (object instanceof Double) {
				double value = ((Double)object).doubleValue();
				cachesStats[i] =  String.valueOf(Math.round(value * 100));
				
			}
			else {
				cachesStats[i] =  String.valueOf(object);
			}
			
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
	     * @param report True to output result line by line as report
	     */
	    @Option( name = "f", longName = "format", description = "Format <list|sql>", defaultValue="list")
	    public final void setFormat( String format )
	    {
	        this.format = format;
	    }
    
   /**
    * Set bean option
    *
    * @param bean Bean to set
    */
   @Option( name = "s", longName = "server", description = "Servername for SQL printout", defaultValue="server" )
   public final void setServerName( String serverName )
   {
       this.serverName = serverName;
   }
   
   /**
    * Set bean option
    *
    * @param bean Bean to set
    */
   @Option( name = "mp", longName = "misspercentage", description = "Prints only that miss percentage is larger", defaultValue="0" )
   public final void setMissPercentage( int misspercentage )
   {
       this.misspercentage = misspercentage;
   }
	
	/**
	 * @inheritDoc
	 */
	@Override
	public void execute() throws JMException, IOException {
		displayLiferayCaches();
	}
}
