package org.starexec.command;

/**
 * This class is responsible for validating the arguments given to functions in a Connection
 */



/*
 * TODO: Ensure that uploaded names and descriptions are in the correct format
 * TODO: Validate set commands and other upload commands
 */
import java.io.File;
import java.util.HashMap;
import java.util.regex.Pattern;



public class Validator {
	
	public static String[] VALID_ARCHIVETYPES={"zip","tar","tar.gz","tgz"};
	public static Pattern patternBoolean = Pattern.compile(R.BOOLEAN_PATTERN, Pattern.CASE_INSENSITIVE);										
	public static Pattern patternInteger = Pattern.compile(R.LONG_PATTERN);
	public static Pattern patternUserName = Pattern.compile(R.USER_NAME_PATTERN, Pattern.CASE_INSENSITIVE);
	public static Pattern patternInstitution = Pattern.compile(R.INSTITUTION_PATTERN, Pattern.CASE_INSENSITIVE);
	public static Pattern patternEmail = Pattern.compile(R.EMAIL_PATTERN, Pattern.CASE_INSENSITIVE);
	public static Pattern patternUrl = Pattern.compile(R.URL_PATTERN, Pattern.CASE_INSENSITIVE);
	public static Pattern patternPrimName = Pattern.compile(R.PRIMITIVE_NAME_PATTERN, Pattern.CASE_INSENSITIVE);
	public static Pattern patternPrimDesc = Pattern.compile(R.PRIMITIVE_DESC_PATTERN, Pattern.DOTALL);
	public static Pattern patternPassword = Pattern.compile(R.PASSWORD_PATTERN);
	public static Pattern patternRequestMsg = Pattern.compile(R.REQUEST_MESSAGE, Pattern.CASE_INSENSITIVE);
	/**
	 * Determines whether the given parameters form a valid delete request
	 * @param commandParams A HashMap of parameters given by the user at the command line
	 * @return 0 if valid and a negative error code otherwise.
	 * @author Eric Burns
	 */
	
	private static String missingParam=null;
	
	
	/**
	 * Gets the missing paramter that was last seen. If none has been seen yet, returns null.
	 * @return The name of the required parameter that is missing. 
	 */
	public static String getMissingParam() {
		return missingParam;
	}
	
	
	public static int isValidDeleteRequest(HashMap<String,String> commandParams) {
		if (! paramsExist(new String[]{R.PARAM_ID},commandParams)) {
			return R.ERROR_MISSING_PARAM;
		}
		if (!isValidPosInteger(commandParams.get(R.PARAM_ID))) {
			return R.ERROR_INVALID_ID;
		}
		return 0;
	}
	
	/**
	 * Determines whether the given string represents a valid id. Valid ids are integers greater than or equal to 0
	 * @param str
	 * @return True if valid, false otherwise.
	 * @author Eric Burns
	 */
	
