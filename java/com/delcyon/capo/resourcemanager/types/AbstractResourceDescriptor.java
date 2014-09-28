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
package com.delcyon.capo.resourcemanager.types;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;
import java.util.Vector;
import java.util.logging.Level;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.delcyon.capo.CapoApplication;
import com.delcyon.capo.controller.ControlElement;
import com.delcyon.capo.datastream.NullOutputStream;
import com.delcyon.capo.datastream.StreamEventFilterOutputStream;
import com.delcyon.capo.datastream.StreamEventListener;
import com.delcyon.capo.datastream.StreamUtil;
import com.delcyon.capo.resourcemanager.ContentFormatType;
import com.delcyon.capo.resourcemanager.ResourceDescriptor;
import com.delcyon.capo.resourcemanager.ResourceParameter;
import com.delcyon.capo.resourcemanager.ResourceParameter.EvaluationContext;
import com.delcyon.capo.resourcemanager.ResourceParameter.Source;
import com.delcyon.capo.resourcemanager.ResourceType;
import com.delcyon.capo.resourcemanager.ResourceURI;
import com.delcyon.capo.server.CapoServer;
import com.delcyon.capo.util.ReflectionUtility;
import com.delcyon.capo.xml.XPath;
import com.delcyon.capo.xml.cdom.CDocument;
import com.delcyon.capo.xml.cdom.CElement;
import com.delcyon.capo.xml.cdom.VariableContainer;
import com.delcyon.capo.xml.dom.ResourceDeclarationElement;

/**
 * @author jeremiah TODO verify required parameters and values
 */
public abstract class AbstractResourceDescriptor implements ResourceDescriptor
{
    private HashMap<String, String> declaredParameterHashMap = new HashMap<String, String>(); // NOW eval context in ResourceElement
    private HashMap<String, String> delayedParameterHashMap = new HashMap<String, String>(); //
    private HashMap<String, String> contextParameterHashMap = new HashMap<String, String>();
    private HashMap<StreamType, State> streamStateHashMap = new HashMap<StreamType, State>();
    private ResourceURI resourceURI = null;
    private ResourceParameter[] initialResourceParameters = null;
    private HashMap<State, StateParameters> stateParametersHashMap = new HashMap<State, StateParameters>();
    private HashMap<String, ResourceDescriptor> childResourceDescriptorHashMap = new HashMap<String, ResourceDescriptor>();
    private List<ContentMetaData> childContentMetaDataList = null;
    private LifeCycle lifeCycle;
    private LifeCycle originallyDeclaredLifeCycle = null; //needed for re-initialization
    private State resourceState = State.NONE;
    private boolean isIterating = false;

    private ResourceType resourceType;
    private VariableContainer declaringVariableContainer;
    private OutputStreamTranslater outputStreamTranslater;

    private Vector<OutputStream> openOutputStreamVector = new Vector<OutputStream>();
    private Vector<InputStream> openInputStreamVector = new Vector<InputStream>();
    private String localName = null;
    private ResourceDeclarationElement declaringResourceElement;
    private ContentMetaData resourceMetaData;
    private transient ResourceDescriptor parentResourceDescriptor;

    @Override
    public void setup(ResourceType resourceType, String resourceURI) throws Exception
    {

        this.resourceState = State.NONE;
        this.resourceType = resourceType;
        this.resourceURI = new ResourceURI(resourceURI);
        this.lifeCycle = resourceType.getDefaultLifeCycle();

    }

    @Override
    public void init(ResourceDeclarationElement declaringResourceElement, VariableContainer variableContainer, LifeCycle lifeCycle, boolean iterate, ResourceParameter... resourceParameters) throws Exception
    {
        this.declaringResourceElement = declaringResourceElement;
        this.declaringVariableContainer = variableContainer;
        // override life cycle
        if (lifeCycle == null)
        {
            this.lifeCycle = resourceType.getDefaultLifeCycle();
        }
        else
        {
        	this.originallyDeclaredLifeCycle = lifeCycle;
        	this.lifeCycle = lifeCycle;
        }

        if (iterate == true)
        {
            isIterating = true;
        }

        // process resource parameters

        initialResourceParameters = resourceParameters;

        // store parameters from URI

        declaredParameterHashMap.putAll(resourceURI.getParameterMap());

        for (ResourceParameter resourceParameter : initialResourceParameters)
        {
            if (resourceParameter.getEvaluationContext() == EvaluationContext.NOW)
            {
                declaredParameterHashMap.put(resourceParameter.getName(), processVars(variableContainer, resourceParameter.getValue()));
            }
            else if (resourceParameter.getEvaluationContext() == EvaluationContext.DELAYED)
            {
                delayedParameterHashMap.put(resourceParameter.getName(), resourceParameter.getValue());
            }
        }
        this.resourceState = State.INITIALIZED;
        this.stateParametersHashMap.put(State.INITIALIZED, new StateParameters(resourceParameters, variableContainer));
    }

    @Override
    public void open(VariableContainer variableContainer, ResourceParameter... resourceParameters) throws Exception
    {
        if (resourceState == State.NONE)
        {
            init(null, variableContainer, null, false, resourceParameters);
        }

        if (resourceState == State.STEPPING)
        {
            return;
        }

        if (resourceState == State.OPEN)
        {
            refreshResourceMetaData(variableContainer, resourceParameters);
            clearContent();
            return;
        }

        addResourceParameters(variableContainer, resourceParameters);

        refreshResourceMetaData(variableContainer, resourceParameters);
        clearContent();
        this.resourceState = State.OPEN;
        this.stateParametersHashMap.put(State.OPEN, new StateParameters(resourceParameters, variableContainer));
    }

    protected abstract void clearContent() throws Exception;

    protected abstract ContentMetaData buildResourceMetaData(VariableContainer variableContainer, ResourceParameter... resourceParameters) throws Exception;

    protected void refreshResourceMetaData(VariableContainer variableContainer, ResourceParameter... resourceParameters) throws Exception
    {
        if (this.resourceMetaData != null && this.resourceMetaData instanceof SimpleContentMetaData == false)
        {
            this.resourceMetaData.refresh(resourceParameters);
        }
        else
        {
            this.resourceMetaData = buildResourceMetaData(variableContainer, resourceParameters);
        }

    }

    @Override
    public ContentMetaData getResourceMetaData(VariableContainer variableContainer, ResourceParameter... resourceParameters) throws Exception
    {
        advanceState(State.OPEN, variableContainer, resourceParameters);
        //System.out.println("hmmm");
//This currently seems unneeded and is about as expensive a call as possible esp when reading a file system         
//        if (resourceMetaData != null)
//        {
//            if (resourceMetaData.isDynamic())
//            {
//                refreshResourceMetaData(variableContainer, resourceParameters);
//            }
//        }
        if (resourceMetaData.isInitialized() == false)
        {
            resourceMetaData.init();
        }
        return resourceMetaData;
    }

    @Override
    public boolean isRemoteResource()
    {
        return false;
    }

    @Override
    public void close(VariableContainer variableContainer, ResourceParameter... resourceParameters) throws Exception
    {

        Vector<ResourceParameter> tempResourceParameterVector = new Vector<ResourceParameter>();

        for (ResourceParameter resourceParameter : resourceParameters)
        {
            if (resourceParameter.getSource() == Source.DECLARATION)
            {
                tempResourceParameterVector.add(resourceParameter);
            }
        }
        if (outputStreamTranslater != null)
        {
            outputStreamTranslater.close();
            outputStreamTranslater = null;
        }
        if (isIterating() == false)
        {
            resourceState = State.CLOSED;
            this.stateParametersHashMap.put(State.CLOSED, new StateParameters(resourceParameters, variableContainer));
        }

        for (InputStream inputStream : openInputStreamVector)
        {
            try
            {
                inputStream.close();
            }
            catch (Exception e)
            {
                CapoApplication.logger.log(Level.WARNING, "Error closing inputstream", e);
            }
        }

        openInputStreamVector.clear();

        for (OutputStream outputStream : openOutputStreamVector)
        {

            try
            {
                outputStream.flush();
                outputStream.close();
            }
            catch (Exception e)
            {
                CapoApplication.logger.log(Level.WARNING, "Error closing outputstream", e);
            }
        }

        openOutputStreamVector.clear();
    }

    // TODO
    /*
     * cleanup should remove call parameters depending on lifeCycle destroy may want to be called destroy should call close depending destroy should only be called when a resourceElement falls out of scope.
     */
    @Override
    public void release(VariableContainer variableContainer, ResourceParameter... resourceParameters) throws Exception
    {
        if (getResourceState().ordinal() >= State.OPEN.ordinal() && getResourceState().ordinal() < State.CLOSED.ordinal())
        {
            close(variableContainer, resourceParameters);
        }
        // TODO cleanup metadata
        childResourceDescriptorHashMap.clear();
        childContentMetaDataList = null;
        setResourceState(State.RELEASED);
        this.stateParametersHashMap.put(State.RELEASED, new StateParameters(resourceParameters, variableContainer));
    }

    @Override
    public void reset(State previousState) throws Exception
    {
        if (previousState.ordinal() > getResourceState().ordinal())
        {
            throw new Exception("Cannot reset a resource to a later state. Current State:" + getResourceState() + " Requested State:" + previousState);
        }

        // if we aren't already open, don't try, we're probably just trying to re-initialize
        if (getResourceState().ordinal() < State.OPEN.ordinal())
        {
            release(null);
        }

        if (previousState.ordinal() == getResourceState().ordinal())
        {
            nextState(null);
        }

        while (getResourceState() != previousState)
        {
            int nextState = getResourceState().ordinal() + 1;
            if (nextState >= State.values().length)
            {
                nextState = 0;
            }
            StateParameters nextStateParameters = stateParametersHashMap.get(State.values()[nextState]);
            if (nextStateParameters == null)
            {
                nextState(null);
            }
            else
            {
                nextState(nextStateParameters.getVariableContainer(), nextStateParameters.getResourceParameters());
            }
        }

    }

    /**
     * This is a utility method for subclases to move the resource forward to the desired state
     * 
     * @param desiredState
     * @param variableContainer
     * @param resourceParameters
     * @throws Exception
     */
    @Override
    public void advanceState(State desiredState, VariableContainer variableContainer, ResourceParameter... resourceParameters) throws Exception
    {
        while (getResourceState().ordinal() < desiredState.ordinal())
        {
            nextState(variableContainer, resourceParameters);
        }
    }

    /** This is just a simple mapping to allow us to jump to the next state. Maybe someday, we can put the proper method pointers in the enum declaration when available. */
    private void nextState(VariableContainer variableContainer, ResourceParameter... resourceParameters) throws Exception
    {
        if (getResourceState() == State.NONE)
        {
        	//if original is null, then init should just pick pick up the default
            init(null, variableContainer, originallyDeclaredLifeCycle, isIterating, resourceParameters);
        }
        else if (getResourceState() == State.INITIALIZED)
        {
            open(variableContainer, resourceParameters);
        }
        else if (getResourceState() == State.OPEN)
        {
            next(variableContainer, resourceParameters);
        }
        else if (getResourceState() == State.STEPPING)
        {
            close(variableContainer, resourceParameters);
        }
        else if (getResourceState() == State.CLOSED)
        {
            release(variableContainer, resourceParameters);
        }
        else if (getResourceState() == State.RELEASED)
        {
            setup(resourceType, resourceURI.getResourceURIString());
        }
    }

    protected ResourceDeclarationElement getDeclaringResourceElement()
    {
        return this.declaringResourceElement;
    }

    public ResourceURI getResourceURI()
    {
        return resourceURI;
    }

    protected void setResourceURI(ResourceURI resourceURI)
    {
        this.resourceURI = resourceURI;
    }

    /** returns last piece of URI **/
    @Override
    public String getLocalName()
    {
        if (localName != null)
        {
            return this.localName;
        }
        else
        {
            if(this.resourceURI.getPath().equals("/"))
            {
                return this.resourceURI.getPath();
            }
            else if(this.resourceURI.getPath().equals("///"))
            {
                return "/";
            }
            
            else
            {                
                String[] splitURI = this.resourceURI.getPath().split("/");
                return splitURI[splitURI.length - 1];
            }
        }
    }

    public void setLocalName(String localName)
    {
        this.localName = localName;
    }

    @Override
    public ResourceType getResourceType()
    {
        return this.resourceType;
    }

    protected void setResourceType(ResourceType resourceType)
    {
        this.resourceType = resourceType;
    }

    public VariableContainer getDeclaringVariableContainer()
    {
        return declaringVariableContainer;

    }

    @Override
    public void addResourceParameters(VariableContainer variableContainer, ResourceParameter... resourceParameters) throws Exception
    {
        for (ResourceParameter resourceParameter : resourceParameters)
        {
            contextParameterHashMap.put(resourceParameter.getName(), processVars(variableContainer, resourceParameter.getValue())); // this is not right
        }
    }

    public String processVars(VariableContainer variableContainer, String varString) throws Exception
    {
        StringBuffer stringBuffer = new StringBuffer(varString);
        processVars(variableContainer, stringBuffer);
        return stringBuffer.toString();
    }

