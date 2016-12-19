# IBM WebSphere MQ integration with Spring Boot MQ (JavaConfig)
## Maven Dependencies
### Install`com.ibm.mq.allclient.jar`
`com.ibm.mq.allclient.jar` is located at `[MQ installed path]/java/lib/`. Find it and install it to your local maven repository.
```sh
mvn install:install-file -Dfile=[jar path] -DgroupId=com.ibm.mq -DartifactId=allclient -Dversion=1.0 -Dpackaging=jar
```
### Add Dependencies to the Project
- Spring-activeMQ
- IBM MQ Allclient
- Javax JMS

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-activemq</artifactId>
</dependency>

<dependency>
    <groupId>com.ibm.mq</groupId>
    <artifactId>allclient</artifactId>
    <version>1.0</version>
</dependency>

<dependency>
    <groupId>javax.jms</groupId>
    <artifactId>javax.jms-api</artifactId>
    <version>2.0.1</version>
</dependency>
```
## JavaConfig
### Configure MQ Rroperties and Read it in Project
```java
@Value("${mq.queueManager}")
private String queueManager;

@Value("${mq.channel}")
private String channel;

@Value("${mq.host}")
private String host;

@Value("${mq.port}")
private Integer port;

@Value("${mq.queue}")
private String queue;
```
### Configure `MQBMConnectionFactory`
```java
@Bean
public MQQueueConnectionFactory mqQueueConnectionFactory() {
    MQQueueConnectionFactory mqQueueConnectionFactory = new MQQueueConnectionFactory();
    mqQueueConnectionFactory.setHostName(host);
    try {
        mqQueueConnectionFactory.setTransportType(WMQConstants.WMQ_CM_CLIENT);
        mqQueueConnectionFactory.setCCSID(1381);
        mqQueueConnectionFactory.setChannel(channel);
        mqQueueConnectionFactory.setPort(port);
        mqQueueConnectionFactory.setMsgBatchSize(10000);
        mqQueueConnectionFactory.setQueueManager(queueManager);
    } catch (Exception e) {
        e.printStackTrace();
    }
    return mqQueueConnectionFactory;
}
```
### Configure `CachingConnectionFactory`
Use `@Primary` annotation to tell Spring use this bean but not `MQQueueConnectionFactory`.
```java
@Bean
@Primary
public CachingConnectionFactory cachingConnectionFactory(@Qualifier("mqQueueConnectionFactory") MQQueueConnectionFactory mqQueueConnectionFactory) {
    CachingConnectionFactory cachingConnectionFactory = new CachingConnectionFactory();
    cachingConnectionFactory.setTargetConnectionFactory(mqQueueConnectionFactory);
    cachingConnectionFactory.setSessionCacheSize(10000);
    cachingConnectionFactory.setReconnectOnException(true);
    return cachingConnectionFactory;
}
```
### Configure `JmsTransactionManager` (Optional)
If you use transaction
```java
@Bean
public JmsTransactionManager jmsTransactionManager(@Qualifier("cachingConnectionFactory") CachingConnectionFactory cachingConnectionFactory) {
    JmsTransactionManager jmsTransactionManager = new JmsTransactionManager();
    jmsTransactionManager.setConnectionFactory(cachingConnectionFactory);
    return jmsTransactionManager;
}
```
### Configure `JmsOperations`
```java
@Bean
public JmsOperations jmsOperations(@Qualifier("cachingConnectionFactory") CachingConnectionFactory cachingConnectionFactory) {
    JmsTemplate jmsTemplate = new JmsTemplate(cachingConnectionFactory);
    jmsTemplate.setSessionTransacted(true);
    jmsTemplate.setDefaultDestinationName(queue);
    jmsTemplate.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
    jmsTemplate.setMessageIdEnabled(false);
    jmsTemplate.setMessageTimestampEnabled(false);
    jmsTemplate.setPubSubNoLocal(true);
    jmsTemplate.setExplicitQosEnabled(true);
    jmsTemplate.setReceiveTimeout(receiveTimeout);
    return jmsTemplate;
}
```
## Operation Example
Now you can inject `jmsOperations` to operate IBM WebSphere MQ Simply.

```java
@Autowired
private JmsOperations jmsOperations;
```
### Send Message to MQ
```java
public void send() {
    jmsOperations.convertAndSend("Hello world");
}
```
### Receive Message to MQ
```java
public void receive() {
    String s = (String) jmsOperations.receiveAndConvert();
}
```
## transaction Example
Inject `JmsTransactionManager` to use transaction.
```java
@Autowired
private JmsTransactionManager jmsTransactionManager;
```
### Commit
```java
public void commit(){
    TransactionStatus transactionStatus = jmsTransactionManager.getTransaction(new DefaultTransactionDefinition());
    for (int i = 0; i < 2000; i++) {
        jmsOperations.convertAndSend("hello world");
    }
    jmsTransactionManager.commit(transactionStatus);
}
```
### Rollback
```java
public void rollback() {
    TransactionStatus transactionStatus = jmsTransactionManager.getTransaction(new DefaultTransactionDefinition());
    for (int i = 0; i < 2000; i++) {
        jmsOperations.convertAndSend("hello world");
    }
    jmsTransactionManager.rollback(transactionStatus);
}
```
