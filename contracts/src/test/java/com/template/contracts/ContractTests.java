package com.template.contracts;

import com.template.states.MetalState;
import com.template.states.TemplateState;
import net.corda.core.contracts.Command;
import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.Contract;
import net.corda.core.identity.CordaX500Name;
import net.corda.testing.contracts.DummyState;
import net.corda.testing.core.DummyCommandData;
import net.corda.testing.core.TestIdentity;
import net.corda.testing.node.MockServices;
import org.junit.Test;

import java.security.PublicKey;
import java.util.Arrays;
import java.util.List;

import static net.corda.testing.node.NodeTestUtils.ledger;
import static net.corda.testing.node.NodeTestUtils.transaction;


public class ContractTests {
    private final TestIdentity Mint = new TestIdentity(new CordaX500Name("Mint","", "GB"));
    private final TestIdentity TraderA = new TestIdentity(new CordaX500Name("TraderA","", "US"));
    private final TestIdentity TraderB = new TestIdentity(new CordaX500Name("TraderB","", "US"));

    private final MockServices ledgerServices = new MockServices(
            Arrays.asList("com.template.contracts") // Add your contract package here
    );

    private MetalState metalState = new MetalState("Gold", 10, Mint.getParty(), TraderA.getParty());
    private MetalState metalStateInput = new MetalState("Gold", 10, Mint.getParty(), TraderA.getParty());
    private MetalState metalStateOutput = new MetalState("Gold", 10, Mint.getParty(), TraderB.getParty());

    public void metalContractImplementsContracts(){
        assert(new MetalContract() instanceof Contract);
    }

    //ISSUE COMMAND TEST CASES

//    @Test
//    public void metalContractRequiresZeroInputsInTheTransaction(){
//        transaction(ledgerServices, tx -> {
//            tx.input(MetalContract.CID, metalState);
//            tx.command(Mint.getPublicKey(), (CommandData) new MetalContract.issue());
//            tx.fails();
//            return null;
//        });
//
//        transaction(ledgerServices, tx -> {
//            tx.output(MetalContract.CID, metalState);
//            tx.command(Mint.getPublicKey(), new MetalContract.issue());
//            tx.verifies();
//            return null;
//        });
//    }

    @Test
    public void metalContractRequiresZeroInputsInTheTransaction() {
        transaction(ledgerServices, tx -> {
            tx.input(MetalContract.CID, metalState);
            CommandData commandData = new MetalContract.issue();
            List<PublicKey> signers = Arrays.asList(Mint.getPublicKey());
            tx.command(signers, commandData);
            tx.fails();
            return null;
        });

        transaction(ledgerServices, tx -> {
            tx.output(MetalContract.CID, metalState);
            CommandData commandData = new MetalContract.issue();
            List<PublicKey> signers = Arrays.asList(Mint.getPublicKey());
            tx.command(signers, commandData);
            // Add debug print
            tx.verifies();
            return null;
        });
    }

    @Test
    public void metalContractRequiresOneOutputInIssueTransaction(){
        transaction(ledgerServices, tx -> {
            tx.output(MetalContract.CID, metalState);
            tx.output(MetalContract.CID, metalState);
            CommandData commandData = new MetalContract.issue();
            List<PublicKey> signers = Arrays.asList(Mint.getPublicKey());
            tx.command(signers, commandData);
            tx.fails();
            return null;
        });

        transaction(ledgerServices, tx -> {
            tx.output(MetalContract.CID, metalState);
            CommandData commandData = new MetalContract.issue();
            List<PublicKey> signers = Arrays.asList(Mint.getPublicKey());
            tx.command(signers, commandData);
            tx.verifies();
            return null;
        });
    }

    @Test
    public void metalContractRequiresTransactionOutputToBeAMetalState(){
        transaction(ledgerServices, tx -> {
            tx.output(MetalContract.CID, new DummyState());
            CommandData commandData = new MetalContract.issue();
            List<PublicKey> signers = Arrays.asList(Mint.getPublicKey());
            tx.command(signers, commandData);
            tx.fails();
            return null;
        });

        transaction(ledgerServices, tx -> {
            tx.output(MetalContract.CID, metalState);
            CommandData commandData = new MetalContract.issue();
            List<PublicKey> signers = Arrays.asList(Mint.getPublicKey());
            tx.command(signers, commandData);
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

    //TRANSFER COMMAND TEST CASES
    @Test
    public void metalContractRequiresOneInputAndOneOutputInTheTransaction(){
        transaction(ledgerServices, tx -> {
            tx.input(MetalContract.CID, metalStateInput);
            tx.output(MetalContract.CID, metalStateOutput);
            tx.command(TraderA.getPublicKey(), new MetalContract.transfer());
            tx.verifies();
            return null;
        });

        transaction(ledgerServices, tx -> {
            tx.output(MetalContract.CID, metalStateOutput);
            tx.command(TraderA.getPublicKey(), new MetalContract.transfer());
            tx.fails();
            return null;
        });

        transaction(ledgerServices, tx -> {
            tx.input(MetalContract.CID, metalStateInput);
            tx.command(TraderA.getPublicKey(), new MetalContract.transfer());
            tx.fails();
            return null;
        });
    }

    @Test
    public void metalContractRequiresTheTransactionCommandToBeATransferCommand(){
        transaction(ledgerServices, tx -> {
            tx.input(MetalContract.CID, metalStateInput);
            tx.output(MetalContract.CID, metalStateOutput);
            tx.command(TraderA.getPublicKey(), DummyCommandData.INSTANCE);
            tx.fails();
            return null;
        });

        transaction(ledgerServices, tx -> {
            tx.input(MetalContract.CID, metalStateInput);
            tx.output(MetalContract.CID, metalStateOutput);
            CommandData commandData = new MetalContract.transfer();
            List<PublicKey> signers = Arrays.asList(TraderA.getPublicKey());
            tx.command(signers, commandData);
            tx.verifies();
            return null;
        });
    }

    @Test
    public void metalContractRequiresTheOwnerToBeARequiredSigner(){
        transaction(ledgerServices, tx -> {
            tx.input(MetalContract.CID, metalStateInput);
            tx.output(MetalContract.CID, metalStateOutput);
            tx.command(TraderA.getPublicKey(), new MetalContract.transfer());
            tx.verifies();
            return null;
        });

        transaction(ledgerServices, tx -> {
            tx.input(MetalContract.CID, metalStateInput);
            tx.output(MetalContract.CID, metalStateOutput);
            tx.command(Mint.getPublicKey(), new MetalContract.transfer());
            tx.fails();
            return null;
        });
    }

}