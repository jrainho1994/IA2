package sim.app.IA2.exploration.env;

import java.util.Vector;

import sim.app.IA2.exploration.agents.BrokerAgent;
import sim.app.IA2.exploration.agents.ExplorerAgent;
import sim.app.IA2.exploration.agents.MapperAgent;
import sim.engine.SimState;

public class Team {
	
	public enum TeamNumber {
		TEAM_A,
		TEAM_B
	};
	
	private TeamNumber id;
	private MapperAgent mapper;
	private BrokerAgent broker;
	private Vector<ExplorerAgent> explorers;
	
	public Team(TeamNumber id, MapperAgent m, BrokerAgent b) {
		this.id = id;
		this.mapper = m;
		this.broker = b;
		this.explorers = new Vector<ExplorerAgent>();
	}
	
	public void addExplorer (ExplorerAgent explorer) {
		this.explorers.addElement(explorer);
		mapper.updateLocation(explorer, explorer.getLoc());
		explorer.mapper = mapper;
		explorer.broker = broker;
	}

	public TeamNumber getId() {
		return id;
	}

	public MapperAgent getMapper() {
		return mapper;
	}

	public BrokerAgent getBroker() {
		return broker;
	}

	public Vector<ExplorerAgent> getExplorers() {
		return explorers;
	}

	public void setExplorers(Vector<ExplorerAgent> explorers) {
		this.explorers = explorers;
	}

	public void step(SimState state) {
		for(ExplorerAgent agent : explorers){
			agent.step(state);
		}
	}
}
