package com.template.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.template.contracts.TemplateContract;
import com.template.states.TemplateState;
import net.corda.core.flows.*;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

// ***************
// * Responder flow *
// ***************
@InitiatedBy(IssueMetal.class)
public class IssueMetalResponder extends FlowLogic<SignedTransaction> {
    private FlowSession otherPartySession;

    public IssueMetalResponder(FlowSession otherPartySession) {
        this.otherPartySession = otherPartySession;
    }

    @Suspendable
    @Override
    public SignedTransaction call() throws FlowException {
        // Responder flow logic goes here.

        System.out.println("Received Metal");
        return subFlow(new ReceiveFinalityFlow(otherPartySession));
    }


}