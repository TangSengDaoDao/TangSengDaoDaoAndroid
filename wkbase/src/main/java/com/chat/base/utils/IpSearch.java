package com.chat.base.utils;

import android.content.Context;
import android.content.res.Resources;
import android.os.Build;

import com.bumptech.glide.load.Options;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;


public class IpSearch {

    private int[] prefStart = new int[256];
    private int[] prefEnd = new int[256];
    private long[] endArr;
    private String[] addrArr;

    private static IpSearch instance = null;
    private static String pathUrl = "qqzeng-ip-3.0-ultimate.dat";

    private IpSearch(Context context, String url) {
        pathUrl = url;
        if (null == pathUrl) {
            return;
        }
        byte[] data = null;
        InputStream is = null;
        try {
            Resources resources = context.getResources();
            Options options = new Options();
            is = context.getAssets().open(pathUrl);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                data = is.readAllBytes();
            } else {
                data = readStreamManually(is);
            }
            if (null == data) {
                return;
            }
            for (int k = 0; k < 256; k++) {
                int i = k * 8 + 4;
                prefStart[k] = (int) BytesToLong(data[i], data[i + 1], data[i + 2], data[i + 3]);
                prefEnd[k] = (int) BytesToLong(data[i + 4], data[i + 5], data[i + 6], data[i + 7]);

            }

            int RecordSize = (int) BytesToLong(data[0], data[1], data[2], data[3]);
            endArr = new long[RecordSize];
            addrArr = new String[RecordSize];
            for (int i = 0; i < RecordSize; i++) {
                int p = 2052 + (i * 8);
                long endipnum = BytesToLong(data[p], data[1 + p], data[2 + p], data[3 + p]);

                int offset = (int) BytesToLong3(data[4 + p], data[5 + p], data[6 + p]);
                int length = data[7 + p] & 0xff;

                endArr[i] = endipnum;

                addrArr[i] = new String(Arrays.copyOfRange(data, offset, (offset + length)));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private byte[] readStreamManually(InputStream is) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[4096]; // 使用合适的缓冲区大小

        while ((nRead = is.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }

        buffer.flush();
        return buffer.toByteArray();
    }



    public static synchronized IpSearch getInstance(Context context) {
        if (instance == null) {
            instance = new IpSearch(context,pathUrl);
        }
        if (null == instance.endArr || null == instance.addrArr) {
            instance = null;
        }
        return instance;
    }

    public  String getArea(String ip) {
        String area = "CN";
        if (null == instance) {
            return area;
        }
        String result = instance.Get(ip);
        String[] split = result.split("\\|");
        if (8 < split.length) {
            area = split[8];
        }
        return area;
    }

    /**
     * 根据ip地址获取所在地址  国家-省份-城市
     *
     * @param ip
     * @return
     */
    public static String getAddressByIp(String ip) {
        String result = instance.Get(ip);
        String addressStr = "";
        if (null == instance || null == result) {
            return addressStr;
        }
        try {
            String[] split = result.split("\\|");
            return split[1] + (split[2].isEmpty() ? "" : "-" + split[2]) + (split[3].isEmpty() ? "" : "-" + split[3]);
        } catch (IndexOutOfBoundsException e) {
            return "";
        }
    }


    public String Get(String ip) {

        String[] ips = ip.split("\\.");
        int pref = Integer.parseInt(ips[0]);
        long val = ipToLong(ip);
        int low = prefStart[pref], high = prefEnd[pref];
        long cur = low == high ? low : BinarySearch(low, high, val);
        return addrArr[(int) cur];
    }

    private int BinarySearch(int low, int high, long k) {
        int M = 0;
        while (low <= high) {
            int mid = (low + high) / 2;

            long endipNum = endArr[mid];
            if (endipNum >= k) {
                M = mid;
                if (mid == 0) {
                    break;
                }
                high = mid - 1;
            } else {
                low = mid + 1;
            }
        }
        return M;
    }

    private long BytesToLong(byte a, byte b, byte c, byte d) {
        return (a & 0xFFL) | ((b << 8) & 0xFF00L) | ((c << 16) & 0xFF0000L) | ((d << 24) & 0xFF000000L);

    }

    private long BytesToLong3(byte a, byte b, byte c) {
        return (a & 0xFFL) | ((b << 8) & 0xFF00L) | ((c << 16) & 0xFF0000L);

    }

    private long ipToLong(String ip) {
        long result = 0;
        String[] d = ip.split("\\.");
        for (String b : d) {
            result <<= 8;
            result |= Long.parseLong(b) & 0xff;
        }
        return result;
    }

//    public static void main(String[] args) {
//        //IpSearch.pathUrl="D:\\IDEAWork\\shiku-im-parent\\modules_new\\mianshi-im-api\\src\\main\\resources\\qqzeng-ip-3.0-ultimate.dat";
//        IpSearch.pathUrl = "F:\\IDEA-WORKSPACE\\shikuIM\\modules_new\\mianshi-im-api\\src\\main\\resources\\qqzeng-ip-3.0-ultimate.dat";
//        IpSearch finder = IpSearch.getInstance(pathUrl);
//
//        String ip = "139.162.46.88";
//        String result = finder.getAddressByIp(ip);
//
//        System.out.println("=====Area===>>> " + result);
//
//		/*System.out.println(ip);
//		System.out.println(result);
//		String[] split = result.split("\\|");
//		System.out.println(Arrays.toString(split));
//		*//*String guojia=split[7];
//		String code=split[8];
//		String area=split[2];
//		String city=split[3];*//*
//		System.out.println("国家==>  "+split[7]+"   代码 ==>   "+ split[8]);
//		System.out.println("省份==>  "+split[2]+"   城市 ==>   "+ split[3]);*/
//
//        /*
//         * 1.197.224.9 亚洲|中国|河南|周口|商水|电信|411623|China|CN|114.60604|33.53912
//         */
//
//    }

}
