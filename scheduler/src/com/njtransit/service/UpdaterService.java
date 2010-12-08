package com.njtransit.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.json.JSONObject;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.njtransit.Root;
import com.njtransit.rail.R;

public class UpdaterService extends Service {

	public class LocalBinder extends Binder {
		UpdaterService getService() {
			return UpdaterService.this;
		}
	}

	private final IBinder mBinder = new LocalBinder();

	private DefaultHttpClient client;

	private Timer timer;

	public IBinder onBind(Intent arg0) {
		return mBinder;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		timer = new Timer(false);
		timer.schedule(new TimerTask() {

			public void run() {
				Context ctx = getApplicationContext();
				int lastVersion = Root.getDatabaseVersion(ctx);
				int version = Root.getVersion(ctx);
				Date lastChecked = Root.getLastChecked(ctx);
				if (lastChecked != null) {
					long now = System.currentTimeMillis();
					Long updateThreshold = Long.parseLong(getResources()
							.getString(R.string.update_threshold));
					if (now - lastChecked.getTime() < updateThreshold) {
						UpdaterService.this.stopSelf();
						return;
					}
				}
				HttpParams params = new BasicHttpParams();
				HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
				HttpProtocolParams.setContentCharset(params, "utf-8");

				params.setBooleanParameter("http.protocol.expect-continue",
						false);
				SchemeRegistry registry = new SchemeRegistry();
				registry.register(new Scheme("http", PlainSocketFactory
						.getSocketFactory(), 80));
				ThreadSafeClientConnManager manager = new ThreadSafeClientConnManager(
						params, registry);
				HttpConnectionParams.setConnectionTimeout(params, 2000);
				HttpConnectionParams.setSoTimeout(params, 2000);
				client = new DefaultHttpClient(manager, params);
				String postUrl = getResources().getString(R.string.update_url);
				HttpPost post = new HttpPost(postUrl);
				try {
					HttpResponse response = client.execute(post);
					int status = response.getStatusLine().getStatusCode();
					if (status >= 200 && status < 300) {
						String updateString = read(response.getEntity()
								.getContent());
						JSONObject update = new JSONObject(updateString);
						Integer newestVersion = update.getInt("version");
						String sha = update.getString("sha");
						Long length = update.getLong("length");
						if (newestVersion > version
								&& newestVersion > lastVersion) {
							String url = update.getString("url");
							HttpGet get = new HttpGet(url);
							File fileDir = ctx.getFilesDir();
							File downloads = new File(fileDir, "downloads");
							downloads.mkdirs();
							for (File file : downloads.listFiles()) {
								if (!file.getName().endsWith(
										newestVersion + ".tmp")) {
									file.delete();
								}
							}
							File newDB = new File(downloads, newestVersion
									+ ".tmp");
							Long newDBLength = newDB.length();
							boolean resume = false;
							if (newDB.exists()) {
								if (length.equals(newDBLength)) {
									String newSha = SHA1(newDB);
									if (newSha.equals(sha)) {
										Root.saveDatabaseVersion(ctx,
												newestVersion);
										Root.setMoveOnRestart(ctx, "downloads/"
												+ newDB.getName());
										Root.setLastChecked(ctx,
												System.currentTimeMillis());
									}
								} else {
									if (newDBLength > length) {
										newDB.delete();
									} else {
										resume = true;
										get.addHeader("Range",
												"bytes=" + newDB.length() + "-");
									}
								}
							}
							response = client.execute(get);
							status = response.getStatusLine().getStatusCode();
							if (status >= 200 && status < 300) {
								byte[] buffer = new byte[1024 * 5];
								InputStream in = response.getEntity()
										.getContent();
								int read;
								FileOutputStream fos = null;

								newDB.createNewFile();
								try {
									fos = new FileOutputStream(newDB, resume);
									while ((read = in.read(buffer)) != -1) {
										fos.write(buffer, 0, read);
									}
									fos.flush();
									fos.close();
									String newDBSha = SHA1(newDB);
									if (sha.equals(newDBSha)) {
										Root.saveDatabaseVersion(ctx,
												newestVersion);
										Root.setMoveOnRestart(ctx, "downloads/"
												+ newDB.getName());
										Root.setLastChecked(ctx,
												System.currentTimeMillis());
									} else {
										newDB.delete();
									}

								} catch (Exception e) {

								} finally {
									if (fos != null) {
										fos.flush();
										fos.close();
									}
								}
							}
						} else {
							Root.setLastChecked(ctx, System.currentTimeMillis());
						}

					}
				} catch (Exception e) {
					e.printStackTrace();
				} finally {

				}
			}
		}, 30000);
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

	public static String SHA1(File file) throws NoSuchAlgorithmException,
			FileNotFoundException {
		FileInputStream fis = new FileInputStream(file);
		return SHA1(fis);
	}

	public static String SHA1(InputStream is) throws NoSuchAlgorithmException {
		MessageDigest md;
		md = MessageDigest.getInstance("SHA-1");
		byte[] sha1hash = new byte[40];
		int read = 0;
		byte[] buffer = new byte[1024 * 5];
		try {
			while ((read = is.read(buffer)) != -1) {
				md.update(buffer, 0, read);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			try {
				is.close();
			} catch (Exception e) {
			}
		}
		sha1hash = md.digest();
		return convertToHex(sha1hash);
	}

	public int onStartCommand(Intent intent, int flags, int startId) {
		return START_STICKY;
	}

	private static String read(InputStream in) throws IOException {
		StringBuilder sb = new StringBuilder();
		BufferedReader r = new BufferedReader(new InputStreamReader(in), 1000);
		for (String line = r.readLine(); line != null; line = r.readLine()) {
			sb.append(line);
		}
		in.close();
		return sb.toString();
	}
}
