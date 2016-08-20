package com.BoBiHealth;
import org.eclipse.jetty.http2.client.*;
import org.eclipse.jetty.util.ssl.*;
import org.eclipse.jetty.util.*;
import org.eclipse.jetty.http2.api.*;
import java.net.*;
import org.eclipse.jetty.http.*;
import org.eclipse.jetty.http2.api.server.*;
import java.util.concurrent.*;
import org.eclipse.jetty.http2.frames.*;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import bolts.*;
import org.json.*;
import java.util.*;
public class PipeManager {
	public final static PipeManager instance = new PipeManager();
	private final HTTP2Client http2Client;
	private final String host;
	private final int port;
	private Session[] session;
	private SslContextFactory sslContextFactory;
	public PipeManager(){
		http2Client = new HTTP2Client();
		host = "localhost";
		port = 8443;
		session = new Session[1];
	}
	
	public static void main(String args[]){
		PipeManager manager = PipeManager.instance;
		try{
			
			manager.Get("/hello","/", "doctor=test");
			Thread.sleep(60000);
			manager.Get("/hello","/", "doctor=test");

		}catch(Exception exception){
			System.out.println("exception happen");
			System.out.println(exception.getMessage());
			exception.printStackTrace(System.out);
		}
	}
	public void start()throws Exception{
		this.sslContextFactory = new SslContextFactory();
		this.sslContextFactory.setTrustAll(true);
		http2Client.addBean(sslContextFactory);
		http2Client.start();
		
		FuturePromise<Session> sessionPromise = new FuturePromise<Session>();
		http2Client.connect( sslContextFactory,new InetSocketAddress("127.0.0.1",8443), new ServerSessionListener.Adapter(){
			@Override
			public void onClose(Session session,GoAwayFrame frame) {
			System.out.println("Session is onClose");
				// TODO Auto-generated method stub
				super.onClose(session, frame);
			}
			@Override
			public void onFailure(Session session, java.lang.Throwable failure) {
				// TODO Auto-generated method stub
				System.out.println("Session is onFailure");
				super.onFailure(session, failure);
			}
			@Override
			public void onAccept(Session session) {
				// TODO Auto-generated method stub
				System.out.println("Session is Accepted!!");
				super.onAccept(session);
			}
			@Override
			public boolean onIdleTimeout(org.eclipse.jetty.http2.api.Session session) {
				// TODO Auto-generated method stub
				System.out.println("idleTime");
				return super.onIdleTimeout(session);
			}
		}, sessionPromise);
		
		session[0] = sessionPromise.get(5, TimeUnit.SECONDS);
		System.out.println("Finally get Session from Server");
	}
	private String byteToString(ByteBuffer byteBuffer){
		return new String(byteBuffer.array(),byteBuffer.arrayOffset()+byteBuffer.position(),byteBuffer.remaining());
	}
	private int extractStatus(String meta){
		String result = meta.substring(meta.indexOf("=")+1,meta.indexOf(","));
		System.out.println(result);
		return new Integer(result).intValue();
	}
	private FuturePromise<Session>  conNect(){
		FuturePromise<Session> sessionPromise = new FuturePromise<Session>();
		http2Client.connect( this.sslContextFactory,new InetSocketAddress("127.0.0.1",8443), new ServerSessionListener.Adapter(){
			@Override
			public void onClose(Session session,GoAwayFrame frame) {
			System.out.println("Session is onClose");
				// TODO Auto-generated method stub
				super.onClose(session, frame);
			}
			@Override
			public void onFailure(Session session, java.lang.Throwable failure) {
				// TODO Auto-generated method stub
				System.out.println("Session is onFailure");
				super.onFailure(session, failure);
			}
			@Override
			public void onAccept(Session session) {
				// TODO Auto-generated method stub
				System.out.println("Session is Accepted!!");
				super.onAccept(session);
			}
			@Override
			public boolean onIdleTimeout(org.eclipse.jetty.http2.api.Session session) {
				// TODO Auto-generated method stub
				System.out.println("idleTime");
				return super.onIdleTimeout(session);
			}
		}, sessionPromise);
		return sessionPromise;
	}
	public void Get(String contex,String path,String parameter)throws Exception{
		try{
		if(!http2Client.isStarted()) start();
		}catch(Exception exception){
			System.out.println("Exception in start");
			System.out.println(exception.getMessage());
			exception.printStackTrace(System.out);
			return;
		}
		
		HttpFields httpField = new HttpFields();
		httpField.put("User-Agent",http2Client.getClass().getName()+"/"+Jetty.VERSION);
		MetaData.Request request = new MetaData.Request("GET", new HttpURI("http://"+"localhost:8443"+contex+path+"?"+parameter), HttpVersion.HTTP_2, httpField);
		HeadersFrame headersFrame = new HeadersFrame(request, null, true);
		TaskCompletionSource<JSONObject> taskCompletionSource = new TaskCompletionSource<JSONObject>();
		Stream.Listener listener = new Stream.Listener.Adapter(){
			@Override
			public void onHeaders(Stream stream,HeadersFrame frame){
				HttpFields httpFields = frame.getMetaData().getFields();
				Iterator<HttpField> iterator = frame.getMetaData().iterator();
				while(iterator.hasNext()){
					HttpField field = iterator.next();
					System.out.printf("name:%s\n",field.getName());
					System.out.printf("value:%s\n",field.getValues());

				}
				System.out.printf("status cod: %d\n",extractStatus(frame.getMetaData().toString()));
				System.out.printf("version:%s\n"
						+ "",frame.getMetaData().getVersion().toString());
				System.out.printf("Meta:%s\n",frame.getMetaData().toString());
				//System.out.printf("the response is %s\n",httpFields.get("status"));
			}
			@Override
			public void onData(Stream stream,DataFrame frame, Callback callback){
				ByteBuffer byteBuffer = frame.getData();
				System.out.printf("the response for data is %s\n",byteToString(byteBuffer));
				callback.succeeded();
			}
		};
		FuturePromise<Stream> streamPromise = new FuturePromise<Stream>();
		synchronized (session) {
			if(session[0].isClosed()){
				System.out.println("Session is closed");
				session[0] = conNect().get(5,TimeUnit.SECONDS);
			}else{
				System.out.println("Session is open");
			}
			session[0].newStream(headersFrame, streamPromise, listener);

			
		}
	
		
		Stream stream = streamPromise.get(1, TimeUnit.SECONDS);
	}
	
}
