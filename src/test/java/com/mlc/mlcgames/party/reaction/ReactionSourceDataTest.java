package com.mlc.mlcgames.party.reaction;

import com.google.gson.*;
import com.mlc.mlcgames.party.reaction.tsays.TSaysOutcome;
import org.junit.jupiter.api.Test;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class ReactionSourceDataTest {
    private JsonObject read(String path){InputStream in=getClass().getResourceAsStream(path);assertNotNull(in);try(in){return JsonParser.parseReader(new InputStreamReader(in,StandardCharsets.UTF_8)).getAsJsonObject();}catch(IOException e){throw new UncheckedIOException(e);}}
    @Test void allSeventySourceTaskIdsAreUniqueAndAllSevenGroupsRemain(){JsonArray tasks=read("/party/reaction/tsays/task-data.json").getAsJsonArray("tasks");assertEquals(70,tasks.size());Set<Integer>ids=new HashSet<>();Set<String>groups=new HashSet<>();for(JsonElement row:tasks){JsonObject t=row.getAsJsonObject();assertTrue(ids.add(t.get("id").getAsInt()));groups.add(t.get("group").getAsString());assertTrue(t.get("seconds").getAsInt()>0);}assertEquals(Set.of("action","craft","wear","hand","use","block","fight"),groups);}
    @Test void negativeTasksHaveTheirOwnDeadlineSemantics(){Set<Integer>negative=new HashSet<>();for(JsonElement row:read("/party/reaction/tsays/task-data.json").getAsJsonArray("tasks")){JsonObject t=row.getAsJsonObject();if(t.get("negative").getAsBoolean())negative.add(t.get("id").getAsInt());}assertEquals(Set.of(3,5,7,20,67),negative);}
    @Test void prefixInvertsActionsAndDeadlines(){assertTrue(TSaysOutcome.success(true,false));assertFalse(TSaysOutcome.success(true,true));assertFalse(TSaysOutcome.success(false,false));assertTrue(TSaysOutcome.success(false,true));}
    @Test void completionOrderAwardsThreeTwoOneAndDeadlinesAlwaysOne(){assertEquals(3,TSaysOutcome.reward(0,false));assertEquals(2,TSaysOutcome.reward(1,false));assertEquals(1,TSaysOutcome.reward(2,false));assertEquals(1,TSaysOutcome.reward(63,false));assertEquals(1,TSaysOutcome.reward(0,true));assertThrows(IllegalArgumentException.class,()->TSaysOutcome.reward(-1,false));}
    @Test void allFortyExactGeometriesCoverTheirEntireInnerFloor(){JsonObject patterns=read("/party/reaction/color/pattern-data.json").getAsJsonObject("patterns");assertEquals(40,patterns.size());for(var entry:patterns.entrySet()){JsonArray cells=entry.getValue().getAsJsonArray();assertEquals(1296,cells.size(),entry.getKey());for(JsonElement cell:cells)assertTrue(cell.getAsInt()>0,entry.getKey());}for(int family=6;family<=17;family++)if(family!=9)assertTrue(patterns.has(family+"_a"));for(int width=1;width<=4;width++)assertTrue(patterns.has("1_"+width));assertTrue(patterns.has("3_1"));assertTrue(patterns.has("4_1"));}
    @Test void allSeventeenGradientLayersAndOriginalThirtyTwoColourMarkersRemain(){JsonObject data=read("/party/reaction/color/pattern-data.json");JsonArray gradients=data.getAsJsonArray("gradients");assertEquals(17,gradients.size());for(JsonElement row:gradients)assertEquals(1600,row.getAsJsonArray().size());JsonArray palette=data.getAsJsonArray("source-palette");assertEquals(32,palette.size());Set<String>colours=new HashSet<>();for(JsonElement e:palette)colours.add(e.getAsString());assertEquals(16,colours.size());}
}