    /**
     * Check String for variables and replace them with the value of the var
     * 
     * @param varStringBuffer
     */
    private void processVars(VariableContainer variableContainer, StringBuffer varStringBuffer) throws Exception
    {
        while (varStringBuffer != null && varStringBuffer.toString().matches(".*\\$\\{.+}.*"))
        {

            CapoServer.logger.log(Level.FINE, "found var in '" + varStringBuffer + "'");
            Stack<StringBuffer> stack = new Stack<StringBuffer>();
            StringBuffer currentStringBuffer = new StringBuffer();
            for (int index = 0; index < varStringBuffer.length(); index++)
            {

                if (varStringBuffer.charAt(index) == '$' && varStringBuffer.charAt(index + 1) == '{')
                {
                    stack.push(currentStringBuffer);
                    currentStringBuffer = new StringBuffer();
                    currentStringBuffer.append(varStringBuffer.charAt(index));
                }
                else if (varStringBuffer.charAt(index) == '}' && varStringBuffer.charAt(index - 1) != '\\' && stack.empty() == false)
                {
                    // pop, and evaluate
                    currentStringBuffer.append(varStringBuffer.charAt(index));
                    String varName = currentStringBuffer.toString().replaceFirst(".*\\$\\{(.+)}.*", "$1");
                    String value = getVarValue(variableContainer, varName);
                    if (value == null)
                    {
                        value = "";// TODO make this configurable to null,exception,or empty
                        CapoServer.logger.log(Level.WARNING, "var '" + varName + "' not found replaced with empty string");
                    }
                    currentStringBuffer = stack.pop();
                    currentStringBuffer.append(value);
                }
                else
                {
                    currentStringBuffer.append(varStringBuffer.charAt(index));
                }

            }

            varStringBuffer.replace(0, varStringBuffer.length(), currentStringBuffer.toString());

        }
        CapoServer.logger.log(Level.FINE, "final replacement =  '" + varStringBuffer + "'");

    }

    @SuppressWarnings("rawtypes")
    public String getVarValue(VariableContainer variableContainer, Enum varName) throws Exception
    {
        return getVarValue(variableContainer, varName.toString());
    }

    /**
     * check request check entries check variables
     * 
     * @param varName
     * @return
     */
    public String getVarValue(VariableContainer variableContainer, String varName) throws Exception
    {
        if (contextParameterHashMap.containsKey(varName))
        {
            return contextParameterHashMap.get(varName);
        }
        else if (delayedParameterHashMap.containsKey(varName))
        {
            return processVars(variableContainer, delayedParameterHashMap.get(varName));
        }
        else if (declaredParameterHashMap.containsKey(varName))
        {
            return declaredParameterHashMap.get(varName);
        }
        else if (variableContainer != null && variableContainer.getVarValue(varName) != null)
        {
            return variableContainer.getVarValue(varName);
        }
        else
        {
            return CapoApplication.getVariableValue(varName);
        }
    }

    // END PARAMETER PROCESSING

    @Override
    public LifeCycle getLifeCycle()
    {
        return this.lifeCycle;
    }

    @Override
    public State getStreamState(StreamType streamType)
    {

        if (streamStateHashMap.containsKey(streamType))
        {
            return streamStateHashMap.get(streamType);
        }
        else
        {
            return State.NONE;
        }
    }

    @Override
    public State getResourceState() throws Exception
    {
        return resourceState;
    }

    protected void setResourceState(State state)
    {
        this.resourceState = state;
    }

    @Deprecated
    // TODO remove, never used
    public void setStreamState(StreamType streamType, State state)
    {
        streamStateHashMap.put(streamType, state);
    }

    public boolean isIterating()
    {
        return isIterating;
    }

    @Override
    public String toString()
    {
        return ReflectionUtility.processToString(this);
    }

