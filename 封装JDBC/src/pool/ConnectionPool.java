package pool;

import util.ConfigReader;

import javax.swing.*;
import java.io.InputStream;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

public class ConnectionPool {

    //将ConnectionPool设计成单例模式
    private ConnectionPool(){}

    private static volatile ConnectionPool connectionPool;

    public static ConnectionPool getInstance(){
        if (connectionPool == null){
            synchronized (ConnectionPool.class){
                if (connectionPool == null){
                    connectionPool = new ConnectionPool();
                }
            }
        }
        return connectionPool;
    }

    //获取最小连接个数以及等待时间
    private int minConnectCount = Integer.parseInt(ConfigReader.getPropertyValue("minConnectCount"));
    private int waitTime = Integer.parseInt(ConfigReader.getPropertyValue("waitTime"));

    //属性---List集合，用来存储连接对象
    private List<Connection> pool = new ArrayList<>();

    //往pool集合里面存放连接对象
    {
        for (int i = 1; i <= minConnectCount; i ++){
            pool.add(new MyConnection());
        }
    }

    //方法，获取连接对象
    private Connection getMC(){
        Connection result = null;
        //遍历连接池中的对象
        for (Connection conn : pool){
            MyConnection mc = (MyConnection) conn;
            if (!mc.isUsed()){//表示连接是可使用的
                synchronized (ConnectionPool.class){
                    if (!mc.isUsed()){
                        mc.setUsed(true);
                        result = mc;
                    }
                }
                break;
            }
        }
        return result;
    }

    //该方法是为了获取连接对象，并增加了等待机制
    public Connection getConnection(){
        Connection result = this.getMC();
        int count = 0;//记录循环的次数
        while (result == null && count < waitTime*10){
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            result = this.getMC();
            count++;
        }
        if (result == null){
            throw new SystemBusyException("当前系统繁忙，请稍后再试");
        }
        return result;
    }
}
