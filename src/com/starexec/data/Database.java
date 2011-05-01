package com.starexec.data;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import com.starexec.constants.R;
import com.starexec.data.to.Benchmark;
import com.starexec.data.to.Configuration;
import com.starexec.data.to.Job;
import com.starexec.data.to.JobPair;
import com.starexec.data.to.Level;
import com.starexec.data.to.Solver;
import com.starexec.data.to.User;
import com.starexec.util.LogUtil;
import com.starexec.util.SHA256;

/**
 * Database objects are responsible for any and all communication to the MySQL back-end database.
 * This class provides a convenient front for storing things in the database and extracting information.
 * 
 * @author Tyler Jensen
 */
public class Database {
	private Logger log = Logger.getLogger("starexec.data");
	private Connection connection = null;
	private PreparedStatement psAddUser = null;
	private PreparedStatement psAddPassword = null;
	private PreparedStatement psAddPermissions = null;
	private PreparedStatement psAddLevel = null;
	private PreparedStatement psGetUser = null;
	private PreparedStatement psGetUser2 = null;
	private PreparedStatement psAddBenchmark = null;
	private PreparedStatement psGetSolvers = null;
	private PreparedStatement psGetAllSolvers = null;
	private PreparedStatement psGetBenchmarks = null;
	private PreparedStatement psGetImmediateBench = null;
	private PreparedStatement psGetAllBenchmarks = null;
	private PreparedStatement psGetMaxLevel = null;
	private PreparedStatement psGetMaxLevelGroup = null;
	private PreparedStatement psGetRootLevels = null;
	private PreparedStatement psGetSubLevels = null;
	private PreparedStatement psAddSolver = null;
	private PreparedStatement psAddCanSolve = null;
	private PreparedStatement psAddJob = null;
	private PreparedStatement psAddJobPair = null;
	private PreparedStatement psJobStatus = null;
	private PreparedStatement psPairStatus = null;
	private PreparedStatement psLevelToBenchs = null;
	private PreparedStatement psGetJobs = null;
	private PreparedStatement psGetJobPairs = null;
	private PreparedStatement psAddConfiguration = null;
	
	public Database() {
		this(R.MYSQL_URL, R.MYSQL_USERNAME, R.MYSQL_PASSWORD);	// Use the default connection info			
	}
	
	public Database(String url, String username, String pass) {
		try {
			Class.forName("com.mysql.jdbc.Driver");	// Load the MYSQL driver						
			connection = DriverManager.getConnection(url, username, pass);	// Open a connection to the database			
		} catch (Exception e) {
			log.severe("DATABASE CONNECTION ERROR");
		}		
	}
	
	/**
	 * Adds the specified user to the database. Defaults to an unverified user and does not handle user verification.
	 * @param u The user to add
	 * @return True if the user was successfully added, false if otherwise
	 */
	public synchronized boolean addUser(User u){
		try{
			connection.setAutoCommit(false);	// Turn auto commit off (the operations below need to be a single transaction)
			
			// INSERT INTO USERS TABLE
			if(psAddUser == null)	// If the statement hasn't been prepared yet, create it...			
					psAddUser = connection.prepareStatement("INSERT INTO users (username, fname, lname, affiliation, created, email) VALUES (?, ?, ?, ?, SYSDATE(), ?)", Statement.RETURN_GENERATED_KEYS);
			
			// Fill in the prepared statement
			psAddUser.setString(1, u.getUsername());
			psAddUser.setString(2, u.getFirstName());
			psAddUser.setString(3, u.getLastName());
			psAddUser.setString(4, u.getAffiliation());
			psAddUser.setString(5, u.getEmail());
			
			// Execute the statement
			int rowsAffected = psAddUser.executeUpdate();	// Execute and get the number of rows affected (should be one)
			ResultSet idSet = psAddUser.getGeneratedKeys(); idSet.next();
			int insertedID = idSet.getInt(1);				// Get the ID the record was inserted under (used for the next two queries
			
			// INSERT INTO PASSWORD TABLE
			if(psAddPassword == null)	// If the statement hasn't been prepared yet, create it...
				psAddPassword = connection.prepareStatement("INSERT INTO passwords (userid, password) VALUES (?, ?)");
			
			// Fill in the prepared statement
			// TODO: Add salt to passwords
			psAddPassword.setInt(1, insertedID);
			psAddPassword.setString(2, SHA256.getHash(u.getPassword()));
			
			rowsAffected += psAddPassword.executeUpdate();	// Execute and get the rows affected
			
			// INSERT INTO PERMISSIONS TABLE
			if(psAddPermissions == null)	// If the statement hasn't been prepared yet, create it...
				psAddPermissions = connection.prepareStatement("INSERT INTO permissions (userid) VALUES (?)");
			
			// Fill in the prepared statement
			psAddPermissions.setInt(1, insertedID);
			
			rowsAffected += psAddPermissions.executeUpdate();	// Execute and get the rows affected
			connection.commit();			// Now commit everything to the database
			
			return rowsAffected == 3;	// If exactly 3 rows were affected, success!
		} catch (Exception e){
			doRollback();
			log.severe("Error in addUser method: " + e.getMessage());
			LogUtil.LogException(e);
			return false;
		} finally {
			autoCommitOn();
		}
	}
	
