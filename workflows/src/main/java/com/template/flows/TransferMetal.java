package com.template.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.template.contracts.MetalContract;
import com.template.states.MetalState;
import net.corda.core.contracts.Command;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.node.services.Vault;
import net.corda.core.node.services.vault.IQueryCriteriaParser;
import net.corda.core.node.services.vault.QueryCriteria;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;
import org.jetbrains.annotations.NotNull;

import javax.persistence.criteria.Predicate;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@InitiatingFlow
@StartableByRPC
public class TransferMetal extends FlowLogic<SignedTransaction>{

    private String metalName;
    private int weight;
    private Party newOwner;
    private int input = 0;

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

    public TransferMetal(String metalName, int weight, Party newOwner) {
        this.metalName = metalName;
        this.weight = weight;
        this.newOwner = newOwner;
    }

    @Override
    public ProgressTracker getProgressTracker() {return progressTracker;}

    StateAndRef<MetalState> checkForMetalStates() throws FlowException {
        QueryCriteria generalCriteria = new QueryCriteria.VaultQueryCriteria(Vault.StateStatus.UNCONSUMED);
        List<StateAndRef<MetalState>> MetalStates= getServiceHub().getVaultService().queryBy(MetalState.class, generalCriteria).getStates();

        boolean inputFound = false;
        int t = MetalStates.size();

        for (int x = 0; x < t; x++) {
            if (MetalStates.get(x).getState().getData().getMetalName().equals(metalName)
                    && MetalStates.get(x).getState().getData().getWeight() == weight) {
                input = x;
                inputFound = true;
            }
        }

        if (inputFound) {
            System.out.println("Input found");
        } else {
            System.out.println("Input not found");
            throw new FlowException();
        }
        return  MetalStates.get(input);
    }


    @Suspendable
    @Override
    public SignedTransaction call() throws FlowException{
        // Initiator flow logic goes here.

        // Retrieve notary identity
        progressTracker.setCurrentStep(RETRIEVING_NOTARY);
        Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0);

        StateAndRef<MetalState> inputState = null;

        inputState = checkForMetalStates();

        Party issuer = inputState.getState().getData().getIssuer();


        // Create transaction component
        MetalState outputState = new MetalState(metalName, weight, issuer, newOwner);
        Command cmd = new Command(new MetalContract.transfer(), getOurIdentity().getOwningKey());

        // Create transaction builder
        progressTracker.setCurrentStep(GENERATING_TRANSACTION);
        TransactionBuilder txb = new TransactionBuilder(notary)
                .addOutputState(outputState, MetalContract.CID)
                .addCommand(cmd);

        txb.addInputState(inputState);

        // Sign the transaction
        progressTracker.setCurrentStep(SIGNING_TRANSACTION);
        SignedTransaction signedTx = getServiceHub().signInitialTransaction(txb);

        // Create session with counterparty
        progressTracker.setCurrentStep(COUNTERPARTY_SESSION);
        FlowSession otherPartySession = initiateFlow(newOwner);
        FlowSession mintPartySession = initiateFlow(issuer);

        // Finalize the transaction
        progressTracker.setCurrentStep(FINALIZE_TRANSACTION);
        return subFlow(new FinalityFlow(signedTx, otherPartySession, mintPartySession));

    }
}