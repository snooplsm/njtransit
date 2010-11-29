package com.gent.mp;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;

import au.com.bytecode.opencsv.CSVReader;

/**
 * Hello world!
 * 
 */
public class App {

	private static DateFormat local = DateFormat.getDateTimeInstance(
			DateFormat.MEDIUM, DateFormat.FULL);
	private static DateFormat gmt = DateFormat.getDateTimeInstance(
			DateFormat.MEDIUM, DateFormat.FULL);
	private static DateFormat njt = new SimpleDateFormat("yyyyddMM");
	private static DateFormat time = new SimpleDateFormat("yyyy-dd-MM kk:mm:ss");

	static {
		gmt.setTimeZone(TimeZone.getTimeZone("GMT"));
		local.setTimeZone(TimeZone.getTimeZone("America/New_York"));
		njt.setTimeZone(TimeZone.getTimeZone("America/New_York"));
		time.setTimeZone(TimeZone.getTimeZone("America/New_York"));

	}

	private static Long gmt(Date date) {
		long msFromEpochGmt = date.getTime();
		int offsetFromUTC = TimeZone.getTimeZone("America/New_York").getOffset(
				msFromEpochGmt);
		Calendar gmtCal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		gmtCal.setTime(date);
		gmtCal.add(Calendar.MILLISECOND, offsetFromUTC);
		return gmtCal.getTimeInMillis()/1000;
	}

