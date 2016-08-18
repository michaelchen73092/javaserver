package com.BoBiHealth;

public interface doctorStatusDelegate {
	public boolean taken();
	public void turnOffline()throws InterruptedException;
	public void lockQueue();
	public void unlockQueue();
}