	/**
	 * Retrieves a user from the database given the username
	 * @param username The username of the user to retrieve
	 * @return The user object associated with the user
	 */
	public synchronized User getUser(String username){
		try {
			if(psGetUser == null)
				psGetUser = connection.prepareStatement("SELECT * FROM users JOIN passwords ON users.userid = passwords.userid WHERE users.username='?'");
			
			psGetUser.setString(1, username);
			ResultSet results = psGetUser.executeQuery();
			
			if(results.next()){
				User u = new User(results.getInt("userid"), results.getString("username"));
				u.setAffiliation(results.getString("affiliation"));
				u.setEmail(results.getString("email"));
				u.setFirstName(results.getString("fname"));
				u.setLastName(results.getString("lname"));
				u.setPassword(results.getString("password"));
				return u;
			}					
		} catch (Exception e){
			log.severe("Error in getUser method: " + e.getMessage());
			LogUtil.LogException(e);		
		}
		
		return null;
	}
	
	/**
	 * Retrieves a user from the database given the user's id
	 * @param userid The id of the user to retrieve
	 * @return The user object associated with the id
	 */
	public synchronized User getUser(int userid){
		try {
			if(psGetUser2 == null)
				psGetUser2 = connection.prepareStatement("SELECT * FROM users JOIN passwords WHERE users.userid=?");
			
			psGetUser2.setInt(1, userid);
			ResultSet results = psGetUser2.executeQuery();
			
			if(results.next()){
				User u = new User(results.getInt("userid"), results.getString("username"));
				u.setAffiliation(results.getString("affiliation"));
				u.setEmail(results.getString("email"));
				u.setFirstName(results.getString("fname"));
				u.setLastName(results.getString("lname"));
				u.setPassword(results.getString("password"));
				return u;
			}					
		} catch (Exception e){
			log.severe("Error in getUser method: " + e.getMessage());
			LogUtil.LogException(e);		
		}
		
		return null;
	}
	
	public synchronized boolean addLevelsBenchmarks(Collection<Level> levels, Collection<Benchmark> benchmarks){		
		int offset = getMaxLevel();
		boolean retVal = true;
		retVal = retVal && addLevelStructure(levels, offset);
		retVal = retVal && addBenchmarks(benchmarks, offset);
		return retVal;
	}
	
