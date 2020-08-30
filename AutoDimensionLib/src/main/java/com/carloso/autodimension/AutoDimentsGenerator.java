package com.carloso.autodimension;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * 自动生产各种分辨率下的对应的尺寸
 * 备注：对于标准分辨率的设备，使用pd，对于非标准分辨率且需要单独适配的设备使用px
 * 在cmd控制台使用命令的方式：
 * 生成默认的尺寸：java -jar xx.jar
 * 生成默认+单独适配的尺寸：java -jar xx.jar [deviceWidthPx] [deviceHeightPx] [deviceWidthPx] [deviceHeightPx]..
 * <p>
 * Created by chenlu on 2016/10/14.
 */


/**
 * PX = density/160 * DP
 */
public class AutoDimentsGenerator {
    /*生成res资源文件目录*/
    private static final String RES_DIR_PATH = "./AutoDimensionLib/src/main/res/";
    int mUiWidth = 1080;
    int mUiHeight = 1920;
    int mUiDensity = 480;

    private int mMaxWidth;
    private int mMaxHeight;

    private int mMinFontPxSize = 20;
    private int mMaxFontPxSize = 70;

    static final float mLdpiDensity = 120;
    static final float mMdpiDensity = 160;
    static final float mHdpiDensity = 240;
    static final float mXHdpiDensity = 320;
    static final float mXXHdpiDensity = 480;

    /**
     * @param uiWidth   UI设计图的宽度Px
     * @param uiHeight  UI设计图的高度Px
     * @param uiDensity UI设计图参考设备的屏幕密度值
     */
    public AutoDimentsGenerator(int uiWidth, int uiHeight, int uiDensity) {
        this.mUiWidth = uiWidth;
        this.mUiHeight = uiHeight;
        this.mUiDensity = uiDensity;

        this.mMaxWidth = this.mUiWidth;
        this.mMaxHeight = this.mUiHeight;

    }

    /**
     * 设置生存字体大小尺寸的范围
     *
     * @param minTextPxSize
     * @param maxTextPxSize
     */
    public void setTextPxSizeRange(int minTextPxSize, int maxTextPxSize) {
        this.mMinFontPxSize = minTextPxSize;
        this.mMaxFontPxSize = maxTextPxSize;
    }


    public static void main() {
        String[] args = {"-width", "1080", "-height", "1920", "-density", "320"};
        main(args);
    }


    public static void main(String[] args) {

        for (String s : args) {
            // java -jar xx.jar -uiwidth 1080 -uiheight 1920 -uidensity 320 720X1080

            System.out.println("args~~~~:" + s);
        }
        if (!checkArgs(args)) {
            System.err.println("输入参数有误，请检查！输入参数有误，请检查！请按如下方式传入：");
            System.out.println("java -jar xx.jar -uiwidth 1080 -uiheight 1920 -uidensity 320 [720X1080] [320x480]");
            return;
        }

        String[] nonStandardDevices = new String[args.length - 6];
        for (int i = 0; i < nonStandardDevices.length; i++) {
            nonStandardDevices[i] = args[6 + i];
        }

        List<DeviceBean> nonStandard;
        try {
            nonStandard = getNonStandardDevices(nonStandardDevices);
        } catch (Exception e) {
            System.err.println("输入参数有误，请检查！请按如下方式传入：");
            System.out.println("java -jar xx.jar -uiwidth 1080 -uiheight 1920 -uidensity 320 [720X1080] [320x480]");
            return;
        }
        int uiWidthPx = Integer.valueOf(args[1]);
        int uiHeight = Integer.valueOf(args[3]);
        int uiDensity = Integer.valueOf(args[5]);

        AutoDimentsGenerator setXmlValues = new AutoDimentsGenerator(uiWidthPx, uiHeight, uiDensity);


        List<DeviceBean> devices = DeviceBean.getStandardDevices();

        if (nonStandard != null && !nonStandard.isEmpty()) {

            devices.addAll(nonStandard);
        }


        for (DeviceBean device : devices) {
            String diments;
            if (device.isStandard) {
                diments = setXmlValues.getStandardDeviceDiments(device.density, device.deviceWidth, device.deviceHeight);
            } else {
                diments = setXmlValues.getNonStandardDeviceDiments(device.deviceWidth, device.deviceHeight);
            }


            writeFile(RES_DIR_PATH + device.dpiName + "/base_values.xml", diments);
        }

        System.out.println("SUCCESS!");

    }


