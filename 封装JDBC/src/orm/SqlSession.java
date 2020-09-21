package orm;

import com.sun.jmx.snmp.SnmpNull;
import orm.annotation.Delete;
import orm.annotation.Insert;
import orm.annotation.Select;
import orm.annotation.Update;
import pool.ConnectionPool;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import javax.xml.transform.Result;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 该类的目的是负责读写数据库
 */
public class SqlSession {

    private Handler handler = new Handler();

    //=====================方案一===================
    //设计一个方法，该方法用来对数据库进行增删改操作
    //缺点：传递的objs数组里面的值有顺序
    //      objs参数可读性不强
    public void update(String sql, Object... objs){

        try {
            ConnectionPool pool = ConnectionPool.getInstance();
            Connection conn = pool.getConnection();
            PreparedStatement pstate = conn.prepareStatement(sql);
            for (int i = 0; i < objs.length; i ++){
                pstate.setObject(i + 1, objs[i]);
            }
            pstate.executeUpdate();
            pstate.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void insert(String sql, Object... objs){
        this.update(sql, objs);
    }

    public void  delete(String sql, Object... objs){
        this.update(sql, objs);
    }

    //----查询方法
    //方法一：构建成Map集合
    public Map<String, Object> selectOne(String sql, Object... objs){
        Map<String, Object> map = new HashMap<>();
        try {
            //1、获取连接池对象
            ConnectionPool pool = ConnectionPool.getInstance();
            //2、获取连接对象
            Connection conn = pool.getConnection();
            //3、获取状态参数
            PreparedStatement pstat = conn.prepareStatement(sql);
            //4、设置？的对象
            if (objs != null){
                for (int i = 0; i < objs.length; i ++){
                    pstat.setObject(i + 1, objs[i]);
                }
            }
            //5、执行SQL语句
            ResultSet rs = pstat.executeQuery();
            //5、获取查询结果
            if (rs.next()){
                ResultSetMetaData rsmd = rs.getMetaData();
                //遍历结果集
                for (int i = 1; i < rsmd.getColumnCount(); i ++){
                    String columnName = rsmd.getColumnName(i);
                    Object value = rs.getObject(columnName);
                    map.put(columnName, value);
                }
            }
            rs.close();
            pstat.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return map;
    }

    //方法二：使用策略模式
    public <T> T selectOne(String sql, RowMapper mapper, Object... objs){
        return (T) this.selectList(sql, mapper, objs).get(0);
    }
    public <T> List<T> selectList(String sql, RowMapper mapper, Object... objs){
        List<T> list = new ArrayList<>();
        try {
            //1、获取连接池对象
            ConnectionPool pool = ConnectionPool.getInstance();
            //2、获取连接对象
            Connection conn = pool.getConnection();
            //3、获取状态参数
            PreparedStatement pstat = conn.prepareStatement(sql);
            //4、设置SQL参数
            if (objs != null){
                for (int i = 0; i < objs.length; i ++){
                    pstat.setObject(i + 1, objs[i]);
                }
            }
            //5、执行SQL语句，获取结果集
            ResultSet rs = pstat.executeQuery();
            //6、取出查询结果
            while (rs.next()){
                T obj = (T) mapper.mapper(rs);
                list.add(obj);
            }
            //7、关闭流
            rs.close();
            pstat.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }


    //===================方案二========================
    //增
    public void insert(String sql, Object obj){this.update(sql, obj); }
    public void insert(String sql){this.update(sql, null);}

    //删
    public void delete(String sql, Object obj){this.update(sql, obj); }
    public void delete(String sql){this.update(sql, null);}

    //改
    public void update(String sql, Object obj){
        try {
            //0、需要将SQL做一个解析
            //  SQL语句的结构是insert into student values(#{account}, #{password}, #{balance});
            //  我们通过解析将4个key值取出来
            //  并将#{xx}结构全部替换成？

            //1、解析sql语句
            SQLAndKey sqlAndKey = handler.parseSQL(sql);
            //2、获取连接池对象
            ConnectionPool pool = ConnectionPool.getInstance();
            //3、获取连接对象
            Connection conn = pool.getConnection();
            //4、获取状态参数
            PreparedStatement pstate = conn.prepareStatement(sqlAndKey.getSQL());
            //5、将SQL语句和问号值组装完整，调用handler的方法将obj对象替代掉？
            if (obj != null){
                handler.handleParameter(pstate, obj, sqlAndKey.getKeyList());
            }
            pstate.executeUpdate();
            pstate.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void update(String sql){this.update(sql, null);}

    //查
    //1、通过反射的方式
    //查询单条
    public <T> T selectOne(String sql, Object obj, Class resultType){
        return (T) this.selectList(sql, obj, resultType).get(0);
    }
    //查询多条
    public <T> List<T> selectList(String sql, Object obj, Class resultType){
        List<T> list = new ArrayList<>();
        try {
            //1、解析SQL
            SQLAndKey sqlAndKey = handler.parseSQL(sql);
            //2、获取连接对象
            ConnectionPool pool = ConnectionPool.getInstance();
            Connection conn = pool.getConnection();
            //3、获取状态参数
            PreparedStatement pstat = conn.prepareStatement(sql);
            //4、把SQL和问号拼接在一起
            if (obj != null){
                handler.handleParameter(pstat, obj, sqlAndKey.getKeyList());
            }
            //5、执行操作
            ResultSet rs = pstat.executeQuery();
            //6、处理结果
            while (rs.next()){
                //通过handleResult获取到
                T result = (T) handler.handleResult(rs, resultType);
                list.add(result);
            }
            rs.close();
            pstat.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    //========================
    //让SqlSession创建一个代理对象
    //参数：具体的dao类
    //返回值：代理对象
    @SuppressWarnings("all")
    public <T> T getMapper(Class clazz){
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                //1、获取方法上面的注解
                Annotation an = method.getAnnotations()[0];
                //2、获取该注解的类型
                Class type = an.annotationType();
                //3、找到当前注解的方法
                Method valueMethod = type.getDeclaredMethod("value");
                //4、执行方法，获取Sql语句
                String sql = (String) valueMethod.invoke(an);
                //5、获取SQL上面的问号值
                Object param = args == null ? null : args[0];
                //6、根据type，判断具体调用哪个方法
                if (type == Insert.class){
                    SqlSession.this.insert(sql, param);
                }else if (type == Delete.class){
                    SqlSession.this.delete(sql, param);
                }else if (type == Update.class){
                    SqlSession.this.update(sql, param);
                }else if (type == Select.class){
                    //如果是查询语句，则有两种情况
                    //1）多条记录
                    //2）单条记录
                    //获取方法返回值类型
                    Class methodReturnTypeClass = method.getReturnType();
                    //判断是多条还是单挑
                    if (methodReturnTypeClass == List.class){//多条
                        //解析返回值类型里面的泛型
                        //获取返回值的具体类型
                        Type returnType = method.getGenericReturnType();
                        ParameterizedTypeImpl realReturnType = (ParameterizedTypeImpl) returnType;
                        //获取到泛型类[]
                        Type[] patternTypes = realReturnType.getActualTypeArguments();
                        //获取泛型类中的第一个元素
                        Type patternType = patternTypes[0];
                        //还原成class
                        Class resultType = (Class) patternType;
                        return SqlSession.this.selectList(sql, param, resultType);
                    }else {//单条
                        return SqlSession.this.selectOne(sql, param, methodReturnTypeClass);
                    }
                }else {
                    System.out.println("没有这个注解");
                }
                return null;
            }
        });
    }

}
