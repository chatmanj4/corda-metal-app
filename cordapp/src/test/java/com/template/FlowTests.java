package com.template;

import com.google.common.collect.ImmutableList;
import com.template.flows.Responder;
import net.corda.testing.node.MockNetwork;
import net.corda.testing.node.StartedMockNode;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class FlowTests {
    private final MockNetwork network = new MockNetwork(ImmutableList.of("com.template"));
    private final StartedMockNode a = network.createNode();
    private final StartedMockNode b = network.createNode();

    public FlowTests() {
        a.registerInitiatedFlow(Responder.class);
        b.registerInitiatedFlow(Responder.class);
    }

    @Before
    public void setup() {
        network.runNetwork();
    }

    @After
    public void tearDown() {
        network.stopNodes();
    }

    @Test
    public void dummyTest() {

    }
}
