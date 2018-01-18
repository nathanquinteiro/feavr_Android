package quinteiro.nathan.feavr.utils;


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
                        boolean isIPv4 = sAddr.indexOf(':') < 0;


                            if (isIPv4)
                                return sAddr;

                    }
                }
            }
        } catch (Exception ex) {
        } // for now eat exceptions

        return "";
    }


    // Check if the string seem to be a valid IPV4
    public static boolean isValidIP4(String ip){

        if(ip == null || ip.isEmpty())
            return false;

        //check if have 3 dots
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
