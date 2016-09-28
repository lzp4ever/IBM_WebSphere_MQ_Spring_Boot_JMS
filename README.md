# 前言
Spring Boot 可以大幅减轻项目中各种繁杂的配置，这几天在国内外网站都没找到 IBM WebSphere MQ 与 Spring Boot JMS 集成的相关资料。在这篇，会详细讲述两者集成的方法。
# 准备
- 开发工具 Intellij Idea + Maven
在 idea 中开启 Spring Boot 插件，可以让我们很方便的生成 Spring Boot 项目
- IBM WebSphere MQ 配置信息（MQ的安装流程配置流程不在此列出）
    - 接收端
      - IP：192.168.1.25
      - 端口：1414
        - 队列管理器：LINUX_QM
        - 通道：LINUX_SVRCONN
        - 队列：LINUX_Q
    - 发送端
       - IP：192.168.1.28
      - 端口：1414
        - 队列管理器：WIN_QM
        - 通道：WIN_SVRCONN
        - 队列：LINUX_Q
# 详细流程
## 配置 Maven 管理依赖
首先我们需要 MQ 相关的 jar 包，**com.ibm.mq.allclient.jar**，该包位于MQ的安装目录下。我们需要将其提取出来并安装到本地 maven 库。groupId 设置为 com.ibm.mq，artifactId 设置为 allclient。
安装命令：
```
mvn install:install-file -Dfile=[jar包所在路径] -DgroupId=com.ibm.mq -DartifactId=allclient -Dversion=1.0 -Dpackaging=jar
```

配置项目的 maven 依赖文件，添加Spring Boot JMS，JAVAX JMS
```
<!-- activemq 即是 Spring Boot JMS-->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-activemq</artifactId>
</dependency>
 
<!-- 安装到本地的 IBM WebSphere MQ 相关jar包-->
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
## 向 properties 文件添加 MQ 配置信息
共有三个 properties 文件，保存发送端信息，接收端信息，application.properties 用以切换发送和接收
**active=send**，表示目前是读取发送端的配置信息
- 发送端：application-send.properties
```
mq.host=192.168.1.28
mq.port=1414
mq.queueManager=WIN_QM
mq.channel=WIN_SVRCONN
mq.queue=LINUX_Q
```
- 接收端：application-receive.properties
```
mq.host=192.168.1.25
mq.port=1414
mq.queueManager=LINUX_QM
mq.channel=LINUX_SVRCONN
mq.queue=LINUX_Q
```
- application.properties
```
spring.profiles.active=send
```
## 由 MQ 的配置信息生成 MQBMConnectionFactory
```
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

private MQBMConnectionFactory mqbmConnectionFactory() {
        MQBMConnectionFactory mqbmConnectionFactory = new MQBMConnectionFactory();
        mqbmConnectionFactory.setHostName(host);
        try {
            mqbmConnectionFactory.setTransportType(1);
            mqbmConnectionFactory.setCCSID(1381);
            mqbmConnectionFactory.setChannel(channel);
            mqbmConnectionFactory.setPort(port);
            mqbmConnectionFactory.setQueueManager(queueManager);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mqbmConnectionFactory;
    }
```
## 配置 JmsTemplate，方法名为 mqJmsTemplate()
```
@Bean
public JmsTemplate mqJmsTemplate() {
        JmsTemplate jmsTemplate = new JmsTemplate();
        jmsTemplate.setConnectionFactory(mqbmConnectionFactory());
        jmsTemplate.setDefaultDestinationName(queue);
        return jmsTemplate;
}
```
## 使用 mqJmsTemplate 完成 MQ 写入操作
使用 **mqJmsTemplate.convertAndSend(String string)** 即可很方便的向 MQ 写入数据
```
private final JmsTemplate mqJmsTemplate;
 
@Autowired
public IbmWebSphereMqSpringBootJmsApplication(JmsTemplate mqJmsTemplate) {
    this.mqJmsTemplate = mqJmsTemplate;
}
 
@Override
public void run(String... strings) throws Exception {
    mqJmsTemplate.convertAndSend("Hello World!");
    //mqJmsTemplate.receiveAndConvert();
}
```
需要从MQ获取数据时，把 application.properties 的 spring.profiles.active=send 改为 receive，使用  **mqJmsTemplate.receiveAndConvert()** 即可从接收端获取数据。

