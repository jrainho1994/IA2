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
import sim.app.IA2.exploration.env.Team.TeamNumber;
import sim.app.IA2.exploration.objects.Bush;
import sim.app.IA2.exploration.objects.House;
import sim.app.IA2.exploration.objects.SimObject;
import sim.app.IA2.exploration.objects.Tree;
import sim.app.IA2.exploration.objects.Wall;
import sim.app.IA2.exploration.objects.Water;

public class SimEnvironmentTeams extends Environment {

	private static final long serialVersionUID = 1L;
	
	private Vector<Integer> typeExplorers; //[0] big explorers, [1] small explorers
	private RefereeAgent referee;
	private FileWriter milestonesTeamsWriter;
	protected HashMap<TeamNumber, HashMap<String, Double>> milestonesTeams  = new HashMap<>();
	
	public SimEnvironmentTeams(SimState state, int width, int height, int nBigAgents, int nSmallAgents){
		
		this.world = new SparseGrid2D(width, height);
		this.occupied = new Class[width][height];
		this.referee = new RefereeAgent(this, width, height);
		
		this.typeExplorers = new Vector<Integer>(2);
		this.typeExplorers.add(nBigAgents);
		this.typeExplorers.add(nSmallAgents);
		
		this.milestonesTeams.put(TeamNumber.TEAM_A, new HashMap<>());
		this.milestonesTeams.put(TeamNumber.TEAM_B, new HashMap<>());
		                          
		this.setup(state);
		
		try {
			initializeStatisticFiles();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void initializeStatisticFiles() throws IOException {
		writer = new FileWriter("stats.csv");
		
		/* Epochs file */
		File f = new File("epochs.csv");
		if(f.exists() && !f.isDirectory()) { 
			epochsWriter = new FileWriter("epochs.csv", true);
		}
		else {
			epochsWriter = new FileWriter("epochs.csv");
			String headerEpoch = "";
			for (int i=1; i<=100; i++) {
				headerEpoch += i*50 + ", ";
			}
			headerEpoch += "\n";
			epochsWriter.append(headerEpoch);
		}
		
		/* Milestones file */
		f = new File("milestones.csv");
		if(f.exists() && !f.isDirectory()) { 
			milestonesWriter = new FileWriter("milestones.csv", true);
		}
		else {
			String headerMilestones = "60%, 75%, 95%, 99%, 100%\n";
			milestonesWriter = new FileWriter("milestones.csv");
			milestonesWriter.append(headerMilestones);
		}
		
		/* Milestones Teams file */
		f = new File("milestonesTeams.csv");
		if(f.exists() && !f.isDirectory()) { 
			milestonesTeamsWriter = new FileWriter("milestonesTeams.csv", true);
		}
		else {
			String headerMilestonesTeams = "60% A, 60% B, 75% A, 75% B, 95% A, 95% B, 99% A, 99% B, 100% A, 100% B\n";
			milestonesTeamsWriter = new FileWriter("milestonesTeams.csv");
			milestonesTeamsWriter.append(headerMilestonesTeams);
		}
	}

	/**
	 * This method should setup the environment: create the objects and populate
	 * it with them and with the explorer agents
	 */
	protected void setup(SimState state){
		
		//addExplorersRandomly(state);
		//addExplorersCornersCenter(state);
		addExplorersTeams(state);
		
		//buildRandomMap(state);
		//buildDonutMap(state);
		buildStructuredMap(state);
	}
	
	private void addExplorersTeams(SimState state) {
		for (int i = 0; i < typeExplorers.get(0); i++) {
			if (i < typeExplorers.get(0)/2) {
				MutableInt2D loc = new MutableInt2D(world.getWidth() / 4, 0);
				addTeamExplorer(state, loc, TeamNumber.TEAM_A, true);
			}
			else {
				MutableInt2D loc = new MutableInt2D(3 * world.getWidth() / 4, 0);
				addTeamExplorer(state, loc, TeamNumber.TEAM_B, true);
			}
		}
		
		for (int i = 0; i < typeExplorers.get(1); i++) {
			if (i < typeExplorers.get(1)/2) {
				MutableInt2D loc = new MutableInt2D(world.getWidth() / 4, 0);
				addTeamExplorer(state, loc, TeamNumber.TEAM_A, false);
			}
			else {
				MutableInt2D loc = new MutableInt2D(3 * world.getWidth() / 4, 0);
				addTeamExplorer(state, loc, TeamNumber.TEAM_B, false);
			}
		}
	}

	/* Explorer Adding Methods */
	
	private void addTeamExplorer(SimState state, MutableInt2D loc, TeamNumber team, boolean big) {
		/* True -> Big agent, False -> Small agent */
		
		ExplorerAgent explorer;
		if (big) {
			explorer = new BigExplorerAgent(loc);
		}
		else {
			explorer = new SmallExplorerAgent(loc);
		}
		
		referee.addExplorerToTeam(explorer, team);
		this.updateLocation(explorer, loc);
	}

	private void addExplorersRandomly(SimState state) {
		for(int i= 0; i < typeExplorers.get(0) + typeExplorers.get(1); i++){
			MutableInt2D loc = new MutableInt2D(state.random.nextInt(world.getWidth()),state.random.nextInt(world.getHeight()));
			if (i < typeExplorers.get(0)) {
				addTeamExplorer(state, loc, TeamNumber.values()[i % 2], true);
			}
			else {
				addTeamExplorer(state, loc, TeamNumber.values()[i % 2], false);
			}
		}
	}
	
	private void addExplorersCornersCenter(SimState state) {
		
		// 4 Explorers in the center of the map
		for (int i = 0; i < 2; i++) {
			MutableInt2D loc1 = new MutableInt2D(world.getWidth() / 2, world.getHeight() / 2);
			MutableInt2D loc2 = new MutableInt2D(world.getWidth() / 2, world.getHeight() / 2);
			addTeamExplorer(state, loc1, TeamNumber.TEAM_A, true);
			addTeamExplorer(state, loc2, TeamNumber.TEAM_B, true);
		}
		
		// 4 Explorers on all 4 corners
		MutableInt2D locs[] = new MutableInt2D[4];
		locs[0] = new MutableInt2D(0, 0);
		locs[1] = new MutableInt2D(world.getWidth(), world.getHeight());
		locs[2] = new MutableInt2D(0, world.getHeight());
		locs[3] = new MutableInt2D(world.getWidth(), 0);
		
		addTeamExplorer(state, locs[0], TeamNumber.TEAM_A, true);
		addTeamExplorer(state, locs[1], TeamNumber.TEAM_B, true);
		addTeamExplorer(state, locs[2], TeamNumber.TEAM_A, true);
		addTeamExplorer(state, locs[3], TeamNumber.TEAM_B, true);
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
				milestonesTeamsWriter.close();
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
		referee.step(state);
	}

	private void printStats() {
		int objsSeen = 0;
		int objsSeenTeamA = 0;
		int objsSeenTeamB = 0;
		int nObjs = 0;
		int nErrors = 0;
		double percObjsSeen = 0.0;
		double percObjsSeenTeamA = 0.0;
		double percObjsSeenTeamB = 0.0;
		double percError = 0.0;
		
		for(int i = 0; i<world.getWidth(); i++){
			for (int j = 0; j < world.getHeight(); j++) {
				Class real = occupied[i][j];
				Class identified = referee.identifiedObjects[i][j];
				Class identifiedTeamA = referee.getTeam(TeamNumber.TEAM_A).getMapper().identifiedObjects[i][j];
				Class identifiedTeamB = referee.getTeam(TeamNumber.TEAM_B).getMapper().identifiedObjects[i][j];
				
				nObjs += real != null ? 1 : 0;				
				objsSeen += identified != null ? 1 : 0;
				objsSeenTeamA += identifiedTeamA != null ? 1 : 0;
				objsSeenTeamB += identifiedTeamB != null ? 1 : 0;
				nErrors += ((real != null && identified != null) && (real != identified)) ? 1 : 0;
			}
		}
		
		percObjsSeen = ((double) objsSeen / (double) nObjs) * 100.0;
		percError = ((double) nErrors/ (double) objsSeen) * 100.0;
		percObjsSeenTeamA = ((double) objsSeenTeamA / (double) nObjs)*100.0;
		percObjsSeenTeamB = ((double) objsSeenTeamB / (double) nObjs)*100.0;
		updateMilestones(percObjsSeen, percError, percObjsSeenTeamA, percObjsSeenTeamB);
		
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
	
	private void updateMilestones (double percObjsSeen, double percError, double percObjsSeenTeamA, double percObjsSeenTeamB) {
		
		if (percObjsSeen > 60 && this.milestones.get("60%") == null) {
			this.milestones.put("60%", this.step);
			this.milestonesTeams.get(TeamNumber.TEAM_A).put("60%", percObjsSeenTeamA);
			this.milestonesTeams.get(TeamNumber.TEAM_B).put("60%", percObjsSeenTeamB);
			this.milestones.put("100%", -1);
			this.milestonesTeams.get(TeamNumber.TEAM_A).put("100%", -1.0);
			this.milestonesTeams.get(TeamNumber.TEAM_B).put("100%", -1.0);
		} 
		else if (percObjsSeen > 75 && this.milestones.get("75%") == null) {
			this.milestones.put("75%", this.step);
			this.milestonesTeams.get(TeamNumber.TEAM_A).put("75%", percObjsSeenTeamA);
			this.milestonesTeams.get(TeamNumber.TEAM_B).put("75%", percObjsSeenTeamB);
		}
		else if (percObjsSeen > 95 && this.milestones.get("95%") == null) {
			this.milestones.put("95%", this.step);
			this.milestonesTeams.get(TeamNumber.TEAM_A).put("95%", percObjsSeenTeamA);
			this.milestonesTeams.get(TeamNumber.TEAM_B).put("95%", percObjsSeenTeamB);
		}
		else if (percObjsSeen > 99 && this.milestones.get("99%") == null) {
			this.milestones.put("99%", this.step);
			this.milestonesTeams.get(TeamNumber.TEAM_A).put("99%", percObjsSeenTeamA);
			this.milestonesTeams.get(TeamNumber.TEAM_B).put("99%", percObjsSeenTeamB);
		}
		else if (percObjsSeen > 100 && this.milestones.get("100%") == null) {
			this.milestones.put("100%", this.step);
			this.milestonesTeams.get(TeamNumber.TEAM_A).put("100%", percObjsSeenTeamA);
			this.milestonesTeams.get(TeamNumber.TEAM_B).put("100%", percObjsSeenTeamB);
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
			
			HashMap<String, Double> ta = milestonesTeams.get(TeamNumber.TEAM_A);
			HashMap<String, Double> tb = milestonesTeams.get(TeamNumber.TEAM_B);
			milestonesTeamsWriter.append(
					ta.get("60%") + ", " +
					tb.get("60%") + ", " +
					ta.get("75%") + ", " +
					tb.get("75%") + ", " +
					ta.get("95%") + ", " +
					tb.get("95%") + ", " +
					ta.get("99%") + ", " +
					tb.get("99%") + ", " +
					ta.get("100%") + ", " +
					tb.get("100%") + "\n"
			);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public MapperAgent getMapper () {
		return referee;
	}
}
