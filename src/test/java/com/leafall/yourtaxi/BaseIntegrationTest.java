package com.leafall.yourtaxi;

import com.leafall.yourtaxi.core.db.DbCleaner;
import com.leafall.yourtaxi.core.db.RedisCleaner;
import com.leafall.yourtaxi.initializer.TestContainersInitializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest(classes = YourTaxiApplication.class)
@ActiveProfiles("test")
@ContextConfiguration(initializers = TestContainersInitializer.class)
@AutoConfigureMockMvc
public class BaseIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected DbCleaner dbCleaner;
    @Autowired
    protected RedisCleaner redisCleaner;

}