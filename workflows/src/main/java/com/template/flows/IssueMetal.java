package com.template.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.template.contracts.MetalContract;
import com.template.contracts.TemplateContract;
import com.template.states.MetalState;
import com.template.states.TemplateState;
import net.corda.core.contracts.Command;
import net.corda.core.flows.*;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@InitiatingFlow
@StartableByRPC
public class IssueMetal extends FlowLogic<SignedTransaction>{

    private String metalName;
    private int weight;
    private Party owner;

    //Progress steps
    private final ProgressTracker.Step RETRIEVING_NOTARY = new ProgressTracker.Step("Retrieving the notary");
    private final ProgressTracker.Step GENERATING_TRANSACTION = new ProgressTracker.Step("Generating transaction");
    private final ProgressTracker.Step SIGNING_TRANSACTION = new ProgressTracker.Step("Signing transaction with our private key");
    private final ProgressTracker.Step COUNTERPARTY_SESSION = new ProgressTracker.Step("Sending flow to counterParty");
    private final ProgressTracker.Step FINALIZE_TRANSACTION = new ProgressTracker.Step("Obtaining notary signature and recording transaction");

    private final ProgressTracker progressTracker = new ProgressTracker(
            RETRIEVING_NOTARY,
            GENERATING_TRANSACTION,
            SIGNING_TRANSACTION,
            COUNTERPARTY_SESSION,
            FINALIZE_TRANSACTION
    );

    public IssueMetal(String metalName, int weight, Party owner) {
        this.metalName = metalName;
        this.weight = weight;
        this.owner = owner;
    }

    @Override
    public ProgressTracker getProgressTracker() {return progressTracker;}

    @Suspendable
    @Override
    public SignedTransaction call() throws FlowException{
        // Initiator flow logic goes here.

        // Retrieve notary identity
        progressTracker.setCurrentStep(RETRIEVING_NOTARY);
        Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0);

        // Create transaction component
        MetalState outputState = new MetalState(metalName, weight, getOurIdentity(), owner);
        Command cmd = new Command(new MetalContract.issue(), getOurIdentity().getOwningKey());

        // Create transaction builder
        progressTracker.setCurrentStep(GENERATING_TRANSACTION);
        TransactionBuilder txb = new TransactionBuilder(notary)
                .addOutputState(outputState, MetalContract.CID)
                .addCommand(cmd);

        // Sign the transaction
        progressTracker.setCurrentStep(SIGNING_TRANSACTION);
        SignedTransaction signedTx = getServiceHub().signInitialTransaction(txb);

        // Create session with counterparty
        progressTracker.setCurrentStep(COUNTERPARTY_SESSION);
        FlowSession otherPartySession = initiateFlow(owner);

        // Finalize the transaction
        progressTracker.setCurrentStep(FINALIZE_TRANSACTION);
        return subFlow(new FinalityFlow(signedTx, otherPartySession));

    }
}