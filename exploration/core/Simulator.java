package sim.app.IA2.exploration.core;

import sim.app.IA2.exploration.env.SimEnvironment;
import sim.app.IA2.exploration.env.SimEnvironmentTeams;
import sim.engine.SimState;
import sim.app.IA2.exploration.env.*;

/**
 * @author Miguel Tavares, Pedro Gaspar
 *
 */
public class Simulator extends SimState {

	private static final long serialVersionUID = 1L;
	
	private final static int N_BIG_EXPLORERS = 4;
	private final static int N_SMALL_EXPLORERS = 4;
	public final static int WIDTH = 400;
	public final static int HEIGHT = 300;
	public final static int limitRadius = (int) (Math.max(WIDTH, HEIGHT) * 0.25);	// The 0.25 should be RAIUS_RATIO or something
	
	public Environment env;
	
	
	//TODO
//	public boolean teams = true;
//	
//	public boolean isTeams() {
//		return teams;
//	}
//
//	public void setTeams(boolean teams) {
//		this.teams = teams;
//	}

	/**
	 * Default constructor that creates a new instance of the simulator
	 * @param seed Seed for the random number generator
	 */
	public Simulator(long seed) {
		super(seed);
	}
	
	/**
	 * Start the simulation. Schedule the environment to iterate.
	 */
	public void start(){
		super.start();
		//env = new SimEnvironment(this, WIDTH, HEIGHT, N_BIG_EXPLORERS, N_SMALL_EXPLORERS);
		env = new SimEnvironmentTeams(this, WIDTH, HEIGHT, N_BIG_EXPLORERS*2, 0);
		schedule.scheduleRepeating(env);
		// Now, everything else is up to the environment
	}

	public static void main(String[] args)
    {
		doLoop(Simulator.class, args);
		System.exit(0);
    }    
}