    private static boolean checkArgs(String[] args) {
        //java -jar xx.jar -uiwidth 1080 -uiheight 1920 -uidensity 320 [720X1080] [320x480]
        if (args == null || args.length < 6) {
            return false;
        } else if (!args[0].equalsIgnoreCase("-uiwidth".trim())) {
            return false;
        } else if (!isNumber(args[1])) {
            return false;
        } else if (!args[2].equalsIgnoreCase("-uiheight".trim())) {
            return false;
        } else if (!isNumber(args[3])) {
            return false;
        } else if (!args[4].equalsIgnoreCase("-uidensity".trim())) {
            return false;
        } else if (!isNumber(args[5])) {
            return false;
        }

        return true;
    }

    private static boolean isNumber(String string) {
        try {
            Integer.parseInt(string);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return false;
        }
        return true;
    }

    /**
     * @param uiPx          UI设计图上的像素值
     * @param deviceWidthPx 设备屏幕的宽度像素值
     * @return 获取UI设计图上的像素尺寸在对应设备上的像素值
     */
    private float uiPx2DevicePx_Width(float uiPx, int deviceWidthPx) {

        float devicePx = 1.0f * deviceWidthPx / mUiWidth * uiPx;


        return devicePx;
    }

    /**
     * @param uiPx           UI设计图上的像素值
     * @param deviceHeightPx 设备屏幕的高度像素值
     * @return 获取UI设计图上的像素尺寸在对应设备上的像素值
     */
    private float uiPx2DevicePx_Height(float uiPx, int deviceHeightPx) {

        float devicePx = 1.0f * deviceHeightPx / mUiHeight * uiPx;


        return devicePx;
    }

    /**
     * @param uiPx          UI设计图上的像素值
     * @param deviceWidthPx 设备屏幕的高度像素值
     * @return 获取UI设计图上的像素尺寸在对应设备上的像素值
     */
    private float uiFontPx2DeviceFontPx(float uiPx, int deviceWidthPx, int deviceHeightPx) {

        float devicePx = (uiPx2DevicePx_Width(uiPx, deviceWidthPx) + uiPx2DevicePx_Height(uiPx, deviceHeightPx)) / 2;

        return devicePx;
    }


    private float px2dp(float px, float density) {

        return 160 * 1.0f / density * px;
    }

    private String getStandardDeviceDiments(int density, int deviceWidthPx, int deviceHeightPx) {
        StringBuffer sb = new StringBuffer();
        sb.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
        sb.append("\r\n").append("<resources>").append("\r\n");

        sb.append(getStandardDeivceValues_X(0, mMaxWidth, deviceWidthPx, density));
        sb.append("\r\n\r\n\r\n");
        sb.append(getStandardDeviceValues_Y(0, mMaxHeight, deviceHeightPx, density));
        sb.append("\r\n\r\n\r\n");
        sb.append(getStandardDeviceValues_FontSize(mMinFontPxSize, mMaxFontPxSize, deviceWidthPx, deviceHeightPx, density));

        sb.append("</resources>").append("\r\n");
        return sb.toString();

    }

    private String getNonStandardDeviceDiments(int deviceWidthPx, int deviceHeightPx) {
        StringBuffer sb = new StringBuffer();
        sb.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
        sb.append("\r\n").append("<resources>").append("\r\n");

        sb.append(getNonStandardDeviceValues_X(0, mMaxWidth, deviceWidthPx));
        sb.append("\r\n\r\n\r\n");
        sb.append(getNonStandardDeviceValues_Y(0, mMaxHeight, deviceHeightPx));
        sb.append("\r\n\r\n\r\n");
        sb.append(getNonStandarddeviceValues_FontSize(mMinFontPxSize, mMaxFontPxSize, deviceWidthPx, deviceHeightPx));

        sb.append("</resources>").append("\r\n");
        return sb.toString();

    }


    private String getStandardDeviceValues_Y(int startY, int endY, int deviceHeightPx, int density) {
        StringBuffer sb = new StringBuffer();
        for (int i = startY; i <= endY; i++) {
            for (int j = 0; j < 1; j++) {
                float uiPx = (float) (i + 0.5 * j);
                String st = "<dimen name=\"y_ui_px_key\">value_dp</dimen>";
                st = st.replaceAll("key", uiPx + "");
                uiPx = uiPx2DevicePx_Height(uiPx, deviceHeightPx);
                st = st.replaceAll("value_", String.format("%.2f", px2dp(uiPx, density)));
                sb.append("\t").append(st).append("\r\n");
            }
        }
        return sb.toString();
    }

