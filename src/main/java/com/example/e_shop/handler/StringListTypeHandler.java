package com.example.e_shop.handler;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@MappedTypes(List.class)
@MappedJdbcTypes(JdbcType.ARRAY)
public class StringListTypeHandler extends BaseTypeHandler<List<String>> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, List<String> parameter, JdbcType jdbcType) throws SQLException {
        ps.setArray(i, ps.getConnection().createArrayOf("text", parameter.toArray()));
    }

    @Override
    public List<String> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return toStringList(rs.getArray(columnName));
    }

    @Override
    public List<String> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return toStringList(rs.getArray(columnIndex));
    }

    @Override
    public List<String> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return toStringList(cs.getArray(columnIndex));
    }

    private List<String> toStringList(java.sql.Array pgArray) throws SQLException {
        if (pgArray == null) return null;
        Object[] objects = (Object[]) pgArray.getArray();
        List<String> result = new ArrayList<>(objects.length);
        for (Object o : objects) {
            result.add(o != null ? o.toString() : null);
        }
        return result;
    }
}
