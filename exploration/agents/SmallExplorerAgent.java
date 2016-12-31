package sim.app.IA2.exploration.agents;

import sim.app.IA2.exploration.env.Team.TeamNumber;
import sim.util.MutableInt2D;

public class SmallExplorerAgent extends ExplorerAgent {

	public SmallExplorerAgent(MutableInt2D loc) {
		/*
		 * Standard agent: 
		 * 		Step: Math.sqrt(2)
		 * 		View range: 40
		 * 		Identify time: 15
		 * 
		 * Small explorer agent:
		 * 		Step -> 20% faster.
		 * 		View Range -> 30% narrower field of vision. (40 * 0.70 = 28)
		 * 		Identify time -> Worse scanner that takes 40% more time to classify objects. (15 * 1.40 = 21)
		 * */
		super(loc, Math.sqrt(2) * 1.20, 28, 21);
	}


}
