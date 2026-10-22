package com.mlc.mlcgames.party.reaction.tsays;

/** Both explicit actions and deadline outcomes use the same inversion rule. */
public final class TSaysOutcome {
    private TSaysOutcome() {}
    public static boolean success(boolean fulfilled,boolean tSays){return fulfilled!=tSays;}
    public static int reward(int priorFinishers,boolean deadline){
        if(priorFinishers<0)throw new IllegalArgumentException("negative finish count");
        return deadline?1:priorFinishers==0?3:priorFinishers==1?2:1;
    }
}
