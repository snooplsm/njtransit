package com.gent.mp;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
public class DatabaseCreater {

	private static DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
	private DateFormat local = DateFormat.getDateTimeInstance(
			DateFormat.MEDIUM, DateFormat.FULL);
	private static DateFormat utc = DateFormat.getDateTimeInstance(
			DateFormat.MEDIUM, DateFormat.FULL);

	static {
		utc.setTimeZone(TimeZone.getTimeZone("UTC"));
	}

	private Long utc(Date date) {
		long result = date.getTime() / 1000;
		return result;
		// (date.getTime()+local.getTimeZone().getOffset(date.getTime())) /
		// 1000;
	}

	public static void main(String[] args) throws Exception {
		DatabaseCreater e = new DatabaseCreater(args);
		e.start();
	}

	private String gtfsUrl;

	private String loginUrl;

	private String password;

	private String username;

	private File workDB;

	private String workDir;
	
	private Map<String,Integer> stringIdToIntegerId = new HashMap<String,Integer>();
	private int last;

	private int splitKiloBytes = 50;
	private int splitBytes = 50 * 1024;
	byte[] read = new byte[5 * 1024];
	
	private String partitionsTarget;

	DatabaseCreater(String[] args) {
		for (int i = 0; i < args.length; i += 2) {
			if ("-login_url".equalsIgnoreCase(args[i])) {
				loginUrl = args[i + 1];
				continue;
			}
			if ("-gtfs".equalsIgnoreCase(args[i])) {
				gtfsUrl = args[i + 1];
				continue;
			}
			if ("-u".equalsIgnoreCase(args[i])) {
				username = args[i + 1];
				continue;
			}
			if ("-p".equalsIgnoreCase(args[i])) {
				password = args[i + 1];
				continue;
			}
			if ("-workdir".equals(args[i])) {
				workDir = args[i + 1];
				continue;
			}
			if ("-split".equals(args[i])) {
				splitKiloBytes = Integer.parseInt(args[i + 1]);
				splitBytes = splitKiloBytes * 1024;
				continue;
			}
			if("-partitions".equals(args[i])) {
				partitionsTarget = args[i+1];
				continue;
			}
		}
	}

	private void connectAndCreateGTFS() {
		if ((loginUrl == null || !gtfsUrl.toLowerCase().startsWith("http"))) {
			return;
		} else {

			HttpClient c = new HttpClient();			

			if (loginUrl != null) {
				PostMethod m = new PostMethod(loginUrl);
				m.setRequestHeader("Content-Type",
						"application/x-www-form-urlencoded");
				m.setRequestHeader("Content-Type",
						"application/x-www-form-urlencoded");
				try {
					m.setRequestEntity(new StringRequestEntity("userName="
							+ URLEncoder.encode(username, "utf-8")
							+ "&password="
							+ URLEncoder.encode(password, "utf-8"),
							"application/x-www-form-urlencoded", "utf-8"));
				} catch (UnsupportedEncodingException e1) {
					throw new RuntimeException(e1);
				}
				try {
					c.executeMethod(m);
				} catch (Exception e) {
					throw new RuntimeException(e);
				} finally {
					m.releaseConnection();
				}
			}
			GetMethod g = new GetMethod(gtfsUrl);
			File workingDirectory = new File(workDir);
			if (workingDirectory.exists()) {
				workingDirectory.delete();
			}
			if (workingDirectory.getParentFile() != null) {
				workingDirectory.getParentFile().mkdirs();
			}
			if(gtfsUrl.startsWith("http://")) {
				g
						.addRequestHeader(
								"User-Agent",
								"Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_6_4; en-us) AppleWebKit/533.16 (KHTML, like Gecko) Version/5.0 Safari/533.16");
				try {
					c.executeMethod(g);
				} catch (Exception e) {
					throw new RuntimeException(e);
				} finally {
					g.releaseConnection();
				}
			}
			File gtfsZip = getGtfsFile();
			if(gtfsUrl.startsWith("http://")) {
				FileOutputStream fos = null;
				BufferedOutputStream bos = null;
				try {
					fos = new FileOutputStream(gtfsZip);
					bos = new BufferedOutputStream(fos);
					byte[] mybites = new byte[1024];
					int read;
					while ((read = g.getResponseBodyAsStream().read(mybites)) != -1) {
						bos.write(mybites, 0, read);
					}
				} catch (Exception e) {
					throw new RuntimeException(e);
				} finally {
					try {
						bos.close();
					} catch (Exception e) {
					}
					try {
						fos.close();
					} catch (Exception e) {
					}
				}
			}
		}
	}

