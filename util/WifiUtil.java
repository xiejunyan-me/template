package com.example.composeproject.util;

import static android.content.Context.LOCATION_SERVICE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiNetworkSpecifier;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 需要权限:
 * <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" /> <!-- 获取WIFI信息状态的权限 -->
 * <uses-permission android:name="android.permission.CHANGE_NETWORK_STATE"/> <!-- 更改WIFI信息状态的权限 -->
 * <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" /> <!-- 获取wifi改变的权限 -->
 * <uses-permission android:name="android.permission.CHANGE_WIFI_STATE" /> <!-- 改变wifi状态的权限 -->
 *  <uses-permission android:name="android.permission.LOCAL_MAC_ADDRESS" tools:ignore="ProtectedPermissions" /><!-- 改变wifi mac地址的权限 -->
 *
 *
 *     <!--    需要手动获取-->
 *     <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" /> <!-- 获取精确位置 -->
 *     <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" /> <!-- 获取初略位置 -->
 *     0326：不需要：<uses-permission android:name="android.permission.WRITE_SETTINGS" tools:ignore="ProtectedPermissions" /> <!-- 写入设置 -->
 *
 *
 *   适配参考：https://juejin.cn/post/6844904201999351815
 *
 *
 *
 */
public class WifiUtil {

    public final static int REQUEST_OPEN_WIFI_CODE = 0x1;
    public final static int REQUEST_CODE_GPS = 0x2;
    public final static int REQUEST_CODE_LOCATION_PERMISSION = 0x3;
    public final static int REQUEST_CODE_WRITTING_PERMISSION = 0x4;

    public static Long ONE_MINUTE = 60*1000L;
    public static Long ONE_HOUR = 60 * ONE_MINUTE;
    public static Long HOUR_72 = 72 * ONE_HOUR;

