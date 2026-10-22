package com.mlc.mlcgames.party.survival.disastersnow;

/** Source windows: 15 s warmup, nine 6 s ramps, 30 s PvP warning, 30 s shrink warning. */
public record SnowTimeline(int warmup,int rampInterval,int speedCap,int pvpWarning,int shrinkWarning,int shrinkInterval,int shrinkSteps,int finalGrace) {
    public SnowTimeline {if(warmup<0||rampInterval<1||speedCap<1||pvpWarning<1||shrinkWarning<1||shrinkInterval<1||shrinkSteps<1||finalGrace<1)throw new IllegalArgumentException("invalid snow timeline");}
    public int speed(int second){return Math.min(speedCap,1+Math.max(0,(second-warmup)/rampInterval));}
    public int pvpAt(){return warmup+(speedCap-1)*rampInterval+pvpWarning;}
    public int firstShrinkAt(){return pvpAt()+shrinkWarning;}
    public int stopSnowAt(){return firstShrinkAt()+(shrinkSteps-1)*shrinkInterval;}
    public int finishAt(){return stopSnowAt()+finalGrace;}
    public boolean shrinkAt(int second){return second>=firstShrinkAt()&&second<=stopSnowAt()&&(second-firstShrinkAt())%shrinkInterval==0;}
}
