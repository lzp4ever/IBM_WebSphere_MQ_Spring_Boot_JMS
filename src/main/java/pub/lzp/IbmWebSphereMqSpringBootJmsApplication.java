package pub.lzp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jms.core.JmsTemplate;

@SpringBootApplication
public class IbmWebSphereMqSpringBootJmsApplication implements CommandLineRunner {


    public static void main(String[] args) {
        SpringApplication.run(IbmWebSphereMqSpringBootJmsApplication.class, args);
    }

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
}
