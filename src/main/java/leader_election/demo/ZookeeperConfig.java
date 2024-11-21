package leader_election.demo;

import org.apache.zookeeper.ZooKeeper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class ZookeeperConfig {
    @Value("${zookeeper.host}")
    private String zkHost;

    @Bean
    public ZooKeeper zooKeeper() throws IOException {
        return new ZooKeeper(zkHost, 3000, null);
    }
}
