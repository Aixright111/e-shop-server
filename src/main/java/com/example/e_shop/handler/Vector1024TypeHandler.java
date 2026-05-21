package com.example.e_shop.handler;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

import java.sql.*;

/**
 * PostgreSQL pgvector 的 vector(1024) 类型处理器。
 * <p>
 * 将 Java float[] 与 PostgreSQL vector 类型互转。
 * 写入时通过 setObject 传入文本格式（[0.1,0.2,...]），
 * 读取时从 getObject 拿到字符串后解析。
 * </p>
 */
@MappedTypes(float[].class)
public class Vector1024TypeHandler extends BaseTypeHandler<float[]> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, float[] parameter, JdbcType jdbcType) throws SQLException {
        ps.setObject(i, arrayToVectorStr(parameter), Types.OTHER);
    }

    @Override
    public float[] getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return parseVector(rs.getString(columnName));
    }

    @Override
    public float[] getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return parseVector(rs.getString(columnIndex));
    }

    @Override
    public float[] getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return parseVector(cs.getString(columnIndex));
    }

    /**
     * 将 pgvector 文本格式 [0.1,0.2,0.3,...] 解析为 float[]。
     */
    private float[] parseVector(String str) {
        if (str == null || str.isBlank()) return null;

        str = str.trim();
        if (str.startsWith("[") && str.endsWith("]")) {
            str = str.substring(1, str.length() - 1);
        }
        if (str.isBlank()) return new float[0];

        String[] parts = str.split(",");
        float[] result = new float[parts.length];
        for (int i = 0; i < parts.length; i++) {
            result[i] = Float.parseFloat(parts[i].trim());
        }
        return result;
    }

    /**
     * 将 float[] 转为 pgvector 文本格式 [0.1,0.2,...]
     */
    private String arrayToVectorStr(float[] arr) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < arr.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(arr[i]);
        }
        sb.append("]");
        return sb.toString();
    }
}
