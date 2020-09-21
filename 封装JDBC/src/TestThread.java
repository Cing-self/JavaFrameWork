import pool.ConnectionPool;

import java.sql.Connection;
import java.sql.SQLException;

public class TestThread extends Thread {

    @Override
    public void run() {
        ConnectionPool pool = ConnectionPool.getInstance();
        try {
            Connection conn = pool.getConnection();
            System.out.println(Thread.currentThread().getName() + "-" +conn);
            Thread.sleep(3000);
            conn.close();
        }catch (InterruptedException | SQLException e) {
            e.printStackTrace();
        }
    }
}
