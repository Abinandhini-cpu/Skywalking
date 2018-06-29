package org.apache.skywalking.apm.webapp.proxy;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import com.netflix.loadbalancer.Server;
import com.netflix.loadbalancer.ServerList;
import com.netflix.loadbalancer.ZoneAwareLoadBalancer;
import org.springframework.cloud.netflix.ribbon.SpringClientFactory;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@DirtiesContext
public class NIWSServerListTest {
    @Autowired
    private Environment env;

    @Autowired
    private SpringClientFactory factory;

    private Map<String, String> serverListClassNames = new HashMap<String, String>();

    @Before
    public void initServerListClassNames() {
        for (Iterator<?> iter = ((AbstractEnvironment) env).getPropertySources().iterator(); iter.hasNext();) {
            Object propertySource = iter.next();
            if (propertySource instanceof MapPropertySource) {
                Map<String, Object> mapPropertySource = ((MapPropertySource) propertySource).getSource();
                for (Map.Entry<String, Object> entry : mapPropertySource.entrySet()) {
                    String key = entry.getKey();
                    int index;
                    if (key.endsWith(".NIWSServerListClassName") &&
                            (index = key.indexOf(".ribbon")) > 0) {
                        String clientName = key.substring(0, index);
                        serverListClassNames.put(clientName,(String)entry.getValue());
                    }
                }
            }
        }
    }
    @Test
    public void serverListClass() throws ClassNotFoundException {
        for (String serverListClassName : serverListClassNames.values()) {
            Class<?> clazz = Class.forName(serverListClassName);
        }
    }

    @Test
    public void serverListFliter() {
        for (Map.Entry<String, String> entry : serverListClassNames.entrySet()) {
            String clientName = entry.getKey();
            String serverListClassName = entry.getValue();
            ServerList<Server> serverList = getLoadBalancer(clientName).getServerListImpl();
            assertNotNull("Client: " + clientName + "'s ServerListImpl is null",serverList);
            assertEquals("Clinet: " + clientName + "'s ServerListImpl not Same with setting in configs",
                    serverListClassName, serverList.getClass().getName());
        }
    }

    @SuppressWarnings("unchecked")
    private ZoneAwareLoadBalancer<Server> getLoadBalancer(String name) {
        return (ZoneAwareLoadBalancer<Server>)this.factory.getLoadBalancer(name);
    }
}
