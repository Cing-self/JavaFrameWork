package dao;

import domain.User;
import orm.Handler;
import orm.RowMapper;
import orm.SqlSession;
import orm.annotation.Insert;
import pool.ConnectionPool;

import java.sql.*;
import java.util.Map;

public class UserDao {

    private SqlSession session = new SqlSession();
    private ConnectionPool pool = ConnectionPool.getInstance();


//    //===========方案一================
//    //增
//    public void insert(User user){
//        String sql = "insert into atm values(?, ?, ?)";
//        session.insert(sql, user.getAccount(), user.getPassword(), user.getBalance());
//    }
//    //删
//    public void delete(String account){
//        String sql = "delete from atm where account = ?";
//        session.delete(sql, account);
//    }
//    //改
//    public void update(User user){
//        String sql = "update atm set account = ?, password = ?, balance = ? where account = ?";
//        session.update(sql, user.getAccount(), user.getPassword(), user.getBalance(), user.getAccount());
//    }

    //查：
    // 1、Map
//    public Map<String, Object> selectOne(String account){
//        String sql = "SELECT *FROM ATM WHERE ACCOUNT = ?";
//        return session.selectOne(sql, account);
//    }
    // 2、策略模式
//    public User selectOne(String account){
//        String sql = "SELECT *FROM ATM WHERE ACCOUNT = ?";
//        return session.selectOne(sql, new RowMapper() {
//            @Override
//            public Object mapper(ResultSet rs)  throws SQLException {
//                String account = rs.getString("account");
//                String password = rs.getString("password");
//                Float balance = rs.getFloat("balance");
//                User user = new User(account, password, balance);
//                return user;
//            }
//        }, account);
//    }

    //=================方案二==================
    @Insert("insert into atm values(#{account}, #{password}, #{balance})")
    public void insert(User user){
        String sql = "insert into atm values(#{account}, #{password}, #{balance})";
        session.insert(sql, user);
    }

    public void delete(String account){
        String sql = "delete from atm where account = #{account}";
        session.delete(sql, account);
    }

    public void update(User user){
        String sql = "update atm set account=#{account}, password=#{password}, balance=#{balance} where account=#{account}";
        session.update(sql, user);
    }

    public User selectOne(String account){
        String sql = "SELECT *FROM ATM WHERE ACCOUNT = ?";
        User user = (User) session.selectOne(sql, account, User.class);
        return user;
    }

}
