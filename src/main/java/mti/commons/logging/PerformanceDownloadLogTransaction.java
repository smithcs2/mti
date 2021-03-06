package mti.commons.logging;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.UUID;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

public class PerformanceDownloadLogTransaction {
	
    private Logger log;

	 private String domain;
	 
	 private String startTime;
	 
	 private String endTime;
	 
	 private long elapsedTime;
	
	 private String userId;
	
	 private String serviceEndpoint;
	
	 private UUID transactionId;
	 
	 private long elapseStart;
	 
	 private long elapseEnd;
	 
	 private String fileName;
	 
	 public PerformanceDownloadLogTransaction(Logger log, String domain, String userId, String serviceEndpoint, String fileName)
			{
			transactionId = UUID.randomUUID();
			this.log = log;
			this.domain = domain;
			this.userId = userId;
			this.serviceEndpoint = serviceEndpoint;						
			this.fileName = fileName;
			
			}
	 
	    public void beginPerf() {
	    	startTime = getTimeStamp();
	    	elapseStart = System.currentTimeMillis();
	    }

	    public void endPerf() {
	        //Generate timestamp and log info
	    	endTime = getTimeStamp();
	    	elapseEnd = System.currentTimeMillis();
	    	elapsedTime = (elapseEnd - elapseStart);
	    	StringBuffer sb = new StringBuffer();
			sb.append(
					",");
	    	sb.append("XID=" + transactionId);
	    	sb.append(",");
	    	sb.append(domain);
	    	sb.append(",");
	    	sb.append(startTime);
	    	sb.append(",");
	    	sb.append(endTime);
	    	sb.append(",");
	    	sb.append(elapsedTime);
	    	sb.append(",");
	    	sb.append(userId);
	    	sb.append(",");
	    	sb.append(serviceEndpoint);
	    	sb.append(",");
	    	sb.append(fileName);
	    	log.log(Level.forName("PERFORMANCE", 650), sb.toString());
	    }

		private String getTimeStamp() {
		    TimeZone tz = TimeZone.getTimeZone("UTC");
		    DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		    df.setTimeZone(tz);
		    return df.format(new Date());
		}

}
