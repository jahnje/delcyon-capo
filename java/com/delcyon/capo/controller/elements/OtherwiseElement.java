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

import com.delcyon.capo.controller.AbstractControl;
import com.delcyon.capo.controller.ControlElementProvider;

/**
 * @author jeremiah
 *
 */
@ControlElementProvider(name="otherwise")
public class OtherwiseElement extends AbstractControl
{
	
	@SuppressWarnings("unchecked")
	@Override
	public Enum[] getAttributes()
	{
		return new Enum[]{};
	}

	@SuppressWarnings("unchecked")
	@Override
	public Enum[] getRequiredAttributes()
	{
		return new Enum[]{};
	}

	@Override
	public String[] getSupportedNamespaces()
	{
		return null;
	}


	@Override
	public Object processServerSideElement() throws Exception
	{
		processChildren(getControlElementDeclaration().getChildNodes(), getParentGroup(), this, getControllerClientRequestProcessor());
		return null;
	}

	
}
