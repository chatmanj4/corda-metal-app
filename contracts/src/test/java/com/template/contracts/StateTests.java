package com.template.contracts;

import com.template.states.MetalState;
import com.template.states.TemplateState;
import net.corda.core.contracts.ContractState;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.testing.core.TestIdentity;
import org.junit.Test;


import static org.junit.Assert.*;

public class StateTests {

    private Party Mint = new TestIdentity(new CordaX500Name("mint","","GB")).getParty();
    private Party Trader = new TestIdentity(new CordaX500Name("trader","","GB")).getParty();

    @Test
    public void metalStateImplementsContractFunction(){
        assertTrue(new MetalState("Gold", 10, Mint, Trader)instanceof ContractState);
    }

    @Test
    public void metalStateHasTwoParticipantsTheIssuerAndOwner(){
        MetalState metalState = new MetalState("Gold", 10, Mint, Trader);
        assertEquals(2, metalState.getParticipants().size());
        assertTrue(metalState.getParticipants().contains(Mint));
        assertTrue(metalState.getParticipants().contains(Trader));
    }

    @Test
    public void metalStateHasGetterForAllFields(){
        MetalState metalState = new MetalState("Gold", 10, Mint, Trader);
        assertEquals("Gold",metalState.getMetalName());
        assertEquals(Mint,metalState.getIssuer());
        assertEquals(Trader,metalState.getOwner());
    }
}