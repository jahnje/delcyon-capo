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
package com.delcyon.capo.resourcemanager;

import java.io.InputStream;
import java.io.OutputStream;

import org.w3c.dom.Element;

import com.delcyon.capo.controller.ControlElement;
import com.delcyon.capo.controller.VariableContainer;
import com.delcyon.capo.resourcemanager.types.ContentMetaData;
import com.delcyon.capo.xml.dom.ResourceDeclarationElement;

/**
 * @author jeremiah
 * Resource Descriptors have the following life cycle: Their is constructor is called then their init() method is called when a ResourceElement declaration is encountered. 
 * Then based on their context attribute their load() method may be called or delayed until a reference to the named resource is encountered. 
 * When a reference is encountered, and initialized its getInputStream() or getOutputStream() is then called and used.
 * After the referencing Element or Group using the resource reference is done the unload() method is called.
 * After the Resource Element declaration falls out of scope, the release() method is called.
 * This allows a Resource to be Declared at a high level, allocate any needed resources, and then be used in multiple places in the configuration files.
 */
public interface ResourceDescriptor
{
	
	public enum LifeCycle
	{
		/** Resource Descriptor will be CLOSED and RELEASED whne group is exited*/
		GROUP,
		/** Resource Descriptor will be CLOSED and RELASED when calling ControlElement is exited*/
		REF,
		/** Resource Descriptor will not be closed automatically */
		APPLICATION,
		/** Resource Descriptor will is User Controlled via open and close control elements */
		EXPLICIT
	}

	public enum Action
	{
		CREATE,
		DELETE, 
		SET_ATTRIBUTE
	}
	
	/** This represents the state of a resource. 
	 * The next state can only be set by calling the relevant method on the resource. 
	 * A previous state can be set by using the reset(state) method.  
	 * Ordinal is used on this enum, so the order of these declarations is important an should not be changed **/
	public enum State
	{
		NONE,
		INITIALIZED,
		OPEN,
		STEPPING,
		CLOSED,
		RELEASED
	}
	
	public enum StreamType
	{
		OUTPUT, INPUT, ERROR
	}

	public enum StreamFormat
	{
		/** This is a raw data Stream using standard reads and writes*/
		STREAM,
		/** A Stream of this nature will return a raw block of data as a byte[] when read, and will take a byte[] as a write parameter */
		BLOCK,
		/** A Stream of this type will accept an XML node as a write parameter, and return an XML node from a read call*/
		XML_BLOCK,
		/**A stream of this type cannot be written to or read from, only called via the process method, For example a jdbc resource does all of its updating via parameters*/
		PROCESS
		
	}

    public enum DefaultParameters 
    {
        /** indicates that we are dealing with a container of some sort (true|false)*/
        CONTAINER
        
    }
	
	
	
	/** Returns the types of streams that this resource descriptor supports 
	 * @throws Exception */
	public StreamType[] getSupportedStreamTypes() throws Exception;
	public boolean isSupportedStreamType(StreamType streamType) throws Exception;
	/**Returns the supported StreamFormats for a particular Stream Type
	 * @throws Exception */
	public StreamFormat[] getSupportedStreamFormats(StreamType streamType) throws Exception;
	public boolean isSupportedStreamFormat(StreamType streamType, StreamFormat streamFormat) throws Exception;
	/** Returns the current State of the resource descriptor as a whole. 
	 * @throws Exception */
	public State getResourceState() throws Exception;
	/** Returns the state of a particular stream
	 * @throws Exception */
	@Deprecated //TODO remove this, seems we've never used it.
	public State getStreamState(StreamType streamType) throws Exception;
	
	public boolean performAction(VariableContainer variableContainer, Action action,ResourceParameter... resourceParameters) throws Exception;
	public boolean isSupportedAction(Action action) throws Exception;
	public boolean isRemoteResource();
	//BEGIN BASIC LIFECYCLE METHODS
	/** used to set the resource to a previous state, will throw an exception if the state requested is not a previous state**/
	public void reset(State previousState) throws Exception;	
	/**Called by resource type when Descriptor is constructed
	 * @throws Exception */
	public void setup(ResourceType resourceType, String resourceURI) throws Exception;
	/** occurs when a ResourceElement is declared 
	 * @param declaringResourceElement TODO*/
	public void init(ResourceDeclarationElement declaringResourceElement, VariableContainer variableContainer,LifeCycle lifeCycle,boolean iterate, ResourceParameter... resourceParameters) throws Exception;
	/** occurs after the resource is initialized/declared is controlled by LifeCycle attribute/parameter */
	public void open(VariableContainer variableContainer, ResourceParameter... resourceParameters) throws Exception;
	
	
	/**Returns an XML node, and requires that the stream support the XML_BLOCK format */
	public Element readXML(VariableContainer variableContainer,ResourceParameter... resourceParameters) throws Exception;
	/** write an XML node, and requires that the output stream support the XML_BLOCk format */
	public void writeXML(VariableContainer variableContainer,Element element,ResourceParameter... resourceParameters) throws Exception;
	
	
	/**Returns an byte[] node, and requires that the stream support the BLOCK format */
	public byte[] readBlock(VariableContainer variableContainer,ResourceParameter... resourceParameters) throws Exception;
	/** write an byte[], and requires that the output stream support the BLOCk format */
	public void writeBlock(VariableContainer variableContainer,byte[] block,ResourceParameter... resourceParameters) throws Exception;
	
	/**Causes a read from the descriptor to move it to he next data iteration*/
	public boolean next(VariableContainer variableContainer, ResourceParameter... resourceParameters) throws Exception;
	
	/** Requires that the resource support the PROCESS stream format on the OUTPUT stream. Used to send commands to the resourceDescriptor*/
	@Deprecated //TODO this may not actually do anything useful
	public void processOutput(VariableContainer variableContainer,ResourceParameter... resourceParameters) throws Exception;	
	/** Requires that the resource support the PROCESS stream format on the INPUT stream, Used to send commands to the resourceDescriptor*/
	@Deprecated //TODO this may not actually do anything useful
	public void processInput(VariableContainer variableContainer,ResourceParameter... resourceParameters) throws Exception;
	
	/** Returns an inputStream for reading, and requires that the resource support the STREAM format type */
	public InputStream getInputStream(VariableContainer variableContainer, ResourceParameter... resourceParameters) throws Exception;
	/** Returns an output stream for writing to, and requires that the resource support the STREAM format type */
	public OutputStream getOutputStream(VariableContainer variableContainer, ResourceParameter... resourceParameters) throws Exception;
	
	/** occurs when were finished with this use of the resource */
	public void close(VariableContainer variableContainer, ResourceParameter... resourceParameters) throws Exception;
	/** occurs when the resource falls out of scope, and is dependent on the lifeCycle attribute to define that scope. */
	public void release(VariableContainer variableContainer, ResourceParameter... resourceParameters) throws Exception;
	
	/** This will build the content meta data which requires reading the resource*/
	public ContentMetaData getContentMetaData(VariableContainer variableContainer, ResourceParameter... resourceParameters) throws Exception;
	
	/** This is the ContentMeta Data available after an iteration of the stream, and iteration can be a full read*/
	public ContentMetaData getIterationMetaData(VariableContainer variableContainer, ResourceParameter... resourceParameters) throws Exception;
	
	/** This returns the scope of a resource
	 * @throws Exception */
	public abstract LifeCycle getLifeCycle() throws Exception;
	
	/** get the official name URN of our resource*/
	public abstract ResourceURI getResourceURI();

	public abstract String getLocalName();
	
	public ResourceType getResourceType();	
		
	/** adds a resource parameter to the list of existing parameters
	 * @param parentGroup 
	 * @throws Exception 
	 */ 
	public abstract void addResourceParameters(VariableContainer variableContainer, ResourceParameter... resourceParameters) throws Exception;
	
    public abstract ResourceDescriptor getChildResourceDescriptor(ControlElement callingControlElement, String relativeURI) throws Exception;
    
	
	
//	/** processes any resource parameters found, and populates any variables found from the current context
//	 * If we are in the initialization phase any declared variables marked as NOW (the default) in the in the declaration will be processed, using any previously declared parameters, and then working up the scoped group variables. 
//	 * If we are in the calling phase parameters will be evaluated in the order they are encountered. 
//	 * Caller scoped parameters will be evaluated before being used as a value in a locally defined caller parameter.  
//	 */
//	public abstract void processParameterVars(ControlElement controlElement, EvaluationContext evaluationContext) throws Exception;
	
	
}
