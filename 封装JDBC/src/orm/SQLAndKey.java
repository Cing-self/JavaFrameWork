package orm;

import java.util.ArrayList;
import java.util.List;

/**
 * 这个类的目的是为了存储被解析后的SQL语句以及SQL语句上的信息（带有问好结构的）
 */
public class SQLAndKey {

    private StringBuilder sql = new StringBuilder();
    private List<String> keyList = new ArrayList<>();

    public SQLAndKey(StringBuilder sql, List<String> keyList){
        this.sql = sql;
        this.keyList = keyList;
    }

    public String getSQL(){
        return sql.toString();
    }

    public List<String> getKeyList(){
        return keyList;
    }
}
