package com.template.contracts;

import com.template.states.MetalState;
import com.template.states.TemplateState;
import net.corda.core.contracts.Contract;
import net.corda.core.identity.CordaX500Name;
import net.corda.testing.contracts.DummyState;
import net.corda.testing.core.DummyCommandData;
import net.corda.testing.core.TestIdentity;
import net.corda.testing.node.MockServices;
import org.junit.Test;

import java.util.Arrays;

import static net.corda.testing.node.NodeTestUtils.ledger;
import static net.corda.testing.node.NodeTestUtils.transaction;


public class ContractTests {
    private final TestIdentity Mint = new TestIdentity(new CordaX500Name("mint","", "GB"));
    private final TestIdentity TraderA = new TestIdentity(new CordaX500Name("traderA","", "US"));
    private final TestIdentity TraderB = new TestIdentity(new CordaX500Name("traderB","", "US"));

    private final MockServices ledgerServices = new MockServices(Arrays.asList("com.template"));

    private MetalState metalState = new MetalState("Gold", 10, mint.getParty(), TraderA.getParty());
    private MetalState metalStateInput = new MetalState("Gold", 10, mint.getParty(), TraderA.getParty());
    private MetalState metalStateOutput = new MetalState("Gold", 10, mint.getParty(), TraderB.getParty());

    public void metalContractImplementsContracts(){
        assert(new MetalContract() instanceof Contract);
    }

    //ISSUE COMMAND TEST CASES

    @Test
    public void metalContractRequiresZeroInputsInTheTransaction(){
        transaction(ledgerServices, tx -> {
            tx.input(MetalContract.CID, metalState);
            tx.command(Mint.getPublicKey(), new MetalContract.issue());
            tx.fails();
            return null;
        });

        transaction(ledgerServices, tx -> {
            tx.output(MetalContract.CID, metalState);
            tx.command(Mint.getPublicKey(), new MetalContract.issue());
            tx.verifies();
            return null;
        });
    }


    @Test
    public void metalContractRequiresOneOutputInIssueTransaction(){
        transaction(ledgerServices, tx -> {
            tx.output(MetalContract.CID, metalState);
            tx.output(MetalContract.CID, metalState);
            tx.command(Mint.getPublicKey(), new MetalContract.issue());
            tx.fails();
            return null;
        });

        transaction(ledgerServices, tx -> {
            tx.output(MetalContract.CID, metalState);
            tx.command(Mint.getPublicKey(), new MetalContract.issue());
            tx.verifies();
            return null;
        });
    }

    @Test
    public void metalContractRequiresTransactionOutputToBeAMetalState(){
        transaction(ledgerServices, tx -> {
            tx.output(MetalContract.CID, new DummyState());
            tx.command(Mint.getPublicKey(), new MetalContract.issue());
            tx.fails();
            return null;
        });

        transaction(ledgerServices, tx -> {
            tx.output(MetalContract.CID, metalState);
            tx.command(Mint.getPublicKey(), new MetalContract.issue());
            tx.verifies();
            return null;
        });
    }

    @Test
    public void metalContractRequiresTransactionCommandToBeAnIssueCommand(){
        transaction(ledgerServices, tx -> {
            tx.output(MetalContract.CID, metalState);
            tx.command(Mint.getPublicKey(), DummyCommandData.INSTANCE);
            tx.fails();
            return null;
        });

        transaction(ledgerServices, tx -> {
            tx.output(MetalContract.CID, metalState);
            tx.command(Mint.getPublicKey(), new MetalContract.issue());
            tx.verifies();
            return null;
        });
    }

    @Test
    public void metalContractRequiresTheIssuerToBeARequiredSignerInTheTransaction(){
        transaction(ledgerServices, tx -> {
            tx.output(MetalContract.CID, metalState);
            tx.command(TraderA.getPublicKey(), DummyCommandData.INSTANCE);
            tx.fails();
            return null;
        });

        transaction(ledgerServices, tx -> {
            tx.output(MetalContract.CID, metalState);
            tx.command(Mint.getPublicKey(), new MetalContract.issue());
            tx.verifies();
            return null;
        });
    }
}