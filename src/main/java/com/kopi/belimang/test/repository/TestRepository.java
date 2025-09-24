package com.kopi.belimang.test.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import java.util.List;

import com.kopi.belimang.test.entity.Test;

@Repository
public class TestRepository {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final RowMapper<Test> rowMapper = (rs, rowNum) -> {
        Test test = new Test();
        test.setId(rs.getLong("id"));
        test.setValue(rs.getString("value"));
        return test;
    };

    public Test insert(Test test) throws Exception {
        String query = """
            INSERT
            INTO
                test (value)
            VALUES 
                (?)
            RETURNING id, value 
        """; 
        Test result = jdbcTemplate.queryForObject(query, rowMapper, test.getValue());
        
        return result;
    }

    public List<Test> getAll() {
        String query = """
            SELECT 
                *
            FROM
                test      
        """;

        return jdbcTemplate.query(query, rowMapper);
    }

    public Test getById(Long id) throws DataAccessException {
        String query = """
            SELECT
                *
            FROM
                test
            WHERE
                id = ?     
        """;

        return jdbcTemplate.queryForObject(query, rowMapper, id);
    }

    public Test update(Test newTest) throws Exception{
        String query = """
            UPDATE
                test
            SET
                value = ?
            WHERE
                id = ?        
        """;

        int rowsAffected = jdbcTemplate.update(query, newTest.getValue(), newTest.getId());
        if (rowsAffected == 0) throw new Exception();
        
        return newTest;
    }

    public Test delete(Long id) throws Exception{
        String sql = """
            DELETE 
            FROM 
                test 
            WHERE 
                id = ?
            RETURNING id, value
        """;
        Test result = jdbcTemplate.queryForObject(sql, rowMapper, id);

        return result;
    }
}
