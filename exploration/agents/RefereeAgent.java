package sim.app.IA2.exploration.agents;

import java.util.Collection;
import java.util.HashMap;
import java.util.Vector;

import sim.app.IA2.exploration.env.Environment;
import sim.app.IA2.exploration.env.Team;
import sim.app.IA2.exploration.env.Team.TeamNumber;
import sim.engine.SimState;

public class RefereeAgent extends MapperAgent {
	
	private HashMap<TeamNumber, Team> teams;
	private Environment env;
	
	public RefereeAgent(Environment env, int width, int height) {
		super(width, height);
		this.env = env;
		this.teams = new HashMap<>(2);
		this.teams.put(TeamNumber.TEAM_A, new Team(TeamNumber.TEAM_A, new TeamMapperAgent(width, height, this), new BrokerAgent()));
		this.teams.put(TeamNumber.TEAM_B, new Team(TeamNumber.TEAM_B, new TeamMapperAgent(width, height, this), new BrokerAgent()));
		//this.identifiedObjects = teams.get(TeamNumber.TEAM_A).getMapper().identifiedObjects;
		//this.knownWorld = teams.get(TeamNumber.TEAM_A).getMapper().knownWorld;
		//this.knownObjects = teams.get(TeamNumber.TEAM_A).getMapper().knownObjects;
	}

	public Collection<Team> getTeams() {
		return teams.values();
	}

	public void addExplorerToTeam(ExplorerAgent explorer, TeamNumber team) {
		teams.get(team).addExplorer(explorer);
		explorer.env = env;
	}

	public void step(SimState state) {
		for (Team team : teams.values()) {
			team.step(state);
		}
	}	

	public Class getIdentifiedBothTeams (int column, int line) {
		MapperAgent m1 = teams.get(TeamNumber.TEAM_A).getMapper();
		MapperAgent m2 = teams.get(TeamNumber.TEAM_B).getMapper();
		
		Class cls = m1.identifiedObjects[column][line];
		return cls != null? cls : m2.identifiedObjects[column][line];
	}
}
