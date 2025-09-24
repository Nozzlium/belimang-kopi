package com.kopi.belimang.test;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kopi.belimang.test.dto.TestRequestBody;
import com.kopi.belimang.test.entity.Test;
import com.kopi.belimang.test.service.TestService;


@RestController
@RequestMapping("/test")
public class TestController {
    
    @Autowired
    private TestService testService;

    @PostMapping()
    public ResponseEntity<Test> create(@RequestBody TestRequestBody requestBody) throws Exception {
        Test createdTest = testService.saveTest(requestBody);
        return ResponseEntity.ok(createdTest);
    }

    @GetMapping()
    public ResponseEntity<List<Test>> findAll() throws Exception {
        List<Test> tests = testService.getAllTest();
        return ResponseEntity.ok(tests);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Test> findById(@PathVariable String id) throws Exception {
        Test test = testService.getTestById(id);
        return ResponseEntity.ok(test);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Test> updateById(@RequestBody TestRequestBody requestBody, @PathVariable String id) throws Exception {
        Test updatedTest = testService.update(id, requestBody);
        return ResponseEntity.ok(updatedTest);
    }

}