    @Override
    public boolean isSupportedStreamFormat(StreamType streamType, StreamFormat streamFormat) throws Exception
    {
        StreamType[] supporedtStreamTypes = getSupportedStreamTypes();
        for (StreamType supportedStreamType : supporedtStreamTypes)
        {
            if (supportedStreamType == streamType)
            {
                StreamFormat[] supportedStreamFormats = getSupportedStreamFormats(supportedStreamType);
                for (StreamFormat supportedSrteamFormat : supportedStreamFormats)
                {
                    if (supportedSrteamFormat == streamFormat)
                    {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public boolean isSupportedStreamType(StreamType streamType) throws Exception
    {
        StreamType[] supporedtStreamTypes = getSupportedStreamTypes();
        for (StreamType supportedStreamType : supporedtStreamTypes)
        {
            if (supportedStreamType == streamType)
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isSupportedAction(Action action)
    {
        Action[] supportedActions = getSupportedActions();
        return Arrays.asList(supportedActions).contains(action);

    }

    @Override
    public boolean performAction(VariableContainer variableContainer, Action action, ResourceParameter... resourceParameters) throws Exception
    {
        if (isSupportedAction(action) == false)
        {
            throw new UnsupportedOperationException();
        }
        else
        {
            return this.performAction(variableContainer, action, resourceParameters);
        }
    }

    protected abstract Action[] getSupportedActions();

    protected InputStream trackInputStream(InputStream inputStream)
    {
        openInputStreamVector.add(inputStream);
        return inputStream;
    }

    protected OutputStream trackOutputStream(OutputStream outputStream)
    {
        openOutputStreamVector.add(outputStream);
        return outputStream;
    }

    @Override
    // TODO
    public InputStream getInputStream(VariableContainer variableContainer, ResourceParameter... resourceParameters) throws Exception
    {
        advanceState(State.STEPPING, variableContainer, resourceParameters);
        StreamFormat streamFormat = getSupportedStreamFormats(StreamType.INPUT)[0];
        ByteArrayInputStream byteArrayInputStream = null;
        if (streamFormat == StreamFormat.XML_BLOCK)
        {
            Element dataElement = readXML(variableContainer, resourceParameters);
            Document tempDocument = CapoApplication.getDocumentBuilder().newDocument();
            tempDocument.appendChild(tempDocument.importNode(dataElement, true));
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            XPath.dumpNode(tempDocument.getDocumentElement(), byteArrayOutputStream);
            // XPath.dumpNode(tempDocument.getDocumentElement(), System.err);
            byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
        }
        else if (streamFormat == StreamFormat.PROCESS)
        {
            byteArrayInputStream = new ByteArrayInputStream(new byte[0]);
        }
        else if (streamFormat == StreamFormat.BLOCK)
        {
            byteArrayInputStream = new ByteArrayInputStream(readBlock(variableContainer, resourceParameters));
        }
        else
        {
            throw new IOException(this.getClass() + "doesn't support getOutputStream()");
        }
        return byteArrayInputStream;
    }

    @Override
    public OutputStream getOutputStream(VariableContainer variableContainer, ResourceParameter... resourceParameters) throws Exception
    {
        advanceState(State.OPEN, variableContainer, resourceParameters);
        StreamFormat streamFormat = getSupportedStreamFormats(StreamType.OUTPUT)[0];
        if (outputStreamTranslater != null)
        {
            return outputStreamTranslater.getOutputStream();
        }

        if (streamFormat == StreamFormat.XML_BLOCK)
        {
            outputStreamTranslater = new OutputStreamTranslater(variableContainer, this, StreamFormat.XML_BLOCK, resourceParameters);
            return outputStreamTranslater.getOutputStream();
        }
        else if (streamFormat == StreamFormat.BLOCK)
        {
            outputStreamTranslater = new OutputStreamTranslater(variableContainer, this, StreamFormat.BLOCK, resourceParameters);
            return outputStreamTranslater.getOutputStream();
        }
        else if (streamFormat == StreamFormat.PROCESS)
        {
            outputStreamTranslater = new OutputStreamTranslater(variableContainer, this, StreamFormat.PROCESS, resourceParameters);
            return outputStreamTranslater.getOutputStream();
        }
        else
        {
            throw new IOException(this.getClass() + "doesn't support getOutputStream()");
        }
    }

    @Override
    public CElement readXML(VariableContainer variableContainer, ResourceParameter... resourceParameters) throws Exception
    {
        advanceState(State.STEPPING, variableContainer, resourceParameters);
        CDocument document = (CDocument) CapoApplication.getDocumentBuilder().newDocument();
        CElement dataElement = (CElement) document.createElement("Data");
        document.appendChild(dataElement);
        StreamFormat streamFormat = getSupportedStreamFormats(StreamType.INPUT)[0];
        if (streamFormat == StreamFormat.BLOCK)
        {
            dataElement.setTextContent(new String(readBlock(variableContainer, resourceParameters)));
        }
        else if (streamFormat == StreamFormat.PROCESS)
        {
            // skip this as we just want to return an empty element I think
        }
        else if (streamFormat == StreamFormat.STREAM)
        {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            StreamUtil.readInputStreamIntoOutputStream(getInputStream(variableContainer, resourceParameters), byteArrayOutputStream);
            if (getContentMetaData(variableContainer, resourceParameters).getContentFormatType() == ContentFormatType.XML)
            {
                dataElement = (CElement) CapoApplication.getDocumentBuilder().parse(new ByteArrayInputStream(byteArrayOutputStream.toByteArray())).getDocumentElement();
            }
            else
            {
                dataElement.setTextContent(new String(byteArrayOutputStream.toByteArray()));
            }
        }
        else
        {
            throw new IOException(this.getClass() + "doesn't support getOutputStream()");
        }
        return dataElement;
    }

    @Override
    public void writeXML(VariableContainer variableContainer, CElement element, ResourceParameter... resourceParameters) throws Exception
    {
        advanceState(State.OPEN, variableContainer, resourceParameters);
        StreamFormat streamFormat = getSupportedStreamFormats(StreamType.OUTPUT)[0];
        if (streamFormat == StreamFormat.BLOCK)
        {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            XPath.dumpNode(element, byteArrayOutputStream);
            writeBlock(variableContainer, byteArrayOutputStream.toByteArray(), resourceParameters);
        }
        else if (streamFormat == StreamFormat.PROCESS)
        {
            processOutput(variableContainer, resourceParameters);
        }
        else if (streamFormat == StreamFormat.STREAM)
        {
            XPath.dumpNode(element, getOutputStream(variableContainer, resourceParameters));
        }
        else
        {
            throw new IOException(this.getClass() + "doesn't support getOutputStream()");
        }

    }

    @Override
    public byte[] readBlock(VariableContainer variableContainer, ResourceParameter... resourceParameters) throws Exception
    {
        advanceState(State.STEPPING, variableContainer, resourceParameters);
        StreamFormat streamFormat = getSupportedStreamFormats(StreamType.INPUT)[0];
        if (streamFormat == StreamFormat.XML_BLOCK)
        {
            Element dataElement = readXML(variableContainer, resourceParameters);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            XPath.dumpNode(dataElement, byteArrayOutputStream);
            return byteArrayOutputStream.toByteArray();
        }
        else if (streamFormat == StreamFormat.PROCESS)
        {
            return new byte[0];
        }
        else if (streamFormat == StreamFormat.STREAM)
        {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            StreamUtil.readInputStreamIntoOutputStream(getInputStream(variableContainer, resourceParameters), byteArrayOutputStream);
            return byteArrayOutputStream.toByteArray();
        }
        else
        {
            throw new IOException(this.getClass() + "doesn't support getOutputStream()");
        }

    }

    @Override
    public void writeBlock(VariableContainer variableContainer, byte[] block, ResourceParameter... resourceParameters) throws Exception
    {
        advanceState(State.OPEN, variableContainer, resourceParameters);
        StreamFormat streamFormat = getSupportedStreamFormats(StreamType.OUTPUT)[0];
        if (streamFormat == StreamFormat.XML_BLOCK)
        {
            Document document = CapoApplication.getDocumentBuilder().newDocument();
            CElement dataElement = (CElement) document.createElement("Data");
            document.appendChild(dataElement);
            dataElement.setTextContent(new String(block));
            writeXML(variableContainer, dataElement, resourceParameters);

        }
        else if (streamFormat == StreamFormat.PROCESS)
        {
            processOutput(variableContainer, resourceParameters);

        }
        else if (streamFormat == StreamFormat.STREAM)
        {
            OutputStream outputStream = getOutputStream(variableContainer, resourceParameters);
            outputStream.write(block);
            outputStream.close();
        }
        else
        {
            throw new IOException(this.getClass() + "doesn't support getOutputStream()");
        }

    }

    @Override
    public void processInput(VariableContainer variableContainer, ResourceParameter... resourceParameters) throws Exception
    {
        advanceState(State.OPEN, variableContainer, resourceParameters);
        StreamFormat streamFormat = getSupportedStreamFormats(StreamType.INPUT)[0];
        if (streamFormat == StreamFormat.XML_BLOCK)
        {
            readXML(variableContainer, resourceParameters);

        }
        else if (streamFormat == StreamFormat.BLOCK)
        {
            readBlock(variableContainer, resourceParameters);
        }
        else if (streamFormat == StreamFormat.STREAM)
        {
            InputStream inputStream = getInputStream(variableContainer, resourceParameters);
            StreamUtil.readInputStreamIntoOutputStream(inputStream, new NullOutputStream());
        }
        else
        {
            throw new IOException(this.getClass() + "doesn't support getOutputStream()");
        }

    }

    @Override
    public void processOutput(VariableContainer variableContainer, ResourceParameter... resourceParameters) throws Exception
    {
        advanceState(State.OPEN, variableContainer, resourceParameters);
        StreamFormat streamFormat = getSupportedStreamFormats(StreamType.OUTPUT)[0];
        if (streamFormat == StreamFormat.XML_BLOCK)
        {
            Document document = CapoApplication.getDocumentBuilder().newDocument();
            CElement dataElement = (CElement) document.createElement("Data");
            writeXML(variableContainer, dataElement, resourceParameters);

        }
        else if (streamFormat == StreamFormat.BLOCK)
        {
            writeBlock(variableContainer, new byte[0], resourceParameters);
        }
        else if (streamFormat == StreamFormat.STREAM)
        {
            OutputStream outputStream = getOutputStream(variableContainer, resourceParameters);
            outputStream.write(new byte[0]);
            outputStream.close();
        }
        else
        {
            throw new IOException(this.getClass() + "doesn't support getOutputStream()");
        }

    }

    public ResourceDescriptor getParentResourceDescriptor()
    {
        return this.parentResourceDescriptor;
    }

    @Override
    public ResourceDescriptor getChildResourceDescriptor(ControlElement callingControlElement, String relativeURI) throws Exception
    {
        advanceState(State.OPEN, null);
        ContentMetaData contentMetaData = getResourceMetaData(null);
        if (contentMetaData.isContainer() == true)
        {
            //getResourceMetaData(null).getContainedResources();
            if(childContentMetaDataList == null)
            {
            	childContentMetaDataList = getResourceMetaData(null).getContainedResources();
            }
            for (ContentMetaData childContentMetaData : childContentMetaDataList)
            {
                if (childContentMetaData.getResourceURI().getBaseURI().endsWith(relativeURI))
                {
                    if (callingControlElement == null)
                    {
                        if (childResourceDescriptorHashMap.containsKey(childContentMetaData.getResourceURI().getBaseURI()))
                        {   
                            ResourceDescriptor childResourceDescriptor = childResourceDescriptorHashMap.get(childContentMetaData.getResourceURI().getBaseURI());
                            if(childResourceDescriptor.getResourceState() != State.RELEASED)
                            {
                                return childResourceDescriptor;
                            }
                        }
                        ResourceDescriptor childResourceDescriptor = CapoApplication.getDataManager().getResourceDescriptor(callingControlElement, childContentMetaData.getResourceURI().getBaseURI());
                        
                        if (childResourceDescriptor != null)
                        {
                            childResourceDescriptor.setParentResourceDescriptor(this);
                            //TODO Hopefully this won't bite us in the butt, but keeps the number of calls way down
                            if(childResourceDescriptor instanceof AbstractResourceDescriptor)
                            {
                                ((AbstractResourceDescriptor)childResourceDescriptor).resourceMetaData = childContentMetaData;
                            }
                        }
                        childResourceDescriptorHashMap.put(childContentMetaData.getResourceURI().getBaseURI(), childResourceDescriptor);
                        return childResourceDescriptor;
                    }
                    else
                    {
                        if (childResourceDescriptorHashMap.containsKey(childContentMetaData.getResourceURI().getBaseURI()))
                        {
                            ResourceDescriptor childResourceDescriptor = childResourceDescriptorHashMap.get(childContentMetaData.getResourceURI().getBaseURI());
                            if(childResourceDescriptor.getResourceState() != State.RELEASED)
                            {
                                return childResourceDescriptor;
                            }
                        }
                        ResourceDescriptor childResourceDescriptor = callingControlElement.getParentGroup().getResourceDescriptor(callingControlElement, childContentMetaData.getResourceURI().getBaseURI());
                        if (childResourceDescriptor != null)
                        {
                            childResourceDescriptor.setParentResourceDescriptor(this);
                          //TODO Hopefully this won't bite us in the butt, but keeps the number of calls way down
                            if(childResourceDescriptor instanceof AbstractResourceDescriptor)
                            {
                                ((AbstractResourceDescriptor)childResourceDescriptor).resourceMetaData = childContentMetaData;
                            }
                        }
                        childResourceDescriptorHashMap.put(childContentMetaData.getResourceURI().getBaseURI(), childResourceDescriptor);
                        return childResourceDescriptor;
                    }
                }
            }
            // return CapoApplication.getDataManager().getResourceDescriptor(callingControlElement, getResourceURI()+(relativeURI.startsWith("/") ? relativeURI : "/"+relativeURI));
            String path = getResourceURI().getBaseURI();
        	if (path.endsWith("/"))
        	{
        		path = path.substring(0, path.length()-1);
        	}
        	path = path + (relativeURI.startsWith("/") ? relativeURI : "/" + relativeURI);
            if (callingControlElement == null)
            {
            	
                if (childResourceDescriptorHashMap.containsKey(path))
                {
                    ResourceDescriptor childResourceDescriptor = childResourceDescriptorHashMap.get(path);
                    if(childResourceDescriptor.getResourceState() != State.RELEASED)
                    {
                        return childResourceDescriptor;
                    }
                }
                ResourceDescriptor childResourceDescriptor = CapoApplication.getDataManager().getResourceDescriptor(callingControlElement, path);
                if (childResourceDescriptor != null)
                {
                    childResourceDescriptor.setParentResourceDescriptor(this);
                }
                childResourceDescriptorHashMap.put(path, childResourceDescriptor);
                return childResourceDescriptor;
            }
            else
            {

                if (childResourceDescriptorHashMap.containsKey(path))
                {
                    ResourceDescriptor childResourceDescriptor =  childResourceDescriptorHashMap.get(path);
                    if(childResourceDescriptor.getResourceState() != State.RELEASED)
                    {
                        return childResourceDescriptor;
                    }
                }
                ResourceDescriptor childResourceDescriptor = callingControlElement.getParentGroup().getResourceDescriptor(callingControlElement, path);
                if (childResourceDescriptor != null)
                {
                    childResourceDescriptor.setParentResourceDescriptor(this);
                }
                childResourceDescriptorHashMap.put(path, childResourceDescriptor);
                return childResourceDescriptor;
            }
        }
        else
        {
            return null;
        }
    }

    @Override
    public void setParentResourceDescriptor(ResourceDescriptor parentResourceDescriptor) throws Exception
    {
        this.parentResourceDescriptor = parentResourceDescriptor;
    }

    private class OutputStreamTranslater implements StreamEventListener
    {

        private ByteArrayOutputStream byteArrayOutputStream;
        private StreamEventFilterOutputStream streamEventFilterOutputStream;
        private StreamFormat streamFormat;
        private ResourceDescriptor resourceDescriptor;
        private ResourceParameter[] resourceParameters;
        private VariableContainer variableContainer;

        public OutputStreamTranslater(VariableContainer variableContainer, ResourceDescriptor resourceDescriptor, StreamFormat streamFormat, ResourceParameter... resourceParameters)
        {
            byteArrayOutputStream = new ByteArrayOutputStream();
            streamEventFilterOutputStream = new StreamEventFilterOutputStream(byteArrayOutputStream);
            streamEventFilterOutputStream.addStreamEventListener(this);
            this.streamFormat = streamFormat;
            this.resourceDescriptor = resourceDescriptor;
            this.resourceParameters = resourceParameters;
            this.variableContainer = variableContainer;
        }

        public OutputStream getOutputStream()
        {
            return streamEventFilterOutputStream;
        }

        public void close()
        {
            byteArrayOutputStream.reset();
            streamEventFilterOutputStream.removeStreamEventListener(this);
        }

        @Override
        public void processStreamEvent(StreamEvent streamEvent) throws IOException
        {
            if (streamEvent == StreamEvent.CLOSED)
            {
                CapoApplication.logger.fine(new String(byteArrayOutputStream.toByteArray()));
                // don't write empty blocks
                if (byteArrayOutputStream.size() == 0 && streamFormat != StreamFormat.PROCESS)
                {
                    return;
                }
                try
                {
                    if (streamFormat == StreamFormat.XML_BLOCK)
                    {
                        Document document = CapoApplication.getDocumentBuilder().parse(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()));
                        resourceDescriptor.writeXML(variableContainer, (CElement) document.getDocumentElement(), resourceParameters);
                    }
                    else if (streamFormat == StreamFormat.BLOCK)
                    {
                        resourceDescriptor.writeBlock(variableContainer, byteArrayOutputStream.toByteArray(), resourceParameters);
                    }
                    else if (streamFormat == StreamFormat.PROCESS)
                    {
                        resourceDescriptor.processOutput(variableContainer, resourceParameters);
                    }
                    else if (streamFormat == StreamFormat.STREAM)
                    {
                        OutputStream outputStream = resourceDescriptor.getOutputStream(variableContainer, resourceParameters);
                        outputStream.write(byteArrayOutputStream.toByteArray());
                        outputStream.close();
                    }
                    byteArrayOutputStream.reset();
                }
                catch (Exception exception)
                {
                    throw new IOException(exception);
                }
            }

        }
    }

}
