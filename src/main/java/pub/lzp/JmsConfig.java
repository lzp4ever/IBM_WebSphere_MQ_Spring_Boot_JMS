package pub.lzp;

import com.ibm.msg.client.mqlight.factories.MQBMConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.core.JmsTemplate;

/**
 * Created by lzp4e on 2016/9/26.
 */
@Configuration
public class JmsConfig {

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

    @Bean
    public JmsTemplate mqJmsTemplate() {
        JmsTemplate jmsTemplate = new JmsTemplate();
        jmsTemplate.setConnectionFactory(mqbmConnectionFactory());
        jmsTemplate.setDefaultDestinationName(queue);
        return jmsTemplate;
    }

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
}
