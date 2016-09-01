package com.BoBiHealth.Doctor;

public interface doctorStatusDelegate {
	public boolean taken();
	public void turnOffline(boolean endbyDoctor)throws InterruptedException;
	public boolean isOnline();
	public void lockQueue();
	public void unlockQueue();
}
