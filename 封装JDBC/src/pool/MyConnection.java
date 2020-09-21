package pool;

import util.ConfigReader;

import java.sql.*;

public class MyConnection extends AdapterConnection{

    private Connection conn;
    //标志位，true表示被占用，false表示该通道可用
    private boolean used = false;

    private static String driver;
    private static String url;
    private static String user;
    private static String password ;

    //静态块，让加载类的步骤只执行一次，并初始化四个属性
    static {
        try {
            driver = ConfigReader.getPropertyValue("driver");
            url = ConfigReader.getPropertyValue("url");
            user = ConfigReader.getPropertyValue("user");
            password = ConfigReader.getPropertyValue("password");
            Class.forName(driver);
        }catch (ClassNotFoundException e){
            e.printStackTrace();
        }
    }

    //用来初始化连接通道，每一次被创建时都进行初始化
    {
        try {
            conn = DriverManager.getConnection(url, user, password);
        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    //获取连接通道
    public Connection getConn() {
        return conn;
    }

    //判断该连接通道是否可用
    public boolean isUsed() {
        return used;
    }

    //设置标志位
    public void setUsed(boolean used) {
        this.used = used;
    }

    @Override
    public Statement createStatement() throws SQLException {
        return this.conn.createStatement();
    }

    //重写了继承自AdapterConnection类的方法
    //该方法是为了获取状态参数，本质上还是调用了PreparedStatement
    @Override
    public PreparedStatement prepareStatement(String sql) throws SQLException {
        PreparedStatement pstate = this.conn.prepareStatement(sql);
        return pstate;
    }

    //重写了继承自AdapterConnection类的方法
    //该方法的目的是为了将连接通道设置为可用，看起来就像关闭流一样
    @Override
    public void close() throws SQLException {
        this.used = false;
    }
}
