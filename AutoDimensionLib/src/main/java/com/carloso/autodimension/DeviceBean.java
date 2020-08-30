package com.carloso.autodimension;

import java.util.ArrayList;
import java.util.List;

public class DeviceBean {
	Type dpiType;
	int deviceWidth;
	int deviceHeight;
	int density;
	String dpiName;
	boolean isStandard = true;

	public enum Type {
		Ldpi, Mdpi, Hdpi, XHdpi, XXHdpi,NonStandard
	}
	
	public DeviceBean(int deviceWidth, int deviceHeight){
		this(Type.NonStandard, deviceWidth, deviceHeight, 0, false);
		
	}

	private DeviceBean(Type dpiType, int deviceWidth, int deviceHeight,
			int density,boolean isStandard) {
		super();
		this.dpiType = dpiType;
		this.deviceWidth = deviceWidth;
		this.deviceHeight = deviceHeight;
		this.density = density;
		this.isStandard = isStandard;

		switch (dpiType) {

		case Ldpi:
			this.dpiName = "ldpi";
			break;
		case Mdpi:
			this.dpiName = "mdpi";
			break;
		case Hdpi:
			this.dpiName = "hdpi";
			break;
		case XHdpi:
			this.dpiName = "xhdpi";
			break;
		case XXHdpi:
			this.dpiName = "xxhdpi";
			break;

		default:
			break;
		}
		
		if(!isStandard){
			this.dpiName = "values-"+deviceHeight+"x"+deviceWidth;
		}else{
			this.dpiName = "values-"+this.dpiName;
		}
		System.out.println("dipName is:"+dpiName+ " is Standard:"+isStandard+"  "+deviceWidth+"x"+deviceHeight);
	}
	
	/**
	 * ��ȡ��׼�ֱ��ʵ��豸
	 * @return
	 */
	public static List<DeviceBean> getStandardDevices(){
		int[] deviceWidth   = new int[]{240,320,480,720, 1080};
    	int[] deviceHeight  = new int[]{320,480,800,1280,1920};
    	int[] density       = new int[]{120,160,240,320, 480};
    	Type[] dpiType      = new Type[]{Type.Ldpi,Type.Mdpi,Type.Hdpi,Type.XHdpi,Type.XXHdpi};
		
    	List<DeviceBean> devices = new ArrayList<DeviceBean>();
    	for (int i = 0; i < dpiType.length; i++) {
    		
			DeviceBean device = new DeviceBean(dpiType[i], deviceWidth[i], deviceHeight[i], density[i], true);
			devices.add(device);
		}
    	
		return devices;
		
	}

}
