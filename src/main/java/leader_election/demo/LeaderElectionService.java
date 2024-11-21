package leader_election.demo;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.Watcher;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;




import java.util.UUID;

@Service
public class LeaderElectionService {
    private static final String LEADER_NODE = "/leader";
    private final CuratorFramework zkClient;
    private final String serviceId;

    public LeaderElectionService(CuratorFramework zkClient) {
        this.zkClient = zkClient;
        this.serviceId = UUID.randomUUID().toString();
    }
    @PostConstruct
    public void initializeLeaderElection() {
        try {
            electLeader();
            watchLeaderChanges();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getServiceRole() {
        try {
            String leader = new String(zkClient.getData().forPath(LEADER_NODE));
            return leader.equals(serviceId) ? "LEADER" : "WORKER";
        } catch (Exception e) {
            return "WORKER";
        }
    }

    public void electLeader() throws Exception {
        try {
                zkClient.create().withMode(CreateMode.EPHEMERAL).forPath(LEADER_NODE, serviceId.getBytes());
                System.out.println("Service " + serviceId + " is now the leader.");
        } catch (Exception e) {
            System.out.println("Service " + serviceId + " is a worker.");
        }
    }


    public void watchLeaderChanges() throws Exception {
        zkClient.getData().usingWatcher((CuratorWatcher) event -> {
            if (event.getType() == org.apache.zookeeper.Watcher.Event.EventType.NodeDeleted) {
                try {
                    electLeader();
                    watchLeaderChanges();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).forPath(LEADER_NODE);
    }

    }
