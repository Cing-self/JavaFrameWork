package util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class ConfigReader {

    private static Properties properties;
    //用Map集合存放properties文件下的配置信息
    private static Map<String, String> configMap;

    //配置文件只加载一次
    static {
        properties = new Properties();
        configMap = new HashMap<>();
        InputStream inputStream = null;
        try {
            inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("configuration.properties");
            properties.load(inputStream);
            Enumeration en = properties.propertyNames();
            while (en.hasMoreElements()){
                String key = (String) en.nextElement();
                String value = properties.getProperty(key);
                configMap.put(key, value);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (inputStream != null){
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //获取配置文件里面的value值
    public static String getPropertyValue(String key){
        return configMap.get(key);
    }
}
