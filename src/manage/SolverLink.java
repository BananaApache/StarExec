package manage;

import java.util.Stack;
import data.Database;


/**
 * This is a link in the solver chain as described above.
 * Associates a solver with n benchmarks.
 * 
 */
public class SolverLink {
	private Stack<String> bPaths;
	private String sPath;
	private Database db;
	
	public SolverLink(Long sid) {
		bPaths = new Stack<String>();
		db = new Database();
		
		sPath = db.getSolver(sid).getPath();
	}
	
	public int getSize() {
		return bPaths.size();
	}
	
	public void addBenchmark(Long bid) {
		String bPath = db.getBenchmark(bid).getPath();
		bPaths.add(bPath);
	}
	
	public String getSolverPath() {
		return sPath;
	}
	
	public String getNextBenchmarkPath() {
		if(bPaths.isEmpty())
			return null;
		else
			return bPaths.pop();
	}
}