	public static boolean isValidPosInteger(String str) {
		try {
			int check=Integer.parseInt(str);
			if (check<0) {
				return false;
			}
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	public static boolean isValidPosNumber(String str) {
		try {
			double check=Double.parseDouble(str);
			if (check<0) {
				return false;
			}
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	public static boolean isValidPrimName(String name){    	
    	return patternPrimName.matcher(name).matches();    	
    }
    
    /**
     * Validates a boolean value by ensuring it is something Boolean.parseBoolean()
     * can handle
     * 
     * @param boolString the string to check for a parse-able boolean value
     * @return true iff boolString isn't null and is either "true" or "false"
     */
    public static boolean isValidBool(String boolString){
    	return patternBoolean.matcher(boolString).matches();
    }
    
    /**
	 * Validates an institution field
	 * 
	 * @param institute the institution to validate
	 * @return true iff institute is less than R.INSTITUTION_LEN characters 
	 * and not null or the empty string
	 */
	public static boolean isValidInstitution(String institute){		
		return patternInstitution.matcher(institute).matches();		
	}
    
    /**
     * Validates a generic description and checks that it contains content and is less than 1024
     * characters long. ALL characters are allowed in descriptions.
     * 
     * @param desc the description to check
     * @return true iff name isn't null or empty and is less than 1024 characters
     */
    public static boolean isValidPrimDescription(String desc){
    	return patternPrimDesc.matcher(desc).matches();
    }
	
	/**
	 * @param urlParams-- A HashMap containing key/value pairs that will be encoded into the download URL
	 * @param A HashMap containing key/value pairs the user entered into the command line
	 * 
	 * @return 0 if the request is valid, and a negative error code if it is not
	 * @author Eric Burns
	 */
	public static int isValidDownloadRequest(HashMap<String,String> urlParams, HashMap<String,String>commandParams) {
		if (! paramsExist(new String[]{R.PARAM_ID,R.PARAM_OUTPUT_FILE},commandParams)) {
			return R.ERROR_MISSING_PARAM;
		}
		
		String outputLocale=commandParams.get(R.PARAM_OUTPUT_FILE);
		if (outputLocale==null) {
			return R.ERROR_INVALID_FILEPATH;
		}
		
		//if the file exists already, make sure the user explicitly wants to overwrite the existing file
		File testFile=new File(outputLocale);
		
		if (testFile.exists()) {
			if (!commandParams.containsKey(R.PARAM_OVERWRITE)) {
				System.out.println(testFile.getAbsolutePath());
				return R.ERROR_FILE_EXISTS;
			}
		}
		
		return 0;
	}
	/**
	 * This function checks all the properties that are common to all uploads. All uploads must specify an id,
	 * and either a filepath or a url
	 * @param commandParams A HashMap mapping String keys to String values
	 * @return 0  if the upload request has all the basic requirements, and a negative error code otherwise.
	 * @author Eric Burns
	 */
	private static int isValidUploadRequest(HashMap<String,String> commandParams) {
		
		//an ID and either a URL or a file is required for every upload
		if (! paramsExist(new String[]{R.PARAM_ID},commandParams)) {
			return R.ERROR_MISSING_PARAM;
		}
		if (!commandParams.containsKey(R.PARAM_FILE) && !commandParams.containsKey(R.PARAM_URL)) {
			missingParam=R.PARAM_FILE+" or "+R.PARAM_URL;
			return R.ERROR_MISSING_PARAM;
		}
		
		if (!isValidPosInteger(commandParams.get(R.PARAM_ID))) {
			return R.ERROR_INVALID_ID;
		}
		
		//if both a file and a url is specified, the upload is ambiguous-- only one or the other should be present
		if (commandParams.containsKey(R.PARAM_FILE) && commandParams.containsKey(R.PARAM_URL)) {
			return R.ERROR_FILE_AND_URL;
		}
		
		//if a file is specified (and it might not be if a URL is used), make sure that it 
		//exists and that it is of a valid extension
		if (commandParams.containsKey(R.PARAM_FILE)) {
			String filePath=commandParams.get(R.PARAM_FILE);
			File test=new File(commandParams.get(R.PARAM_FILE));
			boolean archiveGood=false;
			if (!test.exists()) {
				return R.ERROR_FILE_NOT_FOUND;
			}
			for (String suffix : VALID_ARCHIVETYPES) {
				if (filePath.endsWith(suffix)) {
					archiveGood=true;
					break;
				}
			}
			if (!archiveGood) {
				return R.ERROR_BAD_ARCHIVETYPE;
			}
		}
		
		//if a description file is specified, make sure it exists
		if (commandParams.containsKey(R.PARAM_DESCRIPTION_FILE)) {
			File test=new File(commandParams.get(R.PARAM_FILE));
			if (!test.exists()) {
				return R.ERROR_FILE_NOT_FOUND;
			}
		}
		
		//if a name is specified, it must conform to StarExec rules
		if (commandParams.containsKey(R.PARAM_NAME)) {
			if (!isValidPrimName(commandParams.get(R.PARAM_NAME))) {
				return R.ERROR_BAD_NAME;
			}
		}
		
		//if a description is specified, it must conform to StarExec rules
		if (commandParams.containsKey(R.PARAM_DESC)) {
			if (!isValidPrimDescription(commandParams.get(R.PARAM_NAME))) {
				return R.ERROR_BAD_DESCRIPTION;
			}
		}
		return 0;
	}
	
	/**
	 * Validates an upload in the same way as isValidUploadRequest, except that it ensures that only files
	 * and not URLs are allowed
	 * @param commandParams The key value pairs given by the user at the command line
	 * @return 0 if the request is valid and a negative error code otherwise
	 * @author Eric Burns
	 */
	
	private static int isValidUploadRequestNoURL(HashMap<String,String> commandParams) {
		int valid=isValidUploadRequest(commandParams);
		if (valid<0) {
			return valid;
		}
		
		//if no file exists, it must mean that only a url was specified
		if (!commandParams.containsKey(R.PARAM_FILE)) {
			return R.ERROR_URL_NOT_ALLOWED;
		}
		
		return 0;
	}
	
	public static int isValidBenchmarkUploadRequest(HashMap<String,String> commandParams) {
		return isValidUploadRequest(commandParams);
	}
	
	/**
	 * Validates a solver upload request 
	 * @param commandParams A HashMap mapping String keys to String values
	 * @return 0 if the upload request is valid, and a negative error code if it is not.
	 * @author Eric Burns
	 */
	public static int isValidSolverUploadRequest(HashMap<String,String> commandParams) {
		
		return isValidUploadRequest(commandParams);
		
	}
	
	public static int isValidSpaceUploadRequest(HashMap<String,String> commandParams) {
		return isValidUploadRequest(commandParams);
		
	}
	
	public static int isValidConfigUploadRequest(HashMap<String,String> commandParams) {
		return isValidUploadRequestNoURL(commandParams);
		
	}
	
	public static int isValidProcessorUploadRequest(HashMap<String,String> commandParams) {
		return isValidUploadRequestNoURL(commandParams);
	}
	
	
	public static int isValidGetPrimRequest(HashMap<String,String> urlParams,HashMap<String,String> commandParams) {
		if (! paramsExist(new String[]{R.PARAM_ID},commandParams)) {
			return R.ERROR_MISSING_PARAM;
		}
		
		
		return 0;
	}
	
	public static int isValidSleepCommand(HashMap<String,String> commandParams) {
		if (!paramsExist(new String[] {R.PARAM_TIME},commandParams)) {
			return R.ERROR_MISSING_PARAM;
		}
		if (!isValidPosNumber(commandParams.get(R.PARAM_TIME))) {
			return R.ERROR_BAD_TIME;
		}
		
		return 0;
	}
	
	/**
	 * Determines whether the given parameters form a valid job creation request
	 * @param commandParams A HashMap of key/value pairs indicating values given by the user at the command line
	 * @return 0 if the request is valid and a negative error code otherwise
	 * @author Eric Burns
	 */
	
	public static int isValidCreateJobRequest(HashMap<String,String> commandParams) {
		//Job creation must include a space ID, a processor ID, and a queue ID
		if (! paramsExist(new String[]{R.PARAM_ID,R.PARAM_PROCID,R.PARAM_QUEUEID},commandParams)) {
			return R.ERROR_MISSING_PARAM;
		}
		
		//all IDs should be integers greater than 0
		if (!isValidPosInteger(commandParams.get(R.PARAM_ID)) ||
				!isValidPosInteger(commandParams.get(R.PARAM_PROCID)) ||
				!isValidPosInteger(commandParams.get(R.PARAM_QUEUEID))) {
			return R.ERROR_INVALID_ID;
		}
		
		//timeouts should also be integers greater than 0
		if (commandParams.containsKey(R.PARAM_CPUTIMEOUT)) {
			if (!isValidPosInteger(commandParams.get(R.PARAM_CPUTIMEOUT))) {
				return R.ERROR_INVALID_TIMEOUT;
			}
		}
		
		if (commandParams.containsKey(R.PARAM_WALLCLOCKTIMEOUT)) {
			if (!isValidPosInteger(commandParams.get(R.PARAM_WALLCLOCKTIMEOUT))) {
				return R.ERROR_INVALID_TIMEOUT;
			}
		}
		
		if (commandParams.containsKey(R.PARAM_NAME)) {
			if (!isValidPrimName(commandParams.get(R.PARAM_NAME))) {
				return R.ERROR_BAD_NAME;
			}
		}
		if (commandParams.containsKey(R.PARAM_DESC)) {
			if (!isValidPrimDescription(commandParams.get(R.PARAM_DESC))) {
				return R.ERROR_BAD_DESCRIPTION;
			}
		}
		
		return 0;
	}
	
	
	
	/**
	 * This function determines whether a given subspace creation request is valid
	 * @param commandParams The key/value pairs given by the user at the command line
	 * @return 0 if the request is valid and a negative error code if not
	 * @author Eric Burns
	 */
	public static int isValidSubspaceCreation(HashMap<String,String> commandParams) {
		
		if (! paramsExist(new String[]{R.PARAM_ID},commandParams)) {
			return R.ERROR_MISSING_PARAM;
		}
		
		
		if (!isValidPosInteger(commandParams.get(R.PARAM_ID))) {
			return R.ERROR_INVALID_ID;
		}
		
		if (commandParams.containsKey(R.PARAM_NAME)) {
			if (!isValidPrimName(commandParams.get(R.PARAM_NAME))) {
				return R.ERROR_BAD_NAME;
			}
		}
		if (commandParams.containsKey(R.PARAM_DESC)) {
			if (!isValidPrimDescription(commandParams.get(R.PARAM_DESC))) {
				return R.ERROR_BAD_DESCRIPTION;
			}
		}
		
		return 0;
	}
	
	/**
	 * This function determines whether a given request to set a subspace to public or private is valid
	 * @param commandParams The key/value pairs given by the user at the command line
	 * @return 0 if the request is valid and a negative error code if not
	 * @author Eric Burns
	 */
	public static int isValidSetSpaceVisibilityRequest(HashMap<String,String> commandParams) {
		if (! paramsExist(new String[]{R.PARAM_ID},commandParams)) {
			return R.ERROR_MISSING_PARAM;
		}
		
		if (!isValidPosInteger(commandParams.get(R.PARAM_ID))) {
			return R.ERROR_INVALID_ID;
		}
		
		return 0;
	}
	
	public static int isValidLoginRequest(HashMap<String,String> commandParams) {
		if (!paramsExist(new String[]{R.PARAM_USERNAME},commandParams)) {
			return R.ERROR_MISSING_PARAM;
		}
		
		if (!commandParams.get(R.PARAM_USERNAME).equals(R.PARAM_GUEST)) {
			if (!paramsExist(new String[] {R.PARAM_PASSWORD},commandParams)) {
				return R.ERROR_MISSING_PARAM;
			}
		}
		
		return 0;
	}
	
	public static int isValidPollJobRequest(HashMap<String,String> commandParams) {
		if (!paramsExist(new String[] {R.PARAM_ID,R.PARAM_OUTPUT_FILE,R.PARAM_TIME}, commandParams)) {
			return R.ERROR_MISSING_PARAM;
		}
		
		if (!isValidPosNumber(commandParams.get(R.PARAM_TIME))) {
			return R.ERROR_BAD_TIME;
		}
		
		String outputLocale=commandParams.get(R.PARAM_OUTPUT_FILE);
		if (outputLocale==null) {
			return R.ERROR_INVALID_FILEPATH;
		}
		
		//if the file exists already, make sure the user explicitly wants to overwrite the existing file
		File testFile=new File(outputLocale);
		
		if (testFile.exists()) {
			if (!commandParams.containsKey(R.PARAM_OVERWRITE)) {
				return R.ERROR_FILE_EXISTS;
			}
		}
		
		
		return 0;
	}
	
	
	public static int isValidRunFileRequest(HashMap<String,String> commandParams) {
		if (!paramsExist(new String[] {R.PARAM_FILE},commandParams)) {
			return R.ERROR_MISSING_PARAM;
		}
		File file=new File(commandParams.get(R.PARAM_FILE));
		if (!file.exists()) {
			return R.ERROR_FILE_NOT_FOUND;
		}
		
		return 0;
	}
	
	public static int isValidSetUserSettingRequest(String setting, String newVal) {
		
		if (setting.equals("archivetype")) {
			if (newVal.startsWith(".")) {
				newVal=newVal.substring(1);
			}
			
			if (!Validator.validArchiveType(newVal)) {
				
				return R.ERROR_BAD_ARCHIVETYPE;
			}
		}
		
		if (newVal==null) {
			missingParam=R.PARAM_VAL;
			return R.ERROR_MISSING_PARAM;
		}
		return 0;
	}
	
	/**
	 * Checks to see if the String type represents a valid archive type on Starexec
	 * @param type Should be zip, tar, tar.gz, or tgz
	 * @return True if valid, false otherwise
	 * @author Eric Burns
	 */
	
	public static boolean validArchiveType(String type) {
		type=type.toLowerCase();
		for (String x : VALID_ARCHIVETYPES) {
			if (type.equals(x)) {
				return true;
			}
		}
		return false;
	}
	
	private static boolean paramsExist(String[] params, HashMap<String,String> commandParams) {
		for (String param : params) {
			if (!commandParams.containsKey(param)) {
				missingParam=param;
				return false;
			}
		}
		return true;
	}
	
	
}