    public static Boolean isWifiFuncOpen(Context context) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        return wifiManager.isWifiEnabled();
    }
    public static Boolean isWifiConnected(Context context) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifiManager.getConnectionInfo();
        Log.d("Jeck", "ssid:"+info.getSSID());
        return !info.getSSID().equals("<unknown ssid>");
    }

    /**
     * 判断DNS服务是否可行
     * @param context
     * @return
     */
    public static Boolean isDnsSafe(Context context)  {
        try {
            InetAddress inetAddress = InetAddress.getByName("www.google.com");
            return true;
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return false;
        } catch (SecurityException e) {
            return true;
        }
    }

    /**
     * api 29以上才能用这种方法开启/关闭wifi
     *
     * startActivityForResult(intent, REQUEST_OPEN_WIFI_CODE)
     * @param activity
     */
    @RequiresApi(api = Build.VERSION_CODES.Q)
    public static void changeWifiOverAndroid10(Activity activity) {
        Intent intent = new Intent(Settings.Panel.ACTION_WIFI);
        activity.startActivityForResult(intent, REQUEST_OPEN_WIFI_CODE);
    }

    /**
     * 小于api 29 用这种方法开启/关闭wifi
     *
     * >= 29用这个方法无效
     * @param context
     */
    public static void changeWifiLessAndroid10(Context context, Boolean openOrClose) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiManager.setWifiEnabled(openOrClose);
    }

    /**
     * 打开定位服务
     *
     * startActivityForResult(intent, REQUEST_CODE_GPS)
     * @param activity
     */
    public static void openLocationService(Activity activity) {
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);

        // 判断是否有合适的应用能够处理该 Intent，并且可以安全调用 startActivity()。
        if (intent.resolveActivity(activity.getPackageManager()) != null) {
            activity.startActivityForResult(intent, REQUEST_CODE_GPS);
        } else {
            // 该设备不支持定位服务，这？！几乎不存在吧

        }
    }

    /**
     * 判断定位服务是否打开
     * @param context
     * @return
     */
    public static Boolean isLocationServiceOpen(Context context) {
        LocationManager manager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
        boolean isGPS = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        return isGPS;
    }

    /**
     * api < 26 ,即小于Android8的设备扫描附近wifi时不需要请求位置权限
     *
     * fine和coarse只需要一个，这里选择fine，因为
     * @param activity
     */
    public static void askLocationFinePermission(Activity activity) {
        List<String> permissionList = new ArrayList<>();

        // 判断权限是否已经授予，没有就把该权限添加到列表中
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        // 如果列表为空，就是全部权限都获取了，不用再次获取了。不为空就去申请权限
        if (!permissionList.isEmpty()) {
            ActivityCompat.requestPermissions(activity,
                    permissionList.toArray(new String[0]), REQUEST_CODE_LOCATION_PERMISSION);
        } else {

        }
    }

    /**
     * 请求更改系统设置权限，在连接wifi时需要用到
     * @param activity
     */
    public static void askWriteSettingPermission(Activity activity) {
        Intent intent = getModifySysSettingPermissionIntent(activity.getPackageName());
        activity.startActivityForResult(intent, REQUEST_CODE_WRITTING_PERMISSION);
    }

    /**
     * 前往wifi设置界面
     * @param activity
     */
    public static void toWifiSettingPage(Activity activity) {
        Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
        activity.startActivity(intent);
    }


    /**
     * 是否有 修改系统设置 权限
     */
    public static Boolean isHadModifySysSetPermission(Context context) {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.System.canWrite(context);
    }

    /**
     * 修改系统设置intent
     */
    public static Intent getModifySysSettingPermissionIntent(String packageName){
        Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:"+packageName));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }

    /**
     * 开始扫描，用广播接收
     *
     * 应用的扫描次数有限，参考https://juejin.cn/post/6844904201999351815#heading-0。当相隔扫描时间很短时，可以直接调用
     * wifiManager.getScanResults()调用之前的扫描结果
     * @param context
     * @param onScanStartFailure
     */
    public static void startScan(Context context, OnScanFailure onScanStartFailure) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        Boolean isSuccess = wifiManager.startScan();
        if (!isSuccess) {
            onScanStartFailure.doOnScanFailure();
        }
    }


    /**
     * 需要有权限（startScan可以执行）的情况下才能取到值，可以直接调用，即使应用本身没有扫描过wifi
     * 可以获得系统之前扫描得到的wifi列表
     * @param context
     * @return
     */
    public static List<ScanResult> getScanResults(Context context) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        return wifiManager.getScanResults();
    }

    /**
     * 在wifi的加密类型之中选出第一个加密类型，没有加密类型就返回“”
     * @param result
     * @return
     */
    public static String getOneCapabilities(ScanResult result) {
        String capabilities = result.capabilities;
        if (capabilities.equals("")) return "";
        String[] s = capabilities.split("]");
        return s[0].substring(1);
    }

    /**
     * 得到wifi的名字
     * @return 若用户有连接上一个wifi，就返回wifi名字，否则“- -”
     */
    public static String getWifiName(Context context) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifiManager.getConnectionInfo();
        String ssid = formatSSID(info.getSSID());
        if (ssid.equals("<unknown ssid>")) {
            return "- -";
        }else {
            return ssid;
        }
    }

    /**
     * 如果只是想检测网络是否连接，则直接使用 isNetworkConnected()
     * 如果想检测网络连通性（是否能访问网络），则 isNetworkConnected() 与 isNetworkOnline() 结合使用
     * @param context
     * @return
     */
    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager connMgr = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    public static boolean isNetworkOnline() {
        Runtime runtime = Runtime.getRuntime();
        try {
            Process ipProcess = runtime.exec("ping -c 3 www.google.com");
            int exitValue = ipProcess.waitFor();
            Log.i("Avalible", "Process:"+exitValue);
            return (exitValue == 0);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }



    /**
     * 连接一个wifi
     *
     * 请确保：1）wifi已开启
     *       2）有权限，（调用startScan()可以得到结果）并且具有write setting
     * @param ssid wifi的名字
     * @param password 密码
     * @return
     */
    @SuppressLint("MissingPermission")
    public static void connectWifi(Context context, String ssid, String password) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        List<ScanResult> scanResults = getScanResults(context);
        ScanResult scanResult = null;
        for (ScanResult s : scanResults) {
            if (s.SSID.equals(ssid)) {
                scanResult = s;
                break;
            }
        }
        if (scanResult == null) {
            Log.d("Jeck", "connectWifi: scanResult == null");
            return;
        }
        // 找到scanResult，代表要连接的wifi在搜索列表里
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // > Android10连接wifi方式
            connectByP2P(context, ssid, password);
        } else {
            // < 10方式
            Boolean isSuccess = false;
            List<WifiConfiguration> configurations = wifiManager.getConfiguredNetworks();
            WifiConfiguration config = null;
            for (WifiConfiguration w:configurations) {
                if (w.SSID.replaceAll("\"", "").equals(ssid)){
                    // 有配置，代表之前连接过
                    config = w;
                    break;
                }
            }
            if (config != null) {
                // 之前连接过
                isSuccess = wifiManager.enableNetwork(config.networkId, true);
            }else {
                // 没有配置，之前没有连接过, 正常的addNetWork、enableNetwork即可
                WifiConfiguration padWifiNetwork = createWifiConfiguration(
                                ssid,
                                password,
                                getCipherType(scanResult.capabilities),
                                wifiManager
                        );
                int netId = wifiManager.addNetwork(padWifiNetwork);
                isSuccess = wifiManager.enableNetwork(netId, true);
            }
            if (isSuccess) {
                // 成功
            }else {
                // 失败
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private static void connectByP2P(Context context, String ssid, String password) {
        WifiNetworkSpecifier specifier = new WifiNetworkSpecifier.Builder()
                .setSsid(ssid)
                .setWpa2Passphrase(password)
                .build();
        NetworkRequest request =
                new NetworkRequest.Builder()
                        .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                        .removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                        .setNetworkSpecifier(specifier)
                        .build();

        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(@NonNull Network network) {
                // 进行成功的操作
                Log.d("Jeck", "p2p连接成功");
            }

            @Override
            public void onUnavailable() {
                // 进行失败的操作
                Log.d("Jeck", "p2p连接失败");
            }
        };

        connectivityManager.requestNetwork(request, networkCallback);

    }

    private static WifiConfiguration createWifiConfiguration(String ssid, String password, WifiKind kind, WifiManager wifiManager) {
        WifiConfiguration config = new WifiConfiguration();
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();

        //指定对应的SSID
        config.SSID = "\"" + ssid + "\"";

        if (kind == WifiKind.WIFI_CIPHER_NO_PASS) {
            //不需要密码的场景
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        } else if (kind == WifiKind.WIFI_CIPHER_WEP) {
            //以WEP加密的场景
            config.hiddenSSID = true;
            config.wepKeys[0] = "\"" + password + "\"";
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        } else if (kind == WifiKind.WIFI_CIPHER_WPA) {
            //以WPA加密的场景，自己测试时，发现热点以WPA2建立时，同样可以用这种配置连接
            config.preSharedKey = "\"" + password + "\"";
            config.hiddenSSID = true;
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            config.status = WifiConfiguration.Status.ENABLED;
        }

        return config;
    }

//    /**
//     * 注册扫描wifi的广播接收器
//     * @param context
//     */
//    public static void registerWifiReceiver(Context context, WifiHomeActivity.WifiScanResultReceiver wifiScanResultReceiver) {
//        IntentFilter intentFilter = new IntentFilter();
//        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);// wifi扫描结果通知
//        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);// wifi开关变化通知
//        context.registerReceiver(wifiScanResultReceiver, intentFilter);
//    }


    /**
     * 获取当前wifi的速度
     *
     * 只有在连接上一个wifi的时候，才能得到速度（经检验，无需权限）
     * 注：8Mbps=1MB/s
     * @param context
     * @return
     */
    public static String getWifiSpeed(Context context) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifiManager.getConnectionInfo();
        if (info.getBSSID() != null) {
            // 链接信号强度
//            int strength = WifiManager.calculateSignalLevel(info.getRssi(), 5);
            // 链接速度
            int speed1 = info.getLinkSpeed();
            float speed2 = speed1 / 8f;
            // 链接速度单位
//            String units = WifiInfo.LINK_SPEED_UNITS;
            // Wifi源名称
//            String ssid = info.getSSID();
            String string1 = String.format("%.2f", speed2);
            return string1 + "MB/s";
        }
        return String.format("%.2f", (new Random().nextFloat() * 4f)) + "MB/s";
    }

    /**
     * 获取当前wifi强度，-100 —— 0 ，越接近0，信号越强
     *
     * 无需权限
     * @param context
     * @return 返回一个0——100的百分比，百分比越大，信号越强
     */
    public static int getWifiLevel(Context context) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifiManager.getConnectionInfo();
        if (info.getBSSID() != null) {
            int rawLevel = info.getRssi();
            int level = 100 + rawLevel;
            return level;
        }
        return -1;
    }


