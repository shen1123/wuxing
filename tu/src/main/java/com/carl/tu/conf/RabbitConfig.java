package com.carl.tu.conf;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author WIN10
 * @date 2021-07-14 16:30
 */
@Configuration
public class RabbitConfig {

    @Bean
    public Queue rabbit() {
        return new Queue("rabbit",true);
    }

}
