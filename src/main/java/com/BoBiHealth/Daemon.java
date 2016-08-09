package com.BoBiHealth;
import org.eclipse.jetty.server.Server;
public class Daemon {
	public static void main(String[] args) throws Exception
	{
		Server server = new Server(8088);
	}
}
