package com.example.e_shop.handler;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.sql.*;

@MappedTypes(Long[].class)
@MappedJdbcTypes(JdbcType.ARRAY)
public class LongArrayTypeHandler extends BaseTypeHandler<Long[]> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Long[] parameter, JdbcType jdbcType) throws SQLException {
        ps.setArray(i, ps.getConnection().createArrayOf("bigint", parameter));
    }

    @Override
    public Long[] getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return toLongArray(rs.getArray(columnName));
    }

    @Override
    public Long[] getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return toLongArray(rs.getArray(columnIndex));
    }

    @Override
    public Long[] getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return toLongArray(cs.getArray(columnIndex));
    }

    private Long[] toLongArray(java.sql.Array pgArray) throws SQLException {
        if (pgArray == null) return null;
        Object[] objects = (Object[]) pgArray.getArray();
        Long[] result = new Long[objects.length];
        for (int i = 0; i < objects.length; i++) {
            if (objects[i] instanceof Number) {
                result[i] = ((Number) objects[i]).longValue();
            }
        }
        return result;
    }
}
