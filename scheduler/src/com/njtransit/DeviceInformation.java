package com.njtransit;

import android.content.Context;
import android.os.Build;
import android.provider.Settings;

public class DeviceInformation {

	private DeviceInformation() {}

	private String board;
	private String brand;
	private String manufacturer;
	private String model;
	private String product;
	
	public String getBoard() {
		return board;
	}
	
	public String getBrand() {
		return brand;
	}

	public String getUuid() {
		return uuid;
	}

	private String uuid;
	
	private int version;
	
	public String getManufacturer() {
		return manufacturer;
	}

	public String getModel() {
		return model;
	}

	public String getProduct() {
		return product;
	}

	public int getVersion() {
		return version;
	}
	
	public static DeviceInformation getDeviceInformation(Context ctx) {
		DeviceInformation d = new DeviceInformation();
		d.brand = Build.BRAND;		
		d.manufacturer = Build.MANUFACTURER;
		d.model = Build.MODEL;
		d.product = Build.PRODUCT;		
		d.version = Build.VERSION.SDK_INT;
		d.uuid = Settings.Secure.getString(ctx.getContentResolver(), Settings.Secure.ANDROID_ID);
		return d;
	}

	@Override
	public String toString() {
		return manufacturer+"/"+model+"/"+version+"/"+brand;
	}
	
}
