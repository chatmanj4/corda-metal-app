package com.template.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.template.contracts.TemplateContract;
import com.template.states.MetalState;
import com.template.states.TemplateState;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.flows.*;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.core.node.services.Vault;
import net.corda.core.node.services.vault.QueryCriteria;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

// ******************
// * Initiator flow *
// ******************
@InitiatingFlow
@StartableByRPC
public class SearchVaultFlow extends FlowLogic<Void> {

    void searchForAllStates(){
        QueryCriteria consumedCriteria = new QueryCriteria.VaultQueryCriteria(Vault.StateStatus.CONSUMED);
        List<StateAndRef<MetalState>> consumedMetalStates= getServiceHub().getVaultService().queryBy(MetalState.class, consumedCriteria).getStates();

        if (consumedMetalStates.size() < 1) {
            System.out.println("no consumed metal states found");
        } else {
            System.out.println("consumed metal states found: " + consumedMetalStates.size());
        }

        int c = consumedMetalStates.size();
        for (int i = 0; i < c; i++) {
            System.out.println("name: " + consumedMetalStates.get(i).getState().getData().getMetalName());
            System.out.println("owner: " + consumedMetalStates.get(i).getState().getData().getOwner());
            System.out.println("weight: " + consumedMetalStates.get(i).getState().getData().getWeight());
            System.out.println("issuer: " + consumedMetalStates.get(i).getState().getData().getIssuer());
        }

        QueryCriteria unconsumedCriteria = new QueryCriteria.VaultQueryCriteria(Vault.StateStatus.UNCONSUMED);
        List<StateAndRef<MetalState>> unconsumedMetalStates= getServiceHub().getVaultService().queryBy(MetalState.class, unconsumedCriteria).getStates();

        if (consumedMetalStates.size() < 1) {
            System.out.println("no consumed metal states found");
        } else {
            System.out.println("consumed metal states found: " + unconsumedMetalStates.size());
        }

        int u = consumedMetalStates.size();
        for (int i = 0; i < u; i++) {
            System.out.println("name: " + unconsumedMetalStates.get(i).getState().getData().getMetalName());
            System.out.println("owner: " + unconsumedMetalStates.get(i).getState().getData().getOwner());
            System.out.println("weight: " + unconsumedMetalStates.get(i).getState().getData().getWeight());
            System.out.println("issuer: " + unconsumedMetalStates.get(i).getState().getData().getIssuer());
        }

    }

    @Suspendable
    @Override
    public Void call() throws FlowException {
        // Initiator flow logic goes here.

        searchForAllStates();

        return null;
    }
}