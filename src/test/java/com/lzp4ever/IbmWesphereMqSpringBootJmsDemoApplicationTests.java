package com.lzp4ever;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jms.core.JmsOperations;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringRunner.class)
@SpringBootTest
public class IbmWesphereMqSpringBootJmsDemoApplicationTests {

    @Autowired
    private JmsOperations jmsOperations;

    @Test
    public void contextLoads() {
    }

    @Test
    private void sendMsg() {
        // replace first param with the queue name
        jmsOperations.convertAndSend("<queueName>", "hello world");
    }

    @Test
    private void receiveMsg() {
        // replace first param with the queue name
        jmsOperations.receiveAndConvert("<queueName>");
    }

    @Test
    @Transactional(value = "jmsTransactionManager")
    private void transaction() {
        // replace first param with the queue name
        jmsOperations.convertAndSend("<queueName>", "transaction test");
    }


}