	private void copyBlankDatabase() {
		InputStream db = DatabaseCreater.class
				.getResourceAsStream("emptydb.sqlite");
		File database = new File(workDir, "target");
		workDB = new File(database, "database.sqlite");
		database.delete();
		database.mkdirs();
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(workDB);
			byte[] mybites = new byte[1024 * 5];
			int read;
			while ((read = db.read(mybites)) != -1) {
				out.write(mybites, 0, read);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			try {
				out.close();
			} catch (Exception e) {
			}
			try {
				db.close();
			} catch (Exception e) {
			}
		}
	}

	private void extractGtfs() {
		File gtfs = getGtfsFile();
		if (gtfs.exists() && gtfs.isDirectory()) {
			return;
		}
		File extractFolder = new File(workDir, "gtfs");
		if (extractFolder.exists()) {
			extractFolder.delete();
			System.out.println("what");
		}
		ZipInputStream zis = null;
		try {
			zis = new ZipInputStream(new FileInputStream(gtfs));
			ZipEntry entry = zis.getNextEntry();
			while (entry != null) {
				String entryName = entry.getName();
				FileOutputStream fileoutputstream = null;
				File newFile = new File(gtfs.getParent(), "gtfs/" + entryName);
				String directory = newFile.getParent();

				if (directory == null) {
					if (newFile.isDirectory())
						break;
				}
				byte[] mybites = new byte[1024 * 5];
				int read;
				try {
					fileoutputstream = new FileOutputStream(newFile);
					while ((read = zis.read(mybites, 0, mybites.length)) > -1)
						fileoutputstream.write(mybites, 0, read);
					fileoutputstream.close();
				} catch (Exception e) {

				} finally {
					try {
						fileoutputstream.close();
					} catch (Exception e) {

					}
				}
				zis.closeEntry();
				entry = zis.getNextEntry();
			}
		} catch (Exception e) {
			try {
				zis.close();
			} catch (Exception ex) {
			}
		} finally {

		}

	}

	private File getGtfsFile() {
		if (gtfsUrl.toLowerCase().startsWith("http://")) {
			return new File(workDir, "gtfs.zip");
		} else {
			return new File(gtfsUrl);
		}
	}

