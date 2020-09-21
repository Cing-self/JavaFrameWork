import dao.Dao;
import dao.UserDao;
import domain.User;
import orm.SqlSession;
import pool.ConnectionPool;
import pool.MyConnection;

import java.util.ArrayList;

public class TestMain {

    public static void main(String[] args) {
        Dao dao = new SqlSession().getMapper(Dao.class);
        dao.delete("2024");
        System.out.println(dao.selectList());
//        TestThread thread1 = new TestThread();
//        TestThread thread2 = new TestThread();
//        TestThread thread3 = new TestThread();
//        TestThread thread4 = new TestThread();
//        TestThread thread5 = new TestThread();
//        TestThread thread6 = new TestThread();
//
//        thread1.start();
//        thread2.start();
//        thread3.start();
//        thread4.start();
//        thread5.start();
//        thread6.start();

    }
}
