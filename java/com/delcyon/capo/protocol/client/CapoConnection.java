/**
Copyright (C) 2012  Delcyon, Inc.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.delcyon.capo.protocol.client;

import java.io.BufferedInputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.security.PrivateKey;
import java.security.Signature;
import java.util.logging.Level;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.xml.bind.DatatypeConverter;

import com.delcyon.capo.CapoApplication;
import com.delcyon.capo.Configuration.PREFERENCE;
import com.delcyon.capo.client.CapoClient;

/**
 * @author jeremiah
 *
 */
public class CapoConnection
{

	
	
	private Socket socket;	
	private BufferedInputStream inputStream;
	private OutputStream outputStream;
    private String serverAddress;
    private int port;

	public CapoConnection() throws Exception
	{
	    serverAddress = CapoApplication.getConfiguration().getValue(PREFERENCE.SERVER_LIST).split(",")[0];
        port = CapoApplication.getConfiguration().getIntValue(PREFERENCE.PORT);
		open();					
	}
	
	public CapoConnection(String serverAddress,int port) throws Exception
	{
	    this.serverAddress = serverAddress;
	    this.port = port;
	    open();
	}
	
	/**
	 * A little control logic so we can reuse sockets if we have them, as well as figuring out which server to connect to
	 * @param socket
	 * @return
	 * @throws Exception
	 */
	
	public Socket open() throws Exception
	{
		
		if (this.socket == null || socket.isClosed())
		{
			
			CapoClient.logger.log(Level.FINE, "Opening Socket to "+serverAddress+":"+port);
			if (CapoApplication.getSslSocketFactory() != null)
			{				
				this.socket = CapoApplication.getSslSocketFactory().createSocket(serverAddress, port);
			}
	        else 
	        {
	        	this.socket = new Socket(serverAddress, port);
	        }
			
		}
		socket.setKeepAlive(true);
		this.inputStream = new BufferedInputStream(socket.getInputStream()); 
		this.outputStream = socket.getOutputStream();
		if (CapoApplication.getKeyStore() != null && CapoApplication.getSslSocketFactory() != null)
		{
			CapoApplication.logger.finer("SSL SID:"+DatatypeConverter.printHexBinary(((SSLSocket)socket).getSession().getId()));
			String clientID = CapoApplication.getConfiguration().getValue(CapoClient.Preferences.CLIENT_ID);
			char[] password = CapoApplication.getConfiguration().getValue(PREFERENCE.KEYSTORE_PASSWORD).toCharArray();
			String authMessage = "AUTH:CID="+clientID;

			PrivateKey privateKey = (PrivateKey) CapoApplication.getKeyStore().getKey(clientID+".private", password);
			Signature signature = Signature.getInstance("SHA256withRSA");        
			signature.initSign(privateKey);
			signature.update(clientID.getBytes());
			signature.update(((SSLSocket)socket).getSession().getId());
			authMessage += ":SIG="+DatatypeConverter.printHexBinary(signature.sign())+":";			
			this.outputStream.write(authMessage.getBytes());
			this.outputStream.flush();
			this.inputStream.read();
		}
		//check for a busy signal
		this.outputStream.write("CAPO_REQUEST".getBytes());
		this.outputStream.flush();
		byte[] buffer = new byte[256];
		this.inputStream.read(buffer);
		
		String message = new String(buffer).trim();
		//TODO move this into capo connection, so that any connection attempt will handle a busy signal
		//check to see if this is a busy message
		if (message.matches("BUSY \\d+"))
		{
			long delaytime = Long.parseLong(message.replaceAll("BUSY (\\d+)", "$1"));
			close();
			CapoApplication.logger.log(Level.WARNING, "Server Busy. Retrying connection in "+delaytime+"ms.");
			Thread.sleep(delaytime);
			open();				
		}
		else if (message.matches("OK") == false)
		{				
			throw new Exception("Unknown message from server: '"+message+"'");				
		}
		CapoApplication.logger.log(Level.FINE, "Opened Socket: "+socket);
		return socket;
	}
	
	
	//CS & SS
	@Override
	protected void finalize() throws Throwable
	{
		super.finalize();
		close();
	}
	
	//CS & SS
	public void close()
	{
		
		if (socket != null)
		{
			try
			{
				socket.close();
				if (socket instanceof SSLSocket)
				{
					((SSLSocket) socket).getSession().invalidate();
				}
				socket = null;
			}
			catch (Exception e)
			{
				//we don't really care if this fails.
			}
		}
		if (outputStream != null)
		{
			try
			{
				outputStream.close();
			}
			catch (Exception e)
			{
				//do nothing on failure
			}
		}
		if (inputStream != null)
		{
			try
			{
				inputStream.close();
			}
			catch (Exception e)
			{
				//do nothing on failure
			}
		}
	}
	
	
	
	/**
	 * This returns the output stream of the connection
	 * @return
	 */
	public OutputStream getOutputStream()
	{
		return this.outputStream;
	}


	public BufferedInputStream getInputStream()
	{
		return this.inputStream;
	}
	
}
