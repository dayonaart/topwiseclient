package id.co.payment2go.terminalsdkhelper.szzt.pinpad;

public class SystemUtil {

    public String getSystemProperty(String name,String defValue) {
        Object bootloaderVersion = null;
        try {
            Class<?> systemProperties = Class.forName("android.os.SystemProperties");
            bootloaderVersion = systemProperties.getMethod("get", new Class[] {
                    String.class, String.class
            }).invoke(systemProperties, new Object[] {
                    name, "unknown"
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(bootloaderVersion==null)
            return defValue;
        return bootloaderVersion.toString();
    }



}
