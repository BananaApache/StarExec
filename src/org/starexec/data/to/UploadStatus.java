/**
 * 
 */
package org.starexec.data.to;

import java.sql.Timestamp;

/**
 * 
 *	Object storing the status of a benchmark upload.
 *
 * @author Benton McCune
 *
 */
public class UploadStatus {
	private int id;
	private int spaceId;
	private int userId;
	private Timestamp uploadDate;
	private boolean fileUploadComplete;
	private boolean fileExtractionComplete;
	private boolean processingBegun;
	private int totalSpaces;
	private int completedSpaces;
	private int totalBenchmarks;
	private int completedBenchmarks;
	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}
	/**
	 * @return the spaceId
	 */
	public int getSpaceId() {
		return spaceId;
	}
	/**
	 * @param spaceId the spaceId to set
	 */
	public void setSpaceId(int spaceId) {
		this.spaceId = spaceId;
	}
	/**
	 * @return the userId
	 */
	public int getUserId() {
		return userId;
	}
	/**
	 * @param userId the userId to set
	 */
	public void setUserId(int userId) {
		this.userId = userId;
	}
	/**
	 * @return the uploadDate
	 */
	public Timestamp getUploadDate() {
		return uploadDate;
	}
	/**
	 * @param uploadDate the uploadDate to set
	 */
	public void setUploadDate(Timestamp uploadDate) {
		this.uploadDate = uploadDate;
	}
	/**
	 * @return the fileUploadComplete
	 */
	public boolean isFileUploadComplete() {
		return fileUploadComplete;
	}
	/**
	 * @param fileUploadComplete the fileUploadComplete to set
	 */
	public void setFileUploadComplete(boolean fileUploadComplete) {
		this.fileUploadComplete = fileUploadComplete;
	}
	/**
	 * @return the fileExtractionComplete
	 */
	public boolean isFileExtractionComplete() {
		return fileExtractionComplete;
	}
	/**
	 * @param fileExtractionComplete the fileExtractionComplete to set
	 */
	public void setFileExtractionComplete(boolean fileExtractionComplete) {
		this.fileExtractionComplete = fileExtractionComplete;
	}
	/**
	 * @return the processingBegun
	 */
	public boolean isProcessingBegun() {
		return processingBegun;
	}
	/**
	 * @param processingBegun the processingBegun to set
	 */
	public void setProcessingBegun(boolean processingBegun) {
		this.processingBegun = processingBegun;
	}
	/**
	 * @return the totalSpaces
	 */
	public int getTotalSpaces() {
		return totalSpaces;
	}
	/**
	 * @param totalSpaces the totalSpaces to set
	 */
	public void setTotalSpaces(int totalSpaces) {
		this.totalSpaces = totalSpaces;
	}
	/**
	 * @return the completedSpaces
	 */
	public int getCompletedSpaces() {
		return completedSpaces;
	}
	/**
	 * @param completedSpaces the completedSpaces to set
	 */
	public void setCompletedSpaces(int completedSpaces) {
		this.completedSpaces = completedSpaces;
	}
	/**
	 * @return the totalBenchmarks
	 */
	public int getTotalBenchmarks() {
		return totalBenchmarks;
	}
	/**
	 * @param totalBenchmarks the totalBenchmarks to set
	 */
	public void setTotalBenchmarks(int totalBenchmarks) {
		this.totalBenchmarks = totalBenchmarks;
	}
	/**
	 * @return the completedBenchmarks
	 */
	public int getCompletedBenchmarks() {
		return completedBenchmarks;
	}
	/**
	 * @param completedBenchmarks the completedBenchmarks to set
	 */
	public void setCompletedBenchmarks(int completedBenchmarks) {
		this.completedBenchmarks = completedBenchmarks;
	}
	
}



