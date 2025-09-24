package com.kopi.belimang.test.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.kopi.belimang.test.dto.TestRequestBody;
import com.kopi.belimang.test.entity.Test;
import com.kopi.belimang.test.entity.Test.TestBuilder;
import com.kopi.belimang.test.repository.TestRepository;

@Service
public class TestService {

    @Autowired
    private TestRepository testRepository;
    
    public Test saveTest(TestRequestBody testRequestBody) throws Exception {
        return testRepository.insert(
            Test.builder()
                .value(testRequestBody.getValue())
                .build()
        );
    }

    public List<Test> getAllTest() throws Exception {
        return testRepository.getAll();
    }

    public Test getTestById(String idString) throws Exception {
        Long id;
        try {
            id = Long.parseLong(idString);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid ID: " + idString);
        }

        Test result = testRepository.getById(id);
        if (result == null) throw new Exception();

        return result;
    }    

    public Test update(String idString, TestRequestBody testRequestBody) throws IllegalArgumentException, Exception {
        Long id;
        try {
            id = Long.parseLong(idString);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid ID: " + idString);
        }

        TestBuilder builder = Test.builder().id(id);

        if (testRequestBody.getValue() != null && !testRequestBody.getValue().isEmpty())
            builder = builder.value(testRequestBody.getValue());

        return testRepository.update(builder.build());
    }

    public Test delete(String idString) throws IllegalArgumentException, Exception {
        Long id;
        try {
            id = Long.parseLong(idString);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid ID: " + idString);
        }

        return testRepository.delete(id);
    }

}
