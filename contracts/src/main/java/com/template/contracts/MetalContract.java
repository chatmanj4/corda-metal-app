package com.template.contracts;

import com.template.states.MetalState;
import com.template.states.TemplateState;
import net.bytebuddy.pool.TypePool;
import net.corda.core.contracts.Command;
import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.Contract;
import net.corda.core.contracts.ContractState;
import net.corda.core.identity.Party;
import net.corda.core.transactions.LedgerTransaction;
import org.jetbrains.annotations.NotNull;

import java.security.PublicKey;
import java.util.List;

import static net.corda.core.contracts.ContractsDSL.requireThat;

public class MetalContract implements Contract {

    public static final String CID = "com.template.contracts.MetalContract";

    @Override
    public void verify(@NotNull LedgerTransaction tx) throws IllegalArgumentException {
        if (tx.getCommands().size() != 1){
            throw new IllegalArgumentException("the transaction must have one command");
        }
        Command command = tx.getCommand(0);
        CommandData commandType = command.getValue();

        List<PublicKey> requiredSigners = command.getSigners();

        //----------------------------------------issue command rules -----------------------------------------
        if(commandType instanceof issue){
            //shape rules
            if(tx.getInputs().size() != 0){
                throw new IllegalArgumentException("the issue command cannot have input");
            }
            if(tx.getOutputs().size() != 1){
                throw new IllegalArgumentException("the issue can only have one output");
            }

            //content rules
            ContractState outputState = tx.getOutput(0);
            if(!(outputState instanceof MetalState)){
                throw new IllegalArgumentException("output must be a metal state");
            }

            MetalState metalState = (MetalState) outputState;
            if(!(metalState.getMetalName().equals("Gold"))) {
                throw new IllegalArgumentException("Metal is not gold");
            }

            //signer rules
            Party issuer = metalState.getIssuer();
            PublicKey issuerKey = issuer.getOwningKey();

            if(!requiredSigners.contains(issuerKey)){
                throw new IllegalArgumentException("Issuer has to sign the issuance");
            }
        }else if (commandType instanceof transfer){
            //shape rules
            if(tx.getInputs().size() != 1){
                throw new IllegalArgumentException("the transfer command can have only one input");
            }
            if(tx.getOutputs().size() != 1){
                throw new IllegalArgumentException("the transfer command can only have one output");
            }

            //content rules
            ContractState outputState = tx.getOutput(0);
            ContractState inputState = tx.getInput(0);

            if(!(outputState instanceof MetalState)){
                throw new IllegalArgumentException("output must be a metal state");
            }

            MetalState metalState = (MetalState) inputState;
            if(!(metalState.getMetalName().equals("Gold"))) {
                throw new IllegalArgumentException("Metal is not gold");
            }

            //signer rulers
            Party owner = metalState.getOwner();
            PublicKey ownerKey = owner.getOwningKey();

            if(!requiredSigners.contains(ownerKey)){
                throw new IllegalArgumentException("owner has to sign the transfer");
            }
        }else{
            throw new IllegalArgumentException(("unrecognized command"));
        }

    }

    public static class issue implements CommandData {
        public issue() {}
    }
    public static class transfer implements CommandData {}
}