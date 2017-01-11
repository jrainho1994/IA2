package sim.app.IA2.exploration.env;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Vector;

import java.io.*;

import sim.engine.SimState;
import sim.engine.Steppable;
import sim.field.grid.SparseGrid2D;
import sim.util.Bag;
import sim.util.Int2D;
import sim.util.MutableInt2D;
import sim.app.IA2.exploration.agents.*;
import sim.app.IA2.exploration.objects.Bush;
import sim.app.IA2.exploration.objects.House;
import sim.app.IA2.exploration.objects.SimObject;
import sim.app.IA2.exploration.objects.Tree;
import sim.app.IA2.exploration.objects.Wall;
import sim.app.IA2.exploration.objects.Water;

public class SimEnvironment extends Environment {

	private static final long serialVersionUID = 1L;
	
	private Vector<ExplorerAgent> explorers;
	private Vector<Boolean> typeExplorers;
	private MapperAgent mapper;
	private BrokerAgent broker;
	
	public SimEnvironment(SimState state, int width, int height, int nBigAgents, int nSmallAgents){
		
		this.world = new SparseGrid2D(width, height);
		this.occupied = new Class[width][height];
		
		this.explorers = new Vector<ExplorerAgent>(nBigAgents + nSmallAgents);
		this.typeExplorers = new Vector<Boolean>(nBigAgents + nSmallAgents);
		
		/* True -> Big agent, False -> Small agent */
		for (int i = 0; i < typeExplorers.capacity(); i++) {
			if (i + 1 <= nBigAgents) {
				typeExplorers.add(true);
			}
			else {
				typeExplorers.add(false);
			}
		}
		
		this.mapper = new MapperAgent(width, height);
		this.broker = new BrokerAgent();
		                          
		this.setup(state);
		
		try {
			initializeStatisticFiles();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * This method should setup the environment: create the objects and populate
	 * it with them and with the explorer agents
	 */
	protected void setup(SimState state){
		
		//addExplorersRandomly(state);
		addExplorersCornersCenterV2(state);	// This always adds 8 Explorers
		
		//buildRandomMap(state);
		//buildDonutMap(state);
		buildStructuredMap(state);
	}
	
	/* Explorer Adding Methods */
	
	private void addExplorersRandomly(SimState state) {
		for(int i= 0; i < typeExplorers.capacity(); i++){
			MutableInt2D loc = new MutableInt2D(state.random.nextInt(world.getWidth()),state.random.nextInt(world.getHeight()));
			addExplorer(state, loc, typeExplorers.get(i));
		}
	}
	
	private void addExplorersCornersCenter(SimState state) {
		
		// 4 Explorers in the center of the map
		for (int i = 0; i < 4; i++) {
			MutableInt2D loc = new MutableInt2D(world.getWidth() / 2, world.getHeight() / 2);
			addExplorer(state, loc, true);
		}
		
		// 4 Explorers on all 4 corners
		MutableInt2D locs[] = new MutableInt2D[4];
		locs[0] = new MutableInt2D(0, 0);
		locs[1] = new MutableInt2D(world.getWidth(), world.getHeight());
		locs[2] = new MutableInt2D(0, world.getHeight());
		locs[3] = new MutableInt2D(world.getWidth(), 0);
		
		for (MutableInt2D l : locs)
			addExplorer(state, l, true);
	}
	
	private void addExplorersCornersCenterV2(SimState state) {
		
		// 4 Explorers in the center of the map
		for (int i = 0; i < 4; i++) {
			MutableInt2D loc = new MutableInt2D(world.getWidth() / 2, world.getHeight() / 2);
			addExplorer(state, loc, true);
		}
		
		// 4 Explorers on all 4 corners
		MutableInt2D locs[] = new MutableInt2D[4];
		locs[0] = new MutableInt2D(0, 0);
		locs[1] = new MutableInt2D(world.getWidth(), world.getHeight());
		locs[2] = new MutableInt2D(0, world.getHeight());
		locs[3] = new MutableInt2D(world.getWidth(), 0);
		
		for (MutableInt2D l : locs)
			addExplorer(state, l, false);
	}
	
	private void addExplorer(SimState state, MutableInt2D loc, boolean big) {
		/* True -> Big agent, False -> Small agent */
		
		ExplorerAgent explorer;
		if (big) {
			explorer = new BigExplorerAgent(loc);
		}
		else {
			explorer = new SmallExplorerAgent(loc);
		}
		
		explorers.add(explorer);
		
		mapper.updateLocation(explorer,loc);
		this.updateLocation(explorer, loc);
		explorer.env = this;
		explorer.mapper = mapper;
		explorer.broker = broker;
	}
		
	@Override
	public void step(SimState state) {
		step = step + 1;
		
		if(step > maxSteps){
			try {
				printMilestones();
				writer.close();
				epochsWriter.close();
				milestonesWriter.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			state.finish();
		}
		
		int stepCheckpoint = maxSteps/100;
		if( step%stepCheckpoint == 0 ){
			printStats();
		}
		
		/*
		 * Step over all the explorers in the environment, making them step
		 */
		for(ExplorerAgent agent : explorers){
			agent.step(state);
		}
		
	}

	private void printStats() {
		int objsSeen = 0;
		int nObjs = 0;
		int nErrors = 0;
		double percObjsSeen = 0.0;
		double percError = 0.0;
		
		for(int i = 0; i<world.getWidth(); i++){
			for (int j = 0; j < world.getHeight(); j++) {
				Class real = occupied[i][j];
				Class identified = mapper.identifiedObjects[i][j];
				
				nObjs += real != null ? 1 : 0;
				objsSeen += identified != null ? 1 : 0;
				nErrors += ((real != null && identified != null) && (real != identified)) ? 1 : 0;
			}
		}
		
		percObjsSeen = ((double)objsSeen/(double)nObjs)*100.0;
		percError = ((double)nErrors/(double)objsSeen)*100.0;
		
		updateMilestones(percObjsSeen, percError);
		
		System.err.println("SEEN: " + objsSeen);
		System.err.println("EXIST: " + nObjs);
		
		System.err.println("-------------------------");
		System.err.println("STATISTICS AT STEP: " + this.step);
		System.err.println("-------------------------");
		System.err.println("% OF OBJECTS SEEN: " + Math.ceil(percObjsSeen * 100)/100 + "%");
		System.err.println("% OF ERROR: " + percError + "%");
		System.err.println("-------------------------");
		
		try {
			writer.append("" + step + " , " + percObjsSeen + " , " + percError + "\n");
			epochsWriter.append(Double.toString(percObjsSeen));
			if (step != 5000) {
				epochsWriter.append(", ");
			}
			else {
				epochsWriter.append("\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void updateMilestones(double percObjsSeen, double percError) {
		if (percObjsSeen > 60 && this.milestones.get("60%") == null) {
			this.milestones.put("60%", this.step);
			this.milestones.put("100%", -1);
		} 
		else if (percObjsSeen > 75 && this.milestones.get("75%") == null) {
			this.milestones.put("75%", this.step);
		}
		else if (percObjsSeen > 95 && this.milestones.get("95%") == null) {
			this.milestones.put("95%", this.step);
		}
		else if (percObjsSeen > 99 && this.milestones.get("99%") == null) {
			this.milestones.put("99%", this.step);
		}
		else if (percObjsSeen > 100 && this.milestones.get("100%") == null) {
			this.milestones.put("100%", this.step);
		}
	}

	private void printMilestones() {
		
		System.err.println("-------------------------");
		System.err.println("MILESTONES");
		System.err.println("-------------------------");
		System.err.println("60% - " + this.milestones.get("60%"));
		System.err.println("75% - " + this.milestones.get("75%"));
		System.err.println("95% - " + this.milestones.get("95%"));
		System.err.println("99% - " + this.milestones.get("99%"));
		System.err.println("100% - " + this.milestones.get("100%"));
		System.err.println("-------------------------");
		try {
			writer.append("\n\n" + "MILESTONES: " 
					+ "60% = " + this.milestones.get("60%") + "\n"
					+ "75% = " + this.milestones.get("75%") + "\n"
					+ "95% = " + this.milestones.get("95%") + "\n"
					+ "99% = " + this.milestones.get("99%") + "\n"
					+ "100% = " + this.milestones.get("100%") + "\n"
			);
			
			milestonesWriter.append(
					this.milestones.get("60%") + ", " +
					this.milestones.get("75%") + ", " +
					this.milestones.get("95%") + ", " +
					this.milestones.get("99%") + ", " +
					this.milestones.get("100%") + "\n"
			);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public MapperAgent getMapper() {
		return mapper;
	}

	public BrokerAgent getBroker() {
		return broker;
	}	
}
