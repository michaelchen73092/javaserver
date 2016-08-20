package com.BoBiHealth;
import java.io.IOException;
//servlet

import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHandler;

import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.security.HashLoginService;
import org.eclipse.jetty.server.DebugListener;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.LowResourceMonitor;
import org.eclipse.jetty.server.NCSARequestLog;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.server.handler.*;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.util.thread.ScheduledExecutorScheduler;
import org.eclipse.jetty.http2.server.*;
import org.eclipse.jetty.http2.*;
import org.eclipse.jetty.alpn.server.*;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Slf4jLog;

//for test
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;



//for test



import org.eclipse.jetty.io.EndPoint;
import java.net.InetSocketAddress;

import javax.servlet.ServletContext;
import java.io.*;
import org.json.*;
import java.util.*;
import org.slf4j.*;




public class dynamoDBhandler extends AbstractHandler
{
    public static ServerConnector connector = null;
	public void handle(String target,
                       Request baseRequest,
                       HttpServletRequest request,
                       HttpServletResponse response)
        throws IOException, ServletException
    {
        response.setContentType("text/html;charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
        ServletContext context = baseRequest.getServletContext();
        Enumeration<String> enumeration = baseRequest.getAttributeNames();
        System.out.printf("method: %s\n", baseRequest.getMethod());
        System.out.printf("PathInfo: %s\n", baseRequest.getPathInfo());
        System.out.printf("Protocol: %s\n", baseRequest.getProtocol());
        System.out.printf("Context: %s\n", context.getContextPath());
        System.out.printf("Query: %s\n", baseRequest.getQueryString());
        System.out.printf("ContentType: %s\n", baseRequest.getContentType());
        System.out.printf("userName: %s\n", baseRequest.getParameter("userName"));
        System.out.printf("Time: %s\n",new Date().toString());
        System.out.printf("possible address:%s\n",baseRequest.getHeader("HTTP_X_FORWARDED_FOR"));
        //System.out.printf("userName: %s\n", baseRequest.getParameterValues("userName")[0]);
        //System.out.printf("userName: %s\n", baseRequest.getParameterValues("userName")[1]);
        Iterator<EndPoint> collection = connector.getConnectedEndPoints().iterator();
        while(collection.hasNext()){
        	EndPoint endPoint = collection.next();
        	InetSocketAddress address = endPoint.getRemoteAddress();
        	System.out.printf("address: %s\n",address.toString());		
        }
        
        StringBuilder jasonBuff = new StringBuilder();
        String line = null;
        try {
          BufferedReader reader = baseRequest.getReader();
          while ((line = reader.readLine()) != null)
            jasonBuff.append(line);
        } catch (Exception e) { 
        	System.out.println(e.getMessage());
        	e.getStackTrace();
        }
        String tempStr = jasonBuff.toString();
        System.out.println(tempStr);
        JSONObject jsonObject;
        try{
        	jsonObject = new JSONObject(tempStr);
        	System.out.println(jsonObject.getInt("ID"));
        	JSONArray strings = jsonObject.getJSONArray("userName");
        	for(int i=0;i<strings.length();i++){
        		System.out.println(strings.get(i).toString());
        	}

        }catch(Exception e){
        	System.out.println(e.getMessage());
        	e.getStackTrace();
        }

        while(enumeration.hasMoreElements()){
        	System.out.println(enumeration.nextElement());
        }
        baseRequest.setHandled(true);
        response.getWriter().println("<h1>Hello World</h1>");
    }

    public static void main(String[] args) throws Exception
    {	
    	
    	//Setup Threadpool
        //QueuedThreadPool threadPool = new QueuedThreadPool();
        //threadPool.setMaxThreads(500);
        Server server = new Server(8090);
        // HTTP Configuration
        HttpConfiguration http_config = new HttpConfiguration();
        http_config.setSecureScheme("https");
        http_config.setSecurePort(8443);
        http_config.setOutputBufferSize(32768);
        http_config.setRequestHeaderSize(8192);
        http_config.setResponseHeaderSize(8192);
        http_config.setSendServerVersion(true);
        http_config.setSendDateHeader(true);
        http_config.addCustomizer(new SecureRequestCustomizer());

        //http_config.setSecureScheme("https");
        //http_config.setSecurePort(8443);
        //http_config.setSendXPoweredBy(true);
        //http_config.setSendServerVersion(true);
        
        // SSL Context Factory
        SslContextFactory sslContextFactory = new SslContextFactory();
        setsslContexFactory(sslContextFactory);
        // SSL HTTP Configuration
        HttpConfiguration https_config = new HttpConfiguration(http_config);
        //HttpConfiguration https_config = new HttpConfiguration();

        //https_config.addCustomizer(new SecureRequestCustomizer());
    	//HTTP/2 setting
        
        //HttpConnectionFactory http1 = new HttpConnectionFactory(https_config);
        HTTP2ServerConnectionFactory http2 = new HTTP2ServerConnectionFactory(https_config);
        NegotiatingServerConnectionFactory.checkProtocolNegotiationAvailable();
        ALPNServerConnectionFactory alpn = new ALPNServerConnectionFactory();
        SslConnectionFactory sslConnectionFactory = new SslConnectionFactory(sslContextFactory,"alpn");
        //alpn.setDefaultProtocol(http1.getProtocol());
        
        // SSL Connector
        ServerConnector sslConnector = new ServerConnector(server,
            sslConnectionFactory,
            alpn,http2);
        sslConnector.setPort(8443);
        server.addConnector(sslConnector);
       /* ContextHandler contextHandler = new ContextHandler();
        contextHandler.setContextPath("/hello");
        contextHandler.setHandler(new dynamoDBhandler());*/
        ServletContextHandler contextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        contextHandler.setContextPath("/hello");
        contextHandler.setResourceBase(System.getProperty("java.io.tmpdir"));
        contextHandler.addServlet(HelloServlet.class, "/*");
        server.setHandler(contextHandler);
        //ServletHandler handler = new ServletHandler();
        //server.setHandler(handler);
        //handler.addServletWithMapping(HelloServlet.class,"/hello/*");
        //add servlet
        //looger setting
        Slf4jRequestLog requestLog = new Slf4jRequestLog();
        Log.setLog(new Slf4jLog());

        //connector = sslConnector;
    	server.setRequestLog(requestLog);
    	System.out.printf("LoggerName: %s\n",requestLog.getLoggerName());
        server.start();
        server.join();
    }
    public static void setsslContexFactory(SslContextFactory sslContextFactory){
        sslContextFactory.setKeyStorePath("C:/test.keystore");
        sslContextFactory.setKeyStorePassword("Cc5302196029");
        sslContextFactory.setKeyManagerPassword("Cc5302196029");
        sslContextFactory.setTrustStorePath("C:/test.keystore");
        sslContextFactory.setTrustStorePassword("Cc5302196029");
        //sslContextFactory.setProtocol(FAILED);
        sslContextFactory.setCipherComparator(HTTP2Cipher.COMPARATOR);
        sslContextFactory.setUseCipherSuitesOrder(true);
        /*sslContextFactory.setExcludeCipherSuites("SSL_RSA_WITH_DES_CBC_SHA",
                "SSL_DHE_RSA_WITH_DES_CBC_SHA", "SSL_DHE_DSS_WITH_DES_CBC_SHA",
                "SSL_RSA_EXPORT_WITH_RC4_40_MD5",
                "SSL_RSA_EXPORT_WITH_DES40_CBC_SHA",
                "SSL_DHE_RSA_EXPORT_WITH_DES40_CBC_SHA",
                "SSL_DHE_DSS_EXPORT_WITH_DES40_CBC_SHA");*/
    }
    @SuppressWarnings("serial")
    public static class HelloServlet extends HttpServlet
    {
        @Override
        protected void doGet( HttpServletRequest request,
                              HttpServletResponse response ) throws ServletException,
                                                            IOException
        {
        	String doctor = request.getParameter("doctor");
            System.out.printf("method: %s\n", request.getMethod());
            System.out.printf("PathInfo: %s\n",request.getPathInfo());
        	System.out.println("get called");
            response.setContentType("text/html");
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().println("<h1>Hello from HelloServlet</h1>");
            if(doctor != null)response.getWriter().printf("<h2>%s</h2>",doctor);
        }
    }
}
