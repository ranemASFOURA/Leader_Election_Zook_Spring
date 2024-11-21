package leader_election.demo;

import jakarta.annotation.PostConstruct;
import org.apache.zookeeper.*;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class LeaderElectionService implements Watcher {
    private static final String ELECTION = "election"; // Path for the election node in ZooKeeper
    private final ZooKeeper zookeeper;
    private String myCandidate;

    // Constructor to initialize ZooKeeper client
    public LeaderElectionService(ZooKeeper zookeeper) {
        this.zookeeper = zookeeper;
    }

    // Method executed after the object is constructed to initialize the leader election
    @PostConstruct
    public void initializeLeaderElection() {
        try {
            addCandidate();
            election();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Method to determine the service's role: LEADER or WORKER
    public String getServiceRole() {
        try {
            List<String> children = zookeeper.getChildren(ELECTION, false); // Retrieve all candidate nodes
            Collections.sort(children); // Sort nodes by their sequence
            return children.get(0).equals(myCandidate) ? "LEADER" : "WORKER"; // Check if this instance is the leader
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
            return "UNKNOWN"; // Return UNKNOWN if an error occurs
        }
    }

    // Method to add this instance as a candidate in the election
    private void addCandidate() throws KeeperException, InterruptedException {
        // Ensure the /election node exists; create it if it doesn't
        if (zookeeper.exists(ELECTION, false) == null) {
            zookeeper.create(ELECTION, new byte[]{}, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        }

        // Create an ephemeral sequential node for this candidate
        String path = zookeeper.create(ELECTION + "/c", new byte[]{}, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
        this.myCandidate = path.replace(ELECTION + "/", ""); // Extract the node name
    }

    // Method to handle the leader election process
    private void election() throws KeeperException, InterruptedException {
        List<String> children = zookeeper.getChildren(ELECTION, this); // Retrieve all candidate nodes and watch for changes
        Collections.sort(children); // Sort nodes by their sequence

        if (children.get(0).equals(myCandidate)) {
            // If this instance is the smallest node, it becomes the leader
            System.out.println("Service is now the LEADER.");
        } else {
            // If not the leader, this instance is a worker
            System.out.println("Service is a WORKER.");

            // Watch the predecessor node for deletion to trigger reelection
            String predecessor = children.get(Collections.binarySearch(children, myCandidate) - 1);
            zookeeper.exists(ELECTION + "/" + predecessor, this);
        }
    }

    // Method to handle ZooKeeper events
    @Override
    public void process(WatchedEvent event) {
        if (event.getType() == Event.EventType.NodeDeleted) {
            // Trigger election if a node is deleted
            try {
                election();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