	public static void main(String[] args) throws Exception {
		HttpClient c = new HttpClient();
		PostMethod m = new PostMethod(
				"https://www.njtransit.com/mt/mt_servlet.srv?hdnPageAction=MTDevLoginSubmitTo");
		m.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
		m.setRequestEntity(new StringRequestEntity("userName="
				+ System.getProperty("username") + "&password="
				+ System.getProperty("password"),
				"application/x-www-form-urlencoded", "utf-8"));
		c.executeMethod(m);
		System.out.println(m.getStatusCode());
		m.releaseConnection();
		GetMethod g = new GetMethod(
				"https://www.njtransit.com/mt/mt_servlet.srv?hdnPageAction=MTDevResourceDownloadTo&Category=rail");
		File railData = new File(System.getProperty("zipDestination"));
		g.addRequestHeader("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_6_4; en-us) AppleWebKit/533.16 (KHTML, like Gecko) Version/5.0 Safari/533.16");
		c.executeMethod(g);
		if(railData.exists()) {
			railData.delete();
		}
		if(railData.getParentFile()!=null && !railData.getParentFile().exists()) {
			railData.getParentFile().mkdirs();
		}
		FileOutputStream fos = new FileOutputStream(railData);
		BufferedOutputStream bos = new BufferedOutputStream(fos);
		byte[] mybites = new byte[1024];
		int read;
		while ((read = g.getResponseBodyAsStream().read(mybites)) != -1) {
			bos.write(mybites,0,read);
		}
		bos.close();
		
		File file = new File(railData.getParent(),"gtfs");
		file.delete();
		file.mkdirs();
		ZipInputStream zis = new ZipInputStream(new FileInputStream(railData));
		ZipEntry entry = zis.getNextEntry();
		while(entry!=null) {
			String entryName = entry.getName();
            FileOutputStream fileoutputstream;
            File newFile = new File(railData.getParent(),"gtfs/"+entryName);
            String directory = newFile.getParent();
            
            if(directory == null)
            {
                if(newFile.isDirectory())
                    break;
            }
            
            fileoutputstream = new FileOutputStream(newFile);             
            System.out.println(newFile);
            while ((read = zis.read(mybites, 0, mybites.length)) > -1)
                fileoutputstream.write(mybites, 0, read);

            fileoutputstream.close(); 
            zis.closeEntry();
            entry = zis.getNextEntry();
		}

		if (g.getResponseHeader("Last-Modified") != null) {
			railData.setLastModified(local.parse(
					g.getResponseHeader("Last-Modified").getValue()).getTime());
		}

		InputStream orig = new FileInputStream(System.getProperty("sqlite"));
		File database = new File(railData.getParent(),"sqlite");
		File newDB = new File(database,"database.sqlite");
		database.delete();
		database.mkdirs();
		FileOutputStream out = new FileOutputStream(newDB);
		while ((read = orig.read(mybites)) != -1) {
			out.write(mybites,0,read);
		}
		out.close();

		FileInputStream in = new FileInputStream(newDB);

		Class.forName("org.sqlite.JDBC");
		Connection conn = DriverManager.getConnection("jdbc:sqlite:"
				+ newDB.getAbsolutePath());
		Statement stat = conn.createStatement();
		String[] creates = new String[] {
				"create table if not exists trips(id int, route_id int, service_id varchar(100), headsign varchar(255), direction int, block_id varchar(255))",
				"create table if not exists stops(id int, name varchar(255), desc varchar(255), lat real, lon real, zone_id)",
				"create table if not exists stop_times(trip_id int, arrival int, departure int, stop_id int, sequence int, pickup_type int, drop_off_type int)",
				"create table if not exists routes(id int, agency_id varchar(100), short_name varchar(255), long_name varchar(255), route_type int, timezone varchar(100))",
				"create table if not exists calendar(service_id varchar(100), monday int, tuesday int, wednesday int, thursday int, friday int, saturday int, sunday int, start int, end int)",
				// agency_id,agency_name,agency_url,agency_timezone
				"create table if not exists calendar_dates(service_id varchar(100), calendar_date int, exception_type int)",
				"create table if not exists agency(id varchar(100), name varchar(255), url varchar(255), timezone varchar(100))",
				"CREATE TABLE if not exists android_metadata (locale TEXT DEFAULT 'en_US')",
				"INSERT INTO android_metadata VALUES ('en_US')",
				"create index trip_index on stop_times(trip_id)",
				"create index stop_index on stop_times(stop_id)",
				"create index sequence_index on stop_times(sequence)",
				"create index departure_index on stop_times(departure)"};
		
		for (String createTable : creates) {
			stat.executeUpdate(createTable);
		}

		final TransactionManager manager = new TransactionManager(conn);

		loadPartitioned(manager, "stop_times", new ContentValuesProvider() {

			@Override
			public List<List<Object>> getContentValues(Map<String,Integer> headerToPos, CSVReader reader)
					throws IOException {
				List<List<Object>> values = new ArrayList<List<Object>>();
				String[] nextLine;
				while ((nextLine = reader.readNext()) != null) {
					// trip_id,arrival_time,departure_time,stop_id,stop_sequence,pickup_type,drop_off_type
					List<Object> o = new ArrayList<Object>();
					o.add(nextLine[headerToPos.get("trip_id")]);
					Calendar c = Calendar.getInstance();
					if(nextLine[3].equals("148")) {
						for(int i = 0; i < nextLine.length; i++) {
							System.out.print(nextLine[i] + " ");
						}
						System.out.print("\n");
					} else {
						//continue;
					}
					try {
						if (nextLine[headerToPos.get("arrival_time")].trim().length() != 0) {
							c.setTime(time.parse("1970-01-01 " + nextLine[headerToPos.get("arrival_time")]));
							//System.out.println(c.get(Calendar.HOUR_OF_DAY)+":"+c.get(Calendar.MINUTE));
							o.add(gmt(time.parse("1970-01-01 " + nextLine[headerToPos.get("arrival_time")])));
						}
						if (nextLine[headerToPos.get("departure_time")].trim().length() != 0) {
							c.setTime(time.parse("1970-01-01 " + nextLine[headerToPos.get("departure_time")]));
							//System.out.println(c.get(Calendar.HOUR_OF_DAY)+":"+c.get(Calendar.MINUTE));
							o.add(gmt(time.parse("1970-01-01 " + nextLine[headerToPos.get("departure_time")])));
						}
					} catch (Exception e) {
						e.printStackTrace();
						throw new RuntimeException(e);
					}

					o.add(nextLine[3]);					
					o.add(nextLine[4]);
					o.add(nextLine[5]);
					o.add(nextLine[6]);
					values.add(o);
				}
				return values;
			}

			@Override
			public String getInsertString() {
				return "insert into stop_times (trip_id,arrival,departure,stop_id,sequence,pickup_type,drop_off_type) values (?,?,?,?,?,?,?)";
			}

		});

		loadPartitioned(manager, "trips", new ContentValuesProvider() {

			@Override
			public List<List<Object>> getContentValues(Map<String,Integer> headerToPos,CSVReader reader)
					throws IOException {
				List<List<Object>> values = new ArrayList<List<Object>>();
				String[] nextLine;
				while ((nextLine = reader.readNext()) != null) {
					// trip_id,arrival_time,departure_time,stop_id,stop_sequence,pickup_type,drop_off_type
					List<Object> o = new ArrayList<Object>();
					o.add(nextLine[headerToPos.get("route_id")]);
					o.add(nextLine[headerToPos.get("service_id")]);
					o.add(nextLine[headerToPos.get("trip_id")]);
					o.add(nextLine[headerToPos.get("trip_headsign")]);
					o.add(nextLine[headerToPos.get("direction_id")]);
					o.add(nextLine[headerToPos.get("block_id")]);
					values.add(o);
				}
				return values;
			}

			@Override
			public String getInsertString() {
				return "insert into trips (route_id,service_id,id,headsign,direction,block_id) values (?,?,?,?,?,?)";
			}

		});

		loadPartitioned(manager, "stops", new ContentValuesProvider() {

			@Override
			public List<List<Object>> getContentValues(Map<String,Integer> headerToPos,CSVReader reader)
					throws IOException {
				List<List<Object>> values = new ArrayList<List<Object>>();
				String[] nextLine;
				while ((nextLine = reader.readNext()) != null) {
					// trip_id,arrival_time,departure_time,stop_id,stop_sequence,pickup_type,drop_off_type
					List<Object> o = new ArrayList<Object>();
					o.add(nextLine[headerToPos.get("stop_id")]);
					o.add(nextLine[headerToPos.get("stop_name")]);
					o.add(nextLine[headerToPos.get("stop_desc")]);
					o.add(nextLine[headerToPos.get("stop_lat")]);
					o.add(nextLine[headerToPos.get("stop_lon")]);
					o.add(nextLine[headerToPos.get("zone_id")]);
					values.add(o);
				}
				return values;
			}

			@Override
			public String getInsertString() {
				// stop_id,stop_name,stop_desc,stop_lat,stop_lon,zone_id
				return "insert into stops (id,name,desc,lat,lon,zone_id) values (?,?,?,?,?,?)";
			}

		});

		loadPartitioned(manager, "calendar", new ContentValuesProvider() {

			@Override
			public List<List<Object>> getContentValues(Map<String,Integer> headerToPos,CSVReader reader)
					throws IOException {
				List<List<Object>> values = new ArrayList<List<Object>>();
				String[] nextLine;
				while ((nextLine = reader.readNext()) != null) {
					List<Object> o = new ArrayList<Object>();
					o.add(nextLine[headerToPos.get("service_id")]);//service_id
					o.add(nextLine[headerToPos.get("monday")]);//monday
					o.add(nextLine[headerToPos.get("tuesday")]);//tue
					o.add(nextLine[headerToPos.get("wednesday")]);//wed
					o.add(nextLine[headerToPos.get("thursday")]);//thurs
					o.add(nextLine[headerToPos.get("friday")]);//fri
					o.add(nextLine[headerToPos.get("saturday")]);//sat
					o.add(nextLine[headerToPos.get("sunday")]);//sun
					try {
						o.add(gmt(njt.parse(nextLine[8])));
					} catch (ParseException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					try {
						o.add(gmt(njt.parse(nextLine[9])));
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					values.add(o);
				}
				return values;
			}

			@Override
			public String getInsertString() {
				// service_id,monday,tuesday,wednesday,thursday,friday,saturday,sunday,start_date,end_date
				return "insert into calendar (service_id,monday,tuesday,wednesday,thursday,friday, saturday, sunday, start, end) values (?,?,?,?,?,?,?,?,?,?)";
			}

		});

		loadPartitioned(manager, "calendar_dates", new ContentValuesProvider() {

			@Override
			public List<List<Object>> getContentValues(Map<String,Integer> headerToPos,CSVReader reader)
					throws IOException {
				reader.readNext();
				List<List<Object>> values = new ArrayList<List<Object>>();
				String[] nextLine;
				while ((nextLine = reader.readNext()) != null) {
					// trip_id,arrival_time,departure_time,stop_id,stop_sequence,pickup_type,drop_off_type
					List<Object> o = new ArrayList<Object>();
					o.add(nextLine[headerToPos.get("service_id")]);
					String start = nextLine[headerToPos.get("date")];
					try {
						o.add(gmt(njt.parse(start)));
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					o.add(nextLine[headerToPos.get("exception_type")]);
					values.add(o);
				}
				return values;
			}

			@Override
			public String getInsertString() {
				// service_id,date,exception_type
				return "insert into calendar_dates (service_id,calendar_date,exception_type) values (?,?,?)";
			}

		});

		loadPartitioned(manager, "routes", new ContentValuesProvider() {

			@Override
			public List<List<Object>> getContentValues(Map<String,Integer> headerToPos,CSVReader reader)
					throws IOException {
				List<List<Object>> values = new ArrayList<List<Object>>();
				String[] nextLine;
				while ((nextLine = reader.readNext()) != null) {
					// trip_id,arrival_time,departure_time,stop_id,stop_sequence,pickup_type,drop_off_type
					List<Object> o = new ArrayList<Object>();
					o.add(nextLine[headerToPos.get("route_id")]);
					o.add(nextLine[headerToPos.get("agency_id")]);
					o.add(nextLine[headerToPos.get("route_short_name")]);
					o.add(nextLine[headerToPos.get("route_long_name")]);
					o.add(nextLine[headerToPos.get("route_type")]);
					values.add(o);
				}
				return values;
			}

			@Override
			public String getInsertString() {
				// route_id,agency_id,route_short_name,route_long_name,route_type
				return "insert into routes (id,agency_id,short_name,long_name,route_type) values (?,?,?,?,?)";
			}

		});

		loadPartitioned(manager, "agency", new ContentValuesProvider() {

			@Override
			public List<List<Object>> getContentValues(Map<String,Integer> headerToPos,CSVReader reader)
					throws IOException {
				List<List<Object>> values = new ArrayList<List<Object>>();
				String[] nextLine;
				String lastTimeZone = null;
				while ((nextLine = reader.readNext()) != null) {
					// trip_id,arrival_time,departure_time,stop_id,stop_sequence,pickup_type,drop_off_type
					String timezone = nextLine[headerToPos.get("agency_timezone")];
					if(lastTimeZone==null && timezone!=null) {
						lastTimeZone = timezone;
					}
					if(!lastTimeZone.equals(timezone)) {
						throw new RuntimeException("Weird case, more than 1 timezone, app not able to handle this.");
					}
					List<Object> o = new ArrayList<Object>();
					o.add(nextLine[headerToPos.get("agency_id")]);
					o.add(nextLine[headerToPos.get("agency_name")]);
					o.add(nextLine[headerToPos.get("agency_url")]);
					
					o.add(timezone);					
					values.add(o);
				}
				return values;
			}

			@Override
			public String getInsertString() {
				// agency_id,agency_name,agency_url
				return "insert into agency (id,name,url,timezone) values (?,?,?,?)";
			}

		});

		MessageDigest d = MessageDigest.getInstance("SHA-1");
		
		while ((read = in.read(mybites)) != -1) {
			d.update(mybites, 0, read);
		}
		out = new FileOutputStream(System.getProperty("destination") + ".sha");
		out.write(convertToHex(d.digest()).getBytes());
		out.close();
	}

	private static String convertToHex(byte[] data) {
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < data.length; i++) {
			int halfbyte = (data[i] >>> 4) & 0x0F;
			int two_halfs = 0;
			do {
				if ((0 <= halfbyte) && (halfbyte <= 9))
					buf.append((char) ('0' + halfbyte));
				else
					buf.append((char) ('a' + (halfbyte - 10)));
				halfbyte = data[i] & 0x0F;
			} while (two_halfs++ < 1);
		}
		return buf.toString();
	}

	private static void loadPartitioned(TransactionManager tm,
			final String tableName, final ContentValuesProvider valuesProvider)
			throws SQLException {
		load(tm, tableName, valuesProvider);
	}

	private static void load(TransactionManager tm, final String tableName,
			final ContentValuesProvider valuesProvider) throws SQLException {
		tm.exec(new Transactional() {
			@Override
			public void work(Connection conn) {
				InputStream input = null;
				CSVReader reader = null;
				try {
					final String fileName = tableName + ".txt";
					File file = new File(new File(System.getProperty("zipDestination")).getParent()+"/gtfs/"+fileName);
					if(!file.exists()) {
						System.out.println("No "+tableName+".txt, thats ok just know it");
						return;
					}
					input = new FileInputStream(file);
					reader = new CSVReader(new InputStreamReader(input));
					Map<String,Integer> headerToPos = new HashMap<String,Integer>();
					int i = 0;
					for(String header : reader.readNext()) {
						headerToPos.put(header, i++);
					}
					List<List<Object>> values = valuesProvider
							.getContentValues(headerToPos, reader);
					PreparedStatement s = conn.prepareStatement(valuesProvider
							.getInsertString());
					for (List<Object> cv : values) {
						for (i = 0; i < cv.size(); i++) {
							s.setObject(i + 1, cv.get(i));
						}
						s.addBatch();
					}
					System.out.println(s.executeBatch().length);
				} catch (Throwable t) {
					t.printStackTrace();
					throw new RuntimeException(t);
				} finally {
					try {
						if(input!=null) {
							input.close();
						}
						if(reader!=null) {
							reader.close();
						}
					} catch (Throwable t) {
						t.printStackTrace();
						throw new RuntimeException(t);
					}
				}
			}
		});
	}

}
