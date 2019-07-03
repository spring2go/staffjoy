package xyz.staffjoy.faraday.core.balancer;

import java.util.List;

import static java.util.concurrent.ThreadLocalRandom.current;

public class RandomLoadBalancer implements LoadBalancer {
    @Override
    public String chooseDestination(List<String> destnations) {
        int hostIndex = destnations.size() == 1 ? 0 : current().nextInt(0, destnations.size());
        return destnations.get(hostIndex);
    }
}