//    /**
//     * 获取已保护的wifi时长
//     * @param context
//     * @return
//     */
//    public static String getWifiProtectTime(Context context, Long timeStamp) {
//        if (timeStamp == -1L) return "- -";
//        Long interval = System.currentTimeMillis() - timeStamp;
//        if (interval >= HOUR_72) {
//            return "> 72"+context.getString(R.string.wifi_hour);
//        }else if (interval >= ONE_HOUR) {
//            Long hour = interval / ONE_HOUR;
//            return hour + context.getString(R.string.wifi_hour);
//        }else if (interval >= ONE_MINUTE) {
//            Long min = interval / ONE_MINUTE;
//            return min + context.getString(R.string.wifi_minute);
//        }else {
//            return 1 + context.getString(R.string.wifi_minute);
//        }
//    }

    /**
     * 判断当前wifi是否加密, 加密就返回true，不加密返回false
     *
     * 连接上的wifi没有加密，返回false;若wifi加密或没有连接wifi，就返回true
     * @return
     */
    public static Boolean isHaveEncrypt(Context context) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        List<ScanResult> list = wifiManager.getScanResults();
        String name = wifiManager.getConnectionInfo().getSSID();
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).SSID.equals(name)) {
                return getCipherType(list.get(i).capabilities) != WifiKind.WIFI_CIPHER_NO_PASS;
            }
        }
        return true;
    }

    /**
     * 判断一个wifi的加密类型是否是加密的
     *
     * true 加密，false不加密
     */
    public static Boolean isEncrypt(String capabilities) {
        return getCipherType(capabilities) != WifiKind.WIFI_CIPHER_NO_PASS;
    }


    /**
     * 没有权限 或 没有连接wifi都返回null
     *
     * 需要做null判断
     * @param context
     * @return
     */
    @Deprecated
    public static String getWifiMacAddress(Context context) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifiManager.getConnectionInfo();
        if (info.getBSSID() != null) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
               return null;
            }
            return info.getMacAddress();
        }
        return null;
    }

    /**
     * 返回“0.0.0.0”表示没有连接wifi
     * @param context
     * @return
     */
    public static String getWifiIpAddress(Context context) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifiManager.getConnectionInfo();
        if (info.getBSSID() != null) {
            return intToIpv4(info.getIpAddress());
        }
        return "0";
    }

    /**
     * 将IP地址（IPV4）字符串转换为 int类型的数字
     *
     * 思路:将 IP地址（IPV4）的每一段数字转为 8 位二进制数，并将它们放在结果的适当位置上
     *
     * @param ipv4_string（IPV4） 字符串，如 127.0.0.1
     * @return IP地址（IPV4）  字符串对应的 int值
     */
    public static int ipv4ToInt(String ipv4_string) {
        // 取 ip 的各段
        String[] ipSlices = ipv4_string.split("\\.");

        int result = 0;

        for (int i = 0; i < ipSlices.length; i++) {
            // 将 ip 的每一段解析为 int，并根据位置左移 8 位
            int intSlice = Integer.parseInt(ipSlices[i]) << 8 * i;

            // 求或
            result = result | intSlice;
        }

        return result;
    }

    /**
     * 将 int类型的数字转换为IP地址（IPV4）字符串
     *
     * @param ipv4_int 用 int表示的IP地址（IPV4）字符串
     * @return IP地址（IPV4）字符串，如 127.0.0.1
     */
    public static String intToIpv4(int ipv4_int) {
        String[] ipString = new String[4];

        for (int i = 0; i < 4; i++) {
            // 每 8 位为一段，这里取当前要处理的最高位的位置
            int pos = i * 8;

            // 取当前处理的 ip 段的值
            int and = ipv4_int & (255 << pos);

            // 将当前 ip 段转换为 0 ~ 255 的数字，注意这里必须使用无符号右移
            ipString[i] = String.valueOf(and >>> pos);
        }
        StringBuilder builder = new StringBuilder();
        builder.append(ipString[0]);
        builder.append(".");
        builder.append(ipString[1]);
        builder.append(".");
        builder.append(ipString[2]);
        builder.append(".");
        builder.append(ipString[3]);
        return builder.toString();
    }

    public static String formatSSID(String ssid) {
        return ssid.replaceAll("\"", "");
    }


    public interface OnScanFailure{
        void doOnScanFailure();
    }

    public enum  WifiKind{
        WIFI_CIPHER_WEP, WIFI_CIPHER_WPA, WIFI_CIPHER_NO_PASS
    }

    private static WifiKind getCipherType(String capabilities) {
        if(capabilities.contains("WEB")) {
            return WifiKind.WIFI_CIPHER_WEP;
        }else if (capabilities.contains("PSK")) {
            return WifiKind.WIFI_CIPHER_WPA;
        }else if (capabilities.contains("WPS")) {
            return WifiKind.WIFI_CIPHER_NO_PASS;
        }else {
            return WifiKind.WIFI_CIPHER_NO_PASS;
        }
    }
}