    private String getStandardDeviceValues_FontSize(int start, int end, int deviceWidthPx, int deviceHeightPx, int density) {
        StringBuffer sb = new StringBuffer();
        for (int i = start; i <= end; i++) {
            float uiPx = i;
            String st = "<dimen name=\"font_ui_px_key\">value_sp</dimen>";
            st = st.replaceAll("key", uiPx + "");
            uiPx = uiFontPx2DeviceFontPx(uiPx, deviceWidthPx, deviceHeightPx);
            st = st.replaceAll("value_", String.format("%.2f", px2dp(uiPx, density)));
            sb.append("\t").append(st).append("\r\n");
        }
        return sb.toString();
    }

    private String getNonStandarddeviceValues_FontSize(int start, int end, int deviceWidthPx, int deviceHeightPx) {
        StringBuffer sb = new StringBuffer();
        for (int i = start; i <= end; i++) {
            float uiPx = i;
            String st = "<dimen name=\"font_ui_px_key\">value_px</dimen>";
            st = st.replaceAll("key", uiPx + "");
            uiPx = uiFontPx2DeviceFontPx(uiPx, deviceWidthPx, deviceHeightPx);
            st = st.replaceAll("value_", String.format("%.2f", uiPx));
            sb.append("\t").append(st).append("\r\n");
        }
        return sb.toString();
    }


    private String getStandardDeivceValues_X(int startX, int endX, int deviceWidthPx, int density) {
        StringBuffer sb = new StringBuffer();
        for (int i = startX; i <= endX; i++) {
            for (int j = 0; j < 1; j++) {
                float uiPx = (float) (i + 0.5 * j);
                String st = "<dimen name=\"x_ui_px_key\">value_dp</dimen>";
                st = st.replaceAll("key", uiPx + "");

                uiPx = uiPx2DevicePx_Width(uiPx, deviceWidthPx);
                st = st.replaceAll("value_", String.format("%.2f", px2dp(uiPx, density)));
                sb.append("\t").append(st).append("\r\n");
            }
        }
        return sb.toString();
    }

    private String getNonStandardDeviceValues_X(int startX, int endX, int deviceWidthPx) {
        StringBuffer sb = new StringBuffer();
        for (int i = startX; i <= endX; i++) {
            for (int j = 0; j < 1; j++) {
                float uiPx = (float) (i + 0.5 * j);
                String st = "<dimen name=\"x_ui_px_key\">value_px</dimen>";
                st = st.replaceAll("key", uiPx + "");

                float devicePx = uiPx2DevicePx_Width(uiPx, deviceWidthPx);
                st = st.replaceAll("value_", String.format("%.2f", devicePx));
                sb.append("\t").append(st).append("\r\n");
            }
        }
        return sb.toString();
    }

    private String getNonStandardDeviceValues_Y(int startY, int endY, int deviceHeightPx) {
        StringBuffer sb = new StringBuffer();
        for (int i = startY; i <= endY; i++) {
            for (int j = 0; j < 1; j++) {
                float uiPx = (float) (i + 0.5 * j);
                String st = "<dimen name=\"y_ui_px_key\">value_px</dimen>";
                st = st.replaceAll("key", uiPx + "");

                uiPx = uiPx2DevicePx_Height(uiPx, deviceHeightPx);
                st = st.replaceAll("value_", String.format("%.2f", uiPx));
                sb.append("\t").append(st).append("\r\n");
            }
        }
        return sb.toString();
    }

    private static List<DeviceBean> getNonStandardDevices(String[] args) throws IllegalArgumentException {
        List<DeviceBean> devices = new ArrayList<DeviceBean>();
        for (int i = 0; i < args.length; i++) {
            String device = args[i].toLowerCase();
            if (!device.contains("x".toLowerCase()))
                throw new IllegalArgumentException();
            String[] split = device.split("x".toLowerCase());

            int deviceWidth = Integer.valueOf(split[0]);
            int deviceHeight = Integer.valueOf(split[1]);
            devices.add(new DeviceBean(deviceWidth, deviceHeight));
        }
        return devices;
    }

    public static void writeFile(String filePaht, String text) {

        File file = new File(filePaht);
        File parent = file.getParentFile();
        if (!parent.exists()) {
            parent.mkdirs();
        }
        PrintWriter out = null;
        try {
            out = new PrintWriter(new BufferedWriter(new FileWriter(file)));
            out.println(text);
        } catch (IOException e) {
            e.printStackTrace();
        }
        out.close();
    }


}