	private void load(TransactionManager tm, final String tableName,
			final ContentValuesProvider valuesProvider) throws SQLException {
		tm.exec(new Transactional() {
			@Override
			public void work(Connection conn) {
				InputStream input = null;
				CSVReader reader = null;
				try {
					final String fileName = tableName + ".txt";
					File file = new File(DatabaseCreater.this.getGtfsFile(),
							fileName);
					if (!file.exists()) {
						// System.out.println("No " + tableName
						// + ".txt, thats ok just know it");
						return;
					}
					input = new FileInputStream(file);
					reader = new CSVReader(new InputStreamReader(input));
					Map<String, Integer> headerToPos = new HashMap<String, Integer>();
					int i = 0;
					for (String header : reader.readNext()) {
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
					s.executeBatch();
				} catch (Throwable t) {
					t.printStackTrace();
					throw new RuntimeException(t);
				} finally {
					try {
						if (input != null) {
							input.close();
						}
						if (reader != null) {
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

	private void loadPartitioned(TransactionManager tm, final String tableName,
			final ContentValuesProvider valuesProvider) {
		try {
			load(tm, tableName, valuesProvider);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void split(InputStream stream, File name) {
		int read = 0;
		int neededTotal = this.splitBytes;
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(name);
			int neededNow = Math.min(neededTotal, this.read.length);
			while (neededNow > 0
					&& (read = stream.read(this.read, 0, neededNow)) != -1) {
				fos.write(this.read, 0, read);
				neededTotal -= read;
				neededNow = Math.min(neededTotal, this.read.length);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			try {
				fos.close();
			} catch (IOException e) {

			}
		}
	}

	private void splitDatabase() {
		final File partitionFolder;
		if(partitionsTarget!=null) {
			partitionFolder = new File(partitionsTarget);
		} else {
			partitionFolder =new File(workDir, "target/partitions");
		}
		if (partitionFolder.exists()) {
			for(File file : partitionFolder.listFiles()) {
				file.delete();
			}
		} 
		partitionFolder.mkdirs();

		char[] alphabet = new char[] { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h',
				'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't',
				'u', 'v', 'w', 'y', 'x', 'z' };
		File database = workDB;
		long length = database.length();
		int partitions = (int) Math.ceil(length / (1.0 * splitBytes));
		// FIXME: c'mon Ryan, do some math to figure this out.
		int places = 1;
		while (true) {
			if (Math.pow(alphabet.length, places) >= partitions) {
				break;
			}
			places++;
		}
		char[] partitionName = new char[places];
		for (int j = 0; j < places; j++) {
			partitionName[j] = 'a';
		}
		File db = new File(workDir, "target/database.sqlite");
		FileInputStream fis = null;
		BufferedInputStream bis = null;
		try {
			fis = new FileInputStream(db);
			bis = new BufferedInputStream(fis);
			File file = new File(partitionFolder, "database.sqlite_"
					+ new String(partitionName));
			split(bis, file);
			boolean increasePrevious = false;
			for (int h = 1; h < partitions; h++) {
				for (int i = places - 1; i > -1; i--) {
					int letter = (int) partitionName[i];
					if ((increasePrevious || i == places - 1)
							&& letter + 1 > (int) 'z') {
						partitionName[i] = 'a';
						increasePrevious = true;
					} else {
						if (increasePrevious || i == places - 1) {
							partitionName[i] = (char) ((int) partitionName[i] + 1);
						}
						break;
					}
				}
				file = new File(partitionFolder, "database.sqlite_"
						+ new String(partitionName));
				split(bis, file);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			try {
				if (fis != null)
					fis.close();
			} catch (IOException e) {
			}
			try {
				if (bis != null)
					bis.close();
			} catch (IOException e) {
			}
		}
	}
	
	private Integer stringIdToIntegerId(String id) {
		Integer k = null;
		try {
			k = Integer.parseInt(id);
		} catch (NumberFormatException e) {
			try {
				k = this.stringIdToIntegerId.get(id);
			}catch (Exception ex) {
				
			}
			if(k==null) {
				last+=1;
				k = last;
				this.stringIdToIntegerId.put(id, k);				
			}
		}
		return k;
	}

	private void populateDatabase() {
		Connection conn = null;
		try {
			Class.forName("org.sqlite.JDBC");
			conn = DriverManager.getConnection("jdbc:sqlite:"
					+ workDB.getAbsolutePath());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		Statement stat;
		try {
			stat = conn.createStatement();
		} catch (SQLException e2) {
			throw new RuntimeException(e2);
		}
		String[] creates = new String[] {
				"create table if not exists trips(id varchar(50), route_id int, service_id varchar(100), headsign varchar(255), direction int, block_id varchar(255))",
				"create table if not exists stops(id varchar(50), name varchar(255), desc varchar(255), lat real, lon real, zone_id)",
				"create table if not exists stop_times(trip_id varchar(50), arrival varchar(10), departure varchar(10), stop_id varchar(100), sequence int, pickup_type int, drop_off_type int)",
				"create table if not exists routes(id int, agency_id varchar(100), short_name varchar(255), long_name varchar(255), route_type int, timezone varchar(100))",
				"create table if not exists calendar(service_id varchar(100), monday int, tuesday int, wednesday int, thursday int, friday int, saturday int, sunday int, start int, end int)",
				// agency_id,agency_name,agency_url,agency_timezone
				"create table if not exists calendar_dates(service_id varchar(100), calendar_date varchar(10), exception_type int)",
				"create table if not exists agency(id varchar(100), name varchar(255), url varchar(255), timezone varchar(100))",
				"CREATE TABLE if not exists android_metadata (locale TEXT DEFAULT 'en_US')",
				"create table if not exists schedule(departure_id int, arrival_id int, depart varchar(10), arrive varchar(10), trip_id varchar(20))",
				"INSERT INTO android_metadata VALUES ('en_US')",
				"create index service_index on trips(service_id)",
				"create index schedule_index on schedule(departure_id,arrival_id)",
				"create index trip_index on stop_times(trip_id)",
				"create index stop_index on stop_times(stop_id)",
				//"create index stop2_index on stop_times(stop_id,trip_id)",
				"create index sequence_index on stop_times(sequence)"
				};

		try {
			for (String createTable : creates) {
				stat.executeUpdate(createTable);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		final TransactionManager manager = new TransactionManager(conn);
				

		loadPartitioned(manager, "agency", new ContentValuesProvider() {

			@Override
			public List<List<Object>> getContentValues(
					Map<String, Integer> headerToPos, CSVReader reader)
					throws IOException {
				List<List<Object>> values = new ArrayList<List<Object>>();
				String[] nextLine;
				String lastTimeZone = null;
				while ((nextLine = reader.readNext()) != null) {
					// trip_id,arrival_time,departure_time,stop_id,stop_sequence,pickup_type,drop_off_type
					String timezone = nextLine[headerToPos
							.get("agency_timezone")];
					if (lastTimeZone == null && timezone != null) {
						lastTimeZone = timezone;
					}
					if (!lastTimeZone.equals(timezone)) {
						throw new RuntimeException(
								"Weird case, more than 1 timezone, app not able to handle this.");
					}
					List<Object> o = new ArrayList<Object>();
					o.add(stringIdToIntegerId(nextLine[headerToPos.get("agency_id")]));
					o.add(nextLine[headerToPos.get("agency_name")]);
					o.add(nextLine[headerToPos.get("agency_url")]);

					o.add(timezone);
					values.add(o);
				}
				if (lastTimeZone != null) {
					local.setTimeZone(TimeZone.getTimeZone(lastTimeZone));
				}
				return values;
			}

			@Override
			public String getInsertString() {
				// agency_id,agency_name,agency_url
				return "insert into agency (id,name,url,timezone) values (?,?,?,?)";
			}

		});

		loadPartitioned(manager, "stop_times", new ContentValuesProvider() {

			@Override
			public List<List<Object>> getContentValues(
					Map<String, Integer> headerToPos, CSVReader reader)
					throws IOException {
				List<List<Object>> values = new ArrayList<List<Object>>();
				String[] nextLine;
				while ((nextLine = reader.readNext()) != null) {
					// trip_id,arrival_time,departure_time,stop_id,stop_sequence,pickup_type,drop_off_type
					List<Object> o = new ArrayList<Object>();
					o.add(stringIdToIntegerId(nextLine[headerToPos.get("trip_id")]));
					try {
						o.add(nextLine[headerToPos.get("arrival_time")]);
						o.add(nextLine[headerToPos.get("departure_time")]);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}

					o.add(stringIdToIntegerId(nextLine[headerToPos.get("stop_id")]));
					o.add(nextLine[headerToPos.get("stop_sequence")]);
					o.add(nextLine[headerToPos.get("pickup_type")]);
					o.add(nextLine[headerToPos.get("drop_off_type")]);
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
			public List<List<Object>> getContentValues(
					Map<String, Integer> headerToPos, CSVReader reader)
					throws IOException {
				List<List<Object>> values = new ArrayList<List<Object>>();
				String[] nextLine;
				while ((nextLine = reader.readNext()) != null) {
					// trip_id,arrival_time,departure_time,stop_id,stop_sequence,pickup_type,drop_off_type
					List<Object> o = new ArrayList<Object>();
					o.add(stringIdToIntegerId(nextLine[headerToPos.get("route_id")]));
					o.add(stringIdToIntegerId(nextLine[headerToPos.get("service_id")]));
					o.add(stringIdToIntegerId(nextLine[headerToPos.get("trip_id")]));
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



		loadPartitioned(manager, "calendar", new ContentValuesProvider() {

			@Override
			public List<List<Object>> getContentValues(
					Map<String, Integer> headerToPos, CSVReader reader)
					throws IOException {
				List<List<Object>> values = new ArrayList<List<Object>>();
				String[] nextLine;
				while ((nextLine = reader.readNext()) != null) {
					List<Object> o = new ArrayList<Object>();
					o.add(stringIdToIntegerId(nextLine[headerToPos.get("service_id")]));// service_id
					o.add(nextLine[headerToPos.get("monday")]);// monday
					o.add(nextLine[headerToPos.get("tuesday")]);// tue
					o.add(nextLine[headerToPos.get("wednesday")]);// wed
					o.add(nextLine[headerToPos.get("thursday")]);// thurs
					o.add(nextLine[headerToPos.get("friday")]);// fri
					o.add(nextLine[headerToPos.get("saturday")]);// sat
					o.add(nextLine[headerToPos.get("sunday")]);// sun
					try {
						o.add(utc(dateFormat.parse(nextLine[8])));
					} catch (ParseException e1) {
						throw new RuntimeException(e1);
					}
					try {
						o.add(utc(dateFormat.parse(nextLine[9])));
					} catch (ParseException e) {
						throw new RuntimeException(e);
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
			public List<List<Object>> getContentValues(
					Map<String, Integer> headerToPos, CSVReader reader)
					throws IOException {
				List<List<Object>> values = new ArrayList<List<Object>>();
				String[] nextLine;
				while ((nextLine = reader.readNext()) != null) {
					// trip_id,arrival_time,departure_time,stop_id,stop_sequence,pickup_type,drop_off_type
					List<Object> o = new ArrayList<Object>();
					o.add(stringIdToIntegerId(nextLine[headerToPos.get("service_id")]));
					String start = nextLine[headerToPos.get("date")];
					o.add(start);
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
			public List<List<Object>> getContentValues(
					Map<String, Integer> headerToPos, CSVReader reader)
					throws IOException {
				List<List<Object>> values = new ArrayList<List<Object>>();
				String[] nextLine;
				while ((nextLine = reader.readNext()) != null) {
					// trip_id,arrival_time,departure_time,stop_id,stop_sequence,pickup_type,drop_off_type
					List<Object> o = new ArrayList<Object>();
					o.add(stringIdToIntegerId(nextLine[headerToPos.get("route_id")]));
					o.add(stringIdToIntegerId(nextLine[headerToPos.get("agency_id")]));
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
		
		final Map<Integer,Integer> stopIdToTrips = new HashMap<Integer,Integer>();
		final Map<Integer,String> tripIdToName = new HashMap<Integer,String>();
		
		try {
			Statement statement = conn.createStatement();
			statement.execute("select s.stop_id, s.trip_id from stop_times s group by s.stop_id");
			ResultSet s = statement.getResultSet();
			
			while(s.next()) {
				int stopId = stringIdToIntegerId(s.getString(1));
				int tripId = stringIdToIntegerId(s.getString(2));
				stopIdToTrips.put(stopId,tripId);				
			}
			s.close();
			
			statement.execute("select r.long_name, t.id from trips t join routes r where r.id=t.route_id group by t.id");
			s = statement.getResultSet();
			while(s.next()) {
				String longName = s.getString(1);
				int tripId = s.getInt(2);
				tripIdToName.put(tripId,longName);
			}
			s.close();			
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		
		loadPartitioned(manager, "stops", new ContentValuesProvider() {

			@Override
			public List<List<Object>> getContentValues(
					Map<String, Integer> headerToPos, CSVReader reader)
					throws IOException {
				List<List<Object>> values = new ArrayList<List<Object>>();
				String[] nextLine;
				while ((nextLine = reader.readNext()) != null) {
					// trip_id,arrival_time,departure_time,stop_id,stop_sequence,pickup_type,drop_off_type
					List<Object> o = new ArrayList<Object>();
					int stopId = stringIdToIntegerId(nextLine[headerToPos.get("stop_id")]); 
					o.add(stopId);
					String stopName = nextLine[headerToPos.get("stop_name")];
					if(stopId==38291) {
						if(stopName.equals("TRENTON TRANSIT CENTER")) {
							stopName = stopName + " - RIVERLINE";
						}
					}else {
						stopName = getAlternate(stopName);
					}
					o.add(stopName);
					int tripId = stopIdToTrips.get(stopId);
					o.add(tripIdToName.get(tripId));
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

	}

	public void start() {
		connectAndCreateGTFS();
		extractGtfs();
		copyBlankDatabase();
		populateDatabase();
		splitDatabase();
	}

	private static Map<String, String> alternates = new HashMap<String, String>();

	static {

		// NJTRANSIT
		alternates.put("FRANK R LAUTENBERG SECAUCUS LOWER LEVEL",
				"SEACAUCUS LOWER LEVEL");
		alternates.put("FRANK R LAUTENBERG SECAUCUS UPPER LEVEL",
				"SEACAUCUS UPPER LEVEL");

	}

	private static String getAlternate(String name) {
		String alternate = alternates.get(name.toUpperCase());
		if (alternate != null) {
			return alternate;
		}
		return name;
	}
}
