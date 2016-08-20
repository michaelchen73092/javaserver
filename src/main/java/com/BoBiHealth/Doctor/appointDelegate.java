package com.BoBiHealth.Doctor;
import java.util.*;
import com.BoBiHealth.dynamoDB.*;
public interface appointDelegate {
	public void addAppointToQueue(ItemV2 item);
	public String getEmail();
}
