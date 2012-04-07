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
package com.delcyon.capo.xml;

import java.io.OutputStream;
import java.util.HashMap;

import org.w3c.dom.Document;


/**
 * @author jeremiah
 *
 */
public interface XMLProcessor
{
	public void init(Document document, XMLStreamProcessor xmlStreamProcessor, OutputStream outputStream,HashMap<String, String> sessionHashMap) throws Exception;
	public void process() throws Exception;
	public Document getDocument();
	public XMLStreamProcessor getXmlStreamProcessor();
	public OutputStream getOutputStream();
	public Document readNextDocument() throws Exception;
	public void writeDocument(Document document) throws Exception;
}
