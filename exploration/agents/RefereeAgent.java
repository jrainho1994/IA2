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
	}

	public Collection<Team> getTeams() {
		return teams.values();
	}
	
	public Team getTeam (TeamNumber tn) {
		return teams.get(tn);
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
}
