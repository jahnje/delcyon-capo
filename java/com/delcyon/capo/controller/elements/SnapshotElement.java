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
package com.delcyon.capo.controller.elements;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.PosixFileAttributes;
import java.util.logging.Level;

import org.tanukisoftware.wrapper.WrapperManager;
import org.w3c.dom.Element;

import com.delcyon.capo.CapoApplication;
import com.delcyon.capo.client.CapoClient.Preferences;
import com.delcyon.capo.controller.AbstractClientSideControl;
import com.delcyon.capo.controller.ControlElementProvider;
import com.delcyon.capo.controller.client.ClientSideControl;
import com.delcyon.capo.xml.XPath;

/**
 * @author jeremiah
 *
 */
@ControlElementProvider(name="snapshot")
public class SnapshotElement extends AbstractClientSideControl implements ClientSideControl
{

	
	
	private enum Attributes
	{
		name, startPath,endPath
		
	}
	
	
	private static final String[] supportedNamespaces = {CapoApplication.SERVER_NAMESPACE_URI,CapoApplication.CLIENT_NAMESPACE_URI};
	
	
	
	@Override
	public Attributes[] getAttributes()
	{
		return Attributes.values();
	}
	
	@Override
	public Attributes[] getRequiredAttributes()
	{
		return new Attributes[]{};
	}

	
	@Override
	public String[] getSupportedNamespaces()
	{
		return supportedNamespaces;
	}

	
	
	
	

	@Override
	public Element processClientSideElement() throws Exception
	{
	    
	    CapoApplication.logger.log(Level.INFO, "Asking client for snapshot");
		//Return something here so that the server stops waiting for a document to read, and will shut down the connection. 
		return (Element)(getControlElementDeclaration().cloneNode(true));
	}
	
	
	@Override
	public Object processServerSideElement() throws Exception
	{
		if (getControlElementDeclaration().getNamespaceURI().equals(CapoApplication.CLIENT_NAMESPACE_URI))
		{
		    CapoApplication.logger.log(Level.INFO, "Asking client to capture snapshot.");
			getControllerClientRequestProcessor().sendServerSideClientElement((Element) getParentGroup().replaceVarsInAttributeValues(getControlElementDeclaration().cloneNode(true)));
		}
		else
		{
		    CapoApplication.logger.log(Level.INFO, "Asking server for snapshot");
		    Files.walkFileTree(Paths.get(getAttributeValue(Attributes.startPath)), new FileVisitor<Path>()
            {

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException
                {
                    // TODO Auto-generated method stub
                    System.out.println(dir);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
                {
                    // TODO Auto-generated method stub
                   
                    PosixFileAttributes posixFileAttributes = Files.getFileAttributeView(file, java.nio.file.attribute.PosixFileAttributeView.class).readAttributes();
                    System.out.println(file +"-->"+posixFileAttributes.permissions());
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException
                {
                    // TODO Auto-generated method stub
                    System.out.println(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException
                {
                    System.out.println(dir);
                    return FileVisitResult.CONTINUE;
                }

                
            });
		}
		
		return null;

	}

	
}