	public synchronized boolean addSolver(Solver s){		
		try{
			connection.setAutoCommit(false);
			
			if(psAddSolver == null)			
				psAddSolver = connection.prepareStatement("INSERT INTO solvers (uploaded, path, usr, notes, name) VALUES (SYSDATE(), ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
						
			psAddSolver.setString(1, s.getPath());
			psAddSolver.setInt(2, s.getUserId());
			psAddSolver.setString(3, s.getNotes());
			psAddSolver.setString(4, s.getName());
			
			psAddSolver.executeUpdate();
			ResultSet idSet = psAddSolver.getGeneratedKeys(); idSet.next();
			int insertedID = idSet.getInt(1);

			if(psAddCanSolve == null)
				psAddCanSolve = connection.prepareStatement("INSERT INTO can_solve VALUES (?, ?)");
			
			for(Level l : s.getSupportedDivs()){
				psAddCanSolve.setInt(1, insertedID);
				psAddCanSolve.setInt(2, l.getId());
				psAddCanSolve.executeUpdate();
			}
			
			if(psAddConfiguration == null)
				psAddConfiguration = connection.prepareStatement("INSERT INTO configurations (sid, name) VALUES (?, ?)");
			
			for(Configuration c : s.getConfigurations()){
				psAddConfiguration.setInt(1, insertedID);
				psAddConfiguration.setString(2, c.getName());
				psAddConfiguration.executeUpdate();
			}
			
			connection.commit();
			return true;			
		} catch (Exception e){
			doRollback();
			log.severe("Error in addSolver method: " + e.getMessage());
			LogUtil.LogException(e);
			return false;
		} finally {
			autoCommitOn();
		}
	}	
	
	/**
	 * Adds a set of benchmarks to the database. Benchmarks must be added before the level structure is added or else
	 * the benchmarks will be mis-aligned with their proper levels.
	 * @param benchmarks The set of benchmarks to add
	 * @return True if all benchmarks were added, false if otherwise.
	 */
	public synchronized boolean addBenchmarks(Collection<Benchmark> benchmarks, int offSet){
		try{						
			if(psAddBenchmark == null)	// If the statement hasn't been prepared yet, create it...			
					psAddBenchmark = connection.prepareStatement("INSERT INTO benchmarks (uploaded, physical_path, usr, lvl) VALUES (SYSDATE(), ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
						
			connection.setAutoCommit(false);
			int rowsAffected = 0;
			
			for(Benchmark b : benchmarks){
				// Fill in the prepared statement
				psAddBenchmark.setString(1, b.getPath());
				psAddBenchmark.setInt(2, b.getUserId());
				psAddBenchmark.setInt(3, b.getLevel() + offSet);
				
				// Execute the statement
				rowsAffected += psAddBenchmark.executeUpdate();
			}									
			
			if(rowsAffected == benchmarks.size()) {
				connection.commit();
				return true;
			} else {
				throw new Exception("Benchmarks inserted do not match the benchmarks received.");
			}
		} catch (Exception e){
			doRollback();
			log.severe("Error in addBenchmarks method: " + e.getMessage());
			LogUtil.LogException(e);
			return false;
		} finally {
			autoCommitOn();
		}
	}
	
	/**
	 * Gets a list of benchmarks given a list of id's to retrieve
	 * @param idList The list of ids to fetch benchmarks for. Null retrieves all benchmarks.
	 * @return An arraylist of benchmarks corresponding to the input ids (ordering is preserved)
	 */
	public synchronized List<Benchmark> getBenchmarks(Collection<Integer> idList){
		try {
			ArrayList<Benchmark> returnList = new ArrayList<Benchmark>(5);
			
			if(idList == null){
				if(psGetAllBenchmarks == null)
					psGetAllBenchmarks = connection.prepareStatement("SELECT * FROM benchmarks");
				
				ResultSet results = psGetAllBenchmarks.executeQuery();
				while(results.next()){
					Benchmark benchmark = new Benchmark();
					benchmark.setId(results.getInt("id"));
					benchmark.setPath(results.getString("physical_path"));
					benchmark.setUserId(results.getInt("usr"));
					benchmark.setUploaded(results.getTimestamp("uploaded"));
					returnList.add(benchmark);
				}
			} else {		
				for(int id : idList){
					Benchmark benchmark = this.getBenchmark(id);
					if(benchmark != null)
						returnList.add(benchmark);
				}
			}		
			
			return returnList;
		} catch (Exception e){
			log.severe("Error in getBenchmarks method: " + e.getMessage());
			LogUtil.LogException(e);
			return null;
		}
	}
	
	/**
	 * Gets a benchmark from the database given its id
	 * @param id The is of the benchmark to retrieve
	 * @return The benchmark associated with the id
	 */
	public synchronized Benchmark getBenchmark(int id){
		try {
			if(psGetBenchmarks == null)
				psGetBenchmarks = connection.prepareStatement("SELECT * FROM benchmarks WHERE id=?");
						
			psGetBenchmarks.setInt(1, id);
			
			ResultSet results = psGetBenchmarks.executeQuery();
			
			if(!results.next())
				return null;
			
			Benchmark benchmark = new Benchmark();
			benchmark.setId(results.getInt("id"));
			benchmark.setPath(results.getString("physical_path"));
			benchmark.setUserId(results.getInt("usr"));
			benchmark.setUploaded(results.getTimestamp("uploaded"));
			
			return benchmark;
		} catch (Exception e){
			log.severe("Error in getBenchmark method: " + e.getMessage());
			LogUtil.LogException(e);
			return null;
		}	
	}
	
	/**
	 * Gets a list of solvers given a list of solver id's
	 * @param idList The list of id's of the solvers to retrieve. Null gets all solvers
	 * @return An arraylist of solvers that correspond to the input id's (ordering is preserved)
	 */
	public synchronized List<Solver> getSolvers(Collection<Integer> idList){
		try {
			ArrayList<Solver> returnList = new ArrayList<Solver>(5);
					
			if(idList == null){
				if(psGetAllSolvers == null)
					psGetAllSolvers = connection.prepareStatement("SELECT * FROM solvers");
				
				ResultSet results = psGetAllSolvers.executeQuery();
				while(results.next()){
					Solver solver = new Solver();
					solver.setId(results.getInt("id"));
					solver.setPath(results.getString("path"));
					solver.setName(results.getString("name"));
					solver.setUserId(results.getInt("usr"));
					solver.setUploaded(results.getDate("uploaded"));
					solver.setNotes(results.getString("notes"));
					returnList.add(solver);
				}
			} else {
				for(int id : idList){
					Solver solver = this.getSolver(id);
					if(solver != null)
						returnList.add(solver);
				}	
			}					
			
			return returnList;
		} catch (Exception e) {
			log.severe("Error in getSolvers method: " + e.getMessage());
			LogUtil.LogException(e);
			return null;
		}
	}
	
	/**
	 * Gets a solver from the database given its id
	 * @param id The id of the solver
	 * @return The solver which corresponds to the given id
	 */
	public synchronized Solver getSolver(int id){
		try {
			if(psGetSolvers == null)
				psGetSolvers = connection.prepareStatement("SELECT * FROM solvers WHERE id=?");
						
			psGetSolvers.setInt(1, id);
			
			ResultSet results = psGetSolvers.executeQuery();
			
			if(!results.next())
				return null;
			
			Solver solver = new Solver();
			solver.setId(results.getInt("id"));
			solver.setName(results.getString("name"));
			solver.setPath(results.getString("path"));
			solver.setUserId(results.getInt("usr"));
			solver.setUploaded(results.getDate("uploaded"));
			solver.setNotes(results.getString("notes"));
			
			return solver;
		} catch (Exception e){
			log.severe("Error in getSolver method: " + e.getMessage());
			LogUtil.LogException(e);
			return null;
		}			
	}
	
	/**
	 * Inserts a level structure into the database given a collection of valid levels. Note that
	 * benchmarks must be added first or else the proper offsets will not be obtained for the benchmarks.
	 * @param levels The collection of levels to add
	 * @return True for success, false for failure
	 */
	public synchronized boolean addLevelStructure(Collection<Level> levels, int offSet){
		try {
			if(psAddLevel == null)
				psAddLevel = connection.prepareStatement("INSERT INTO levels (name, lft, rgt, gid, usr, dep) VALUES (?, ?, ?, ?, ?, ?)");
			
			connection.setAutoCommit(false);
			int inserted = 0;						
			int nextGroupId = getNextLevelGroup();
			
			for(Level l : levels){
				psAddLevel.setString(1, l.getName());
				psAddLevel.setInt(2, l.getLeft() + offSet);
				psAddLevel.setInt(3, l.getRight() + offSet);
				psAddLevel.setInt(4, nextGroupId);
				psAddLevel.setInt(5, l.getUserId());
				psAddLevel.setInt(6, l.getDepth());
				inserted += psAddLevel.executeUpdate();
			}
													
			connection.commit();			
			return levels.size() == inserted;
		} catch (Exception e){
			doRollback();
			log.severe("Error in addLevelStructure method: " + e.getMessage());
			LogUtil.LogException(e);
			return false;
		} finally {
			autoCommitOn();
		}
	}
	
	/**
	 * Adds a job and any embedded job pairs to the database.
	 * @param j The job to add to the database
	 * @return True if the the operation was a success, false otherwise.
	 */
	public synchronized boolean addJob(Job j){
		try{
			connection.setAutoCommit(false);
			
			if(psAddJob == null)			
				psAddJob = connection.prepareStatement("INSERT INTO jobs (id, subDate, description, status, node, timeout, usr) VALUES (?, SYSDATE(), ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
			
			psAddJob.setInt(1, j.getJobId());
			psAddJob.setString(2, j.getDescription());
			psAddJob.setString(3, j.getStatus());
			psAddJob.setString(4, j.getNode());
			psAddJob.setLong(5, j.getTimeout());
			psAddJob.setInt(6, j.getUserId());

			int rowsAffected = psAddJob.executeUpdate();
			//ResultSet idSet = psAddJob.getGeneratedKeys(); idSet.next();
			//int insertedID = idSet.getInt(1);
			
			if(psAddJobPair == null)
				psAddJobPair = connection.prepareStatement("INSERT INTO job_pairs (id, jid, sid, bid) VALUES (?, ?, ?, ?)");
			
			for(JobPair jp : j.getJobPairs()){ 
				psAddJobPair.setInt(1, jp.getId());
				psAddJobPair.setInt(2, j.getJobId());
				psAddJobPair.setInt(3, jp.getSolver().getId());
				psAddJobPair.setInt(4, jp.getBenchmark().getId());
				rowsAffected += psAddJobPair.executeUpdate();
			}						
			
			connection.commit();			
			return rowsAffected == j.getJobPairs().size() + 1;
		} catch (Exception e){
			doRollback();
			log.severe("Error in addJob method: " + e.getMessage());
			LogUtil.LogException(e);
			return false;
		} finally {
			autoCommitOn();
		}
	}
	
	/**
	 * @return The next level to use when inserting into the levels table. (Essentially the largest right-value)
	 */
	public synchronized int getMaxLevel(){
		try {
			if(psGetMaxLevel == null)
				psGetMaxLevel = connection.prepareStatement("SELECT MAX(rgt) FROM levels");
												
			ResultSet results = psGetMaxLevel.executeQuery();
			
			if(!results.next())
				return 0;					
			
			return results.getInt(1);
		} catch (Exception e){
			log.severe("Error in getMaxLevel method: " + e.getMessage());
			LogUtil.LogException(e);
			return 0;
		}	
	}
	
	/**
	 * @return The next level to use when inserting into the levels table. (Essentially the largest right-value)
	 */
	public synchronized int getNextLevelGroup(){
		try {
			if(psGetMaxLevelGroup == null)
				psGetMaxLevelGroup = connection.prepareStatement("SELECT MAX(gid) FROM levels");
												
			ResultSet results = psGetMaxLevelGroup.executeQuery();
			
			if(!results.next())
				return 0;					
			
			return results.getInt(1) + 1;
		} catch (Exception e){
			log.severe("Error in getNextLevelGroup method: " + e.getMessage());
			LogUtil.LogException(e);
			return 0;
		}	
	}
	
	public synchronized boolean updateJobStatus(int jobId, String status, String node){
		try {
			if(psJobStatus == null)
					psJobStatus = connection.prepareStatement("UPDATE jobs SET status=?,node=?,finDate=SYSDATE() WHERE id=?");
			
			psJobStatus.setString(1, status);
			psJobStatus.setString(2, node);
			psJobStatus.setInt(3, jobId);
			
			LogUtil.LogInfo(psJobStatus.toString());
			
			return psJobStatus.executeUpdate() == 1;			
		} catch (Exception e){
			log.severe("Error in updateJobStatus method: " + e.getMessage());
			LogUtil.LogException(e);
			return false;
		}
	}
	
	public synchronized boolean updatePairResult(int pairId, String result){
		try {
			if(psPairStatus == null)
				psPairStatus = connection.prepareStatement("UPDATE job_pairs SET result=? WHERE id=?");
			
			psPairStatus.setString(1, result);
			psPairStatus.setInt(2, pairId);
			
			return psPairStatus.executeUpdate() == 1;			
		} catch (Exception e){
			log.severe("Error in updatePairResult method: " + e.getMessage());
			LogUtil.LogException(e);
			return false;
		}
	}
	
	/**
	 * Gets all virtual directories listed under another (children directories)
	 * @param id The is the parent directory
	 * @return A list of children directories of the parent
	 */
	public synchronized List<Level> getSubLevels(int id){
		try {
			ResultSet results;
			
			if(id < 0){
				if(psGetRootLevels == null)
					psGetRootLevels = connection.prepareStatement("SELECT * FROM levels WHERE dep=0");
				
				results = psGetRootLevels.executeQuery();
			} else {
				if(psGetSubLevels == null)
					psGetSubLevels = connection.prepareStatement("SELECT node.* FROM levels AS node, (SELECT lft, rgt, dep FROM levels WHERE id=?) AS parent WHERE node.lft > parent.lft AND node.rgt < parent.rgt AND node.dep=(parent.dep + 1)");
										
				psGetSubLevels.setInt(1, id);
				results = psGetSubLevels.executeQuery();
			}						
						 
			ArrayList<Level> returnList = new ArrayList<Level>(10);
			
			while(results.next()){
				Level l = new Level(results.getInt("id"));
				l.setGroupId(results.getInt("gid"));
				l.setLeft(results.getInt("lft"));
				l.setRight(results.getInt("rgt"));
				l.setName(results.getString("name"));
				l.setUserId(results.getInt("usr"));
				l.setDescription(results.getString("description"));
				
				returnList.add(l);
			}			
						
			return returnList;
		} catch (Exception e){
			log.severe("Error in getSubLevels method: " + e.getMessage());
			LogUtil.LogException(e);
			return null;
		}	
	}
	
	/**
	 * Gets all virtual directories listed under another (children directories) including
	 * benchmarks that are within each child sub-level
	 * @param id The is the parent directory
	 * @return A list of children directories of the parent
	 */
	public synchronized List<Level> getSubLevelsWithBench(int id){
		try {
			ResultSet results;
			
			if(id < 0){	// Get roots
				if(psGetRootLevels == null)
					psGetRootLevels = connection.prepareStatement("SELECT * FROM levels WHERE dep=0");
				
				results = psGetRootLevels.executeQuery();
			} else {
				if(psGetSubLevels == null)
					psGetSubLevels = connection.prepareStatement("SELECT node.* FROM levels AS node, (SELECT lft, rgt, dep FROM levels WHERE id=?) AS parent WHERE node.lft > parent.lft AND node.rgt < parent.rgt AND node.dep=(parent.dep + 1)");
										
				psGetSubLevels.setInt(1, id);
				results = psGetSubLevels.executeQuery();
			}			
						
			ArrayList<Level> returnList = new ArrayList<Level>(10);
			
			while(results.next()){
				Level l = new Level(results.getInt("id"));
				l.setGroupId(results.getInt("gid"));
				l.setLeft(results.getInt("lft"));
				l.setRight(results.getInt("rgt"));
				l.setName(results.getString("name"));
				l.setUserId(results.getInt("usr"));
				l.setDescription(results.getString("description"));
				
				if(psGetImmediateBench == null)
					psGetImmediateBench = connection.prepareStatement("SELECT * FROM benchmarks WHERE lvl=?");
				
				psGetImmediateBench.setInt(1, l.getLeft());
				
				ResultSet benchResult = psGetImmediateBench.executeQuery();	
				while(benchResult.next()){
					Benchmark benchmark = new Benchmark();
					benchmark.setId(benchResult.getInt("id"));					
					benchmark.setUserId(benchResult.getInt("usr"));
					benchmark.setUploaded(benchResult.getTimestamp("uploaded"));
					benchmark.setPath(benchResult.getString("physical_path"));	// TODO: Eventually we want to hide this
					l.getBenchmarks().add(benchmark);
				}
				
				returnList.add(l);
			}			
						
			return returnList;
		} catch (Exception e){
			log.severe("Error in getSubLevels method: " + e.getMessage());
			LogUtil.LogException(e);
			return null;
		}	
	}
	
	/**
	 * @param levelId An id of a virtual level
	 * @return A list of all benchmark IDs that are contained within that level (including all sublevels)
	 */
	public synchronized List<Integer> levelToBenchmarkIds(int levelId){
		try {
			if(psLevelToBenchs == null)
				psLevelToBenchs = connection.prepareStatement("SELECT bench.id FROM benchmarks AS bench, (SELECT lft, rgt, dep FROM levels WHERE id=?) AS parent WHERE bench.lvl BETWEEN parent.lft AND parent.rgt");
												
			psLevelToBenchs.setInt(1, levelId);
			ResultSet results = psLevelToBenchs.executeQuery();
			ArrayList<Integer> returnList = new ArrayList<Integer>(5);
			
			while(results.next())
				returnList.add(results.getInt("id"));							
						
			return returnList;
		} catch (Exception e){
			log.severe("Error in levelToBenchmarkIds method: " + e.getMessage());
			LogUtil.LogException(e);
			return null;
		}	
	}
	
	/**
	 * Gets all job pairs associated with a job.
	 * @param id The id of the job to retrieve pairs for
	 * @return A list of job pairs under the job
	 */
	public synchronized List<JobPair> getJobPairs(int id){
		try {						
			if(psGetJobPairs == null)
				psGetJobPairs = connection.prepareStatement("SELECT *, TIMESTAMPDIFF(SECOND, job_pairs.endTime,job_pairs.startTime) AS runtime FROM job_pairs JOIN benchmarks ON bid=benchmarks.id JOIN solvers ON sid=solvers.id WHERE jid=?");
			
			psGetJobPairs.setInt(1, id);
			ResultSet results = psGetJobPairs.executeQuery();									 
			List<JobPair> returnList = new ArrayList<JobPair>(10);
			
			while(results.next()){
				Benchmark b = new Benchmark();
				Solver s = new Solver();
				JobPair p = new JobPair();
				
				p.setId(results.getInt(1));
				p.setJobId(results.getInt(2));
				p.setResult(results.getString(5));
				p.setStartTime(results.getTimestamp(6));
				p.setEndTime(results.getTimestamp(7));
				p.setNode(results.getString(8));
				p.setStatus(results.getString(9));
				p.setRunTime(results.getInt(21));
				
				b.setId(results.getInt(4));
				b.setPath(results.getString(12));
				b.setLevel(results.getInt(14));
				b.setUserId(results.getInt(13));
				p.setBenchmark(b);
				
				s.setId(results.getInt(3));
				s.setName(results.getString(16));
				s.setUploaded(results.getDate(17));
				s.setUserId(results.getInt(19));
				s.setNotes(results.getString(20));
				s.setPath(results.getString(18));
				p.setSolver(s);
				
				returnList.add(p);
			}			
						
			return returnList;
		} catch (Exception e){
			log.severe("Error in getJobPairs method: " + e.getMessage());
			LogUtil.LogException(e);
			return null;
		}	
	}
	
	/**
	 * Gets all jobs in the database (without job pair info)
	 * @return A list of jobs in the database
	 */
	public synchronized List<Job> getJobs(){
		try {						
			if(psGetJobs == null)
				psGetJobs = connection.prepareStatement("SELECT * FROM jobs");
			
			ResultSet results = psGetJobs.executeQuery();									 
			List<Job> returnList = new ArrayList<Job>(10);
			
			while(results.next()){
				Job j = new Job();
				
				j.setCompleted(results.getTimestamp("finDate"));
				j.setDescription(results.getString("description"));
				j.setJobId(results.getInt("id"));
				j.setNode(results.getString("node"));
				j.setStatus(results.getString("status"));
				j.setSubmitted(results.getTimestamp("subDate"));
				j.setTimeout(results.getLong("timeout"));
				j.setUserId(results.getInt("usr"));
				
				returnList.add(j);
			}			
						
			return returnList;
		} catch (Exception e){
			log.severe("Error in getJobs method: " + e.getMessage());
			LogUtil.LogException(e);
			return null;
		}	
	}
	
	protected synchronized void autoCommitOn(){
		try {
			connection.setAutoCommit(true);
		} catch (Exception e) {
			// Ignore any errors
		}
	}
	
	protected synchronized void doRollback(){
		try {
			connection.rollback();
			log.warning("Database transaction rollback.");
		} catch (Exception e) {
			// Ignore any errors
		}
	}
}
