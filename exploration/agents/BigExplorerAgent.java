package sim.app.IA2.exploration.agents;

import sim.app.IA2.exploration.env.Team.TeamNumber;
import sim.util.MutableInt2D;

public class BigExplorerAgent extends ExplorerAgent {

	public BigExplorerAgent(MutableInt2D loc) {
		/*
		 * Standard agent: 
		 * 		Step: Math.sqrt(2)
		 * 		View range: 40
		 * 		Identify time: 15
		 * 
		 * Big explorer agent:
		 * 		Step -> 20% slower.
		 * 		View Range -> 15% wider field of vision. (40 * 1.15 = 46)
		 * 		Identify time -> Better scanner that takes 20% less time to classify objects. (15 * 0.80 = 12)
		 * */
		super(loc, Math.sqrt(2) * 0.80, 46, 12);
	}

	

}
