package leader_election.demo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ServiceController {
    private final LeaderElectionService leaderElectionService;

    public ServiceController(LeaderElectionService leaderElectionService) {
        this.leaderElectionService = leaderElectionService;
    }

    @GetMapping("/status")
    public String getServiceStatus() {
        return "This service is " + leaderElectionService.getServiceRole();
    }
}

