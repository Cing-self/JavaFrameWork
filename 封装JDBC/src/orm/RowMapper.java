package orm;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface RowMapper {

    Object mapper(ResultSet rs) throws SQLException;
}
