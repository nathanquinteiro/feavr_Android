package quinteiro.nathan.feavr.utils;

import android.content.Intent;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

/**
 * Created by jeremie on 04.12.17.
 */

public class NetworkUtils {

    public static String getIP4(){

        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress();
                        //boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
                        boolean isIPv4 = sAddr.indexOf(':') < 0;

                        //if (useIPv4) {
                            if (isIPv4)
                                return sAddr;
                        /*} else {
                            if (!isIPv4) {
                                int delim = sAddr.indexOf('%'); // drop ip6 zone suffix
                                return delim < 0 ? sAddr.toUpperCase() : sAddr.substring(0, delim).toUpperCase();
                            }
                        }*/
                    }
                }
            }
        } catch (Exception ex) {
        } // for now eat exceptions

        return "";
    }


    public static boolean isValidIP4(String ip){

        if(ip == null || ip.isEmpty())
            return false;

        //check if have 3.
        String[] splitted = ip.split("\\.");
        if (splitted.length != 4)
            return false;

        //check if number between 0 and 255 :

       for(String s :splitted){
           int i = Integer.parseInt(s);
           if ((i<0)|| (i>255) )
               return false;
       }

        return true;
    }
}
