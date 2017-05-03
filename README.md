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

application.yml
```yaml
project: 
  mq:
    host: 192.168.1.180
    port: 1416
    queue-manager: QM
    channel: mqm.SVRCONN   # SVRCONN
    username: mqm
    password: 123456
    receive-timeout: 2000
```

```java
@Configuration
public class JmsConfig {   
    @Value("${project.mq.host}")
    private String host;
    @Value("${project.mq.port}")
    private Integer port;
    @Value("${project.mq.queue-manager}")
    private String queueManager;
    @Value("${project.mq.channel}")
    private String channel;
    @Value("${project.mq.username}")
    private String username;
    @Value("${project.mq.password}")
    private String password;
    @Value("${project.mq.receive-timeout}")
    private long receiveTimeout;
}
```
### Configure `MQBMConnectionFactory`

**CCISD has to be the same within the Queue Manager, 1208 is UTF-8**

```java
@Bean
public MQQueueConnectionFactory mqQueueConnectionFactory() {
    MQQueueConnectionFactory mqQueueConnectionFactory = new MQQueueConnectionFactory();
    mqQueueConnectionFactory.setHostName(host);
    try {
        mqQueueConnectionFactory.setTransportType(WMQConstants.WMQ_CM_CLIENT);
        mqQueueConnectionFactory.setCCSID(1208);
        mqQueueConnectionFactory.setChannel(channel);
        mqQueueConnectionFactory.setPort(port);
        mqQueueConnectionFactory.setQueueManager(queueManager);
    } catch (Exception e) {
        e.printStackTrace();
    }
    return mqQueueConnectionFactory;
}
```

### Config 'UserCredentialsConnectionFactoryAdapter'
If you have to connect with Username and Password
```java
@Bean
UserCredentialsConnectionFactoryAdapter userCredentialsConnectionFactoryAdapter(MQQueueConnectionFactory mqQueueConnectionFactory) {
    UserCredentialsConnectionFactoryAdapter userCredentialsConnectionFactoryAdapter = new UserCredentialsConnectionFactoryAdapter();
    userCredentialsConnectionFactoryAdapter.setUsername(username);
    userCredentialsConnectionFactoryAdapter.setPassword(password);
    userCredentialsConnectionFactoryAdapter.setTargetConnectionFactory(mqQueueConnectionFactory);
    return userCredentialsConnectionFactoryAdapter;
}
```

### Configure `CachingConnectionFactory`
Use `@Primary` annotation to tell Spring use this bean but not `MQQueueConnectionFactory`.
```java
@Bean
@Primary
public CachingConnectionFactory cachingConnectionFactory(UserCredentialsConnectionFactoryAdapter userCredentialsConnectionFactoryAdapter) {
    CachingConnectionFactory cachingConnectionFactory = new CachingConnectionFactory();
    cachingConnectionFactory.setTargetConnectionFactory(userCredentialsConnectionFactoryAdapter);
    cachingConnectionFactory.setSessionCacheSize(500);
    cachingConnectionFactory.setReconnectOnException(true);
    return cachingConnectionFactory;
}
```
### Configure `JmsTransactionManager` (Optional)
If you use transaction
```java
@Bean
public PlatformTransactionManager jmsTransactionManager(CachingConnectionFactory cachingConnectionFactory) {
    JmsTransactionManager jmsTransactionManager = new JmsTransactionManager();
    jmsTransactionManager.setConnectionFactory(cachingConnectionFactory);
    return jmsTransactionManager;
}
```
### Configure `JmsOperations`

**Have to set Receive Timeout, or Receive Method would hang if queue is empty**

```java
@Bean
public JmsOperations jmsOperations(CachingConnectionFactory cachingConnectionFactory) {
    JmsTemplate jmsTemplate = new JmsTemplate(cachingConnectionFactory);
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
**If you wanna send and Receive Object, that object have to implement Serializable Interface. Also Setting the SerialID is Better**
```java
@Autowired
JmsOperations jmsOperations;

public void send(User user){
  jmsOperations.convertAndSend("QUEUE.USER", user);
}
```
### Receive Message to MQ
```java
@Autowired
JmsOperations jmsOperations;
public void receive(User user){
  jmsOperations.receiveAndConvert("QUEUE.USER");
}
```

