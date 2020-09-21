package dao;

import domain.User;
import orm.annotation.Delete;
import orm.annotation.Insert;
import orm.annotation.Select;
import orm.annotation.Update;

import java.util.List;

public interface Dao {

    @Insert("insert into atm values(#{account}, #{password}, #{balance})")
    void insert(User user);

    @Delete("delete from atm where account = #{account}")
    void delete(String account);

    @Update("update atm set account=#{account}, password=#{password}, balance=#{balance} where account=#{account}")
    void update(String account);

    @Select("SELECT *FROM ATM WHERE ACCOUNT = ?")
    User selectOne(String account);

    @Select("SELECT *FROM ATM")
    List<User> selectList();

}
