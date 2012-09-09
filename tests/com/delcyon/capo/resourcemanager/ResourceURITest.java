/**
Copyright (c) 2012 Delcyon, Inc.
This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package com.delcyon.capo.resourcemanager;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author jeremiah
 *
 */
public class ResourceURITest
{

	private String testURI = "jdbc:hsqldb:file:testdb/testdb?user=user\\?&password&\\&\\!systems=\\=!systems?id=:242";
	private String opaqueTestURI = "scheme:/schemespecific";
	private String testURL = "foo://username:password@example.com:8042/over/there/index.dtb?type=animal&name=narwhal#nose";
	private String testURNPath = "urn:example:animal:ferret:nose";
	private String testMailToURN = "mailto:username@example.com?subject=Topic";
	/**
	 * Test method for {@link com.delcyon.capo.resourcemanager.ResourceURI#ResourceURI(java.lang.String)}.
	 */
	@Test
	public void testResourceURI()
	{
		ResourceURI resourceURI = new ResourceURI(testURI);
	}

	/**
	 * Test method for {@link com.delcyon.capo.resourcemanager.ResourceURI#getScheme(java.lang.String)}.
	 */
	@Test
	public void testGetScheme()
	{
		Assert.assertEquals("jdbc",ResourceURI.getScheme(testURI));
		Assert.assertEquals("foo",ResourceURI.getScheme(testURL));
		Assert.assertEquals("scheme",ResourceURI.getScheme(opaqueTestURI));
	}

	/**
	 * Test method for {@link com.delcyon.capo.resourcemanager.ResourceURI#getSchemeSpecificPart(java.lang.String)}.
	 */
	@Test
	public void testGetSchemeSpecificPart()
	{
		Assert.assertEquals("hsqldb:file:testdb/testdb?user=user\\?&password&\\&\\!systems=\\=!systems?id=:242",ResourceURI.getSchemeSpecificPart(testURI));
		Assert.assertEquals("//username:password@example.com:8042/over/there/index.dtb?type=animal&name=narwhal#nose",ResourceURI.getSchemeSpecificPart(testURL));
	}

	/**
	 * Test method for {@link com.delcyon.capo.resourcemanager.ResourceURI#isOpaque(java.lang.String)}.
	 */
	@Test
	public void testIsOpaque()
	{
		Assert.assertEquals(true,ResourceURI.isOpaque(testURI));
		Assert.assertEquals(false,ResourceURI.isOpaque(opaqueTestURI));
		Assert.assertEquals(false,ResourceURI.isOpaque(testURL));
	}

	/**
	 * Test method for {@link com.delcyon.capo.resourcemanager.ResourceURI#getAuthroity(java.lang.String)}.
	 */
	@Test
	public void testGetAuthroity()
	{
		Assert.assertEquals("username:password@example.com:8042",ResourceURI.getAuthroity(testURL));
		Assert.assertEquals(null,ResourceURI.getAuthroity(testMailToURN));
		Assert.assertEquals("hsqldb:file",ResourceURI.getAuthroity(testURI));
		Assert.assertEquals(null,ResourceURI.getAuthroity(testURNPath));
	}

	@Test
	public void testGetUserInfo()
	{
		Assert.assertEquals("username:password",ResourceURI.getUserInfo(testURL));
	}
	
	@Test
	public void testGetHostname()
	{
		Assert.assertEquals("example.com",ResourceURI.getHostname(testURL));
		Assert.assertEquals(null,ResourceURI.getHostname(testURI));
		Assert.assertEquals(null,ResourceURI.getHostname(opaqueTestURI));
	}
	
	@Test
	public void testGetPort()
	{
		Assert.assertEquals(8042l,(long)ResourceURI.getPort(testURL));
		Assert.assertEquals(null,ResourceURI.getPort(testURI));
		Assert.assertEquals(null,ResourceURI.getPort(opaqueTestURI));
	}
	
	@Test
	public void testGetBaseURI()
	{		
		Assert.assertEquals("foo://username:password@example.com:8042/over/there/index.dtb?type=animal&name=narwhal#nose",ResourceURI.getBaseURI(testURL));
		Assert.assertEquals("jdbc:hsqldb:file:testdb/testdb?user=user\\?&password&\\&!systems=\\=",ResourceURI.getBaseURI(testURI));
		Assert.assertEquals("scheme:/schemespecific",ResourceURI.getBaseURI(opaqueTestURI));
	}
	
	@Test
	public void testGetHierarchy()
	{
		Assert.assertEquals("username:password@example.com:8042/over/there/index.dtb",ResourceURI.getHierarchy(testURL));
		Assert.assertEquals("hsqldb:file:testdb/testdb",ResourceURI.getHierarchy(testURI));
		Assert.assertEquals("/schemespecific",ResourceURI.getHierarchy(opaqueTestURI));
		Assert.assertEquals("example:animal:ferret:nose",ResourceURI.getHierarchy(testURNPath));
		Assert.assertEquals(null,ResourceURI.getHierarchy(testMailToURN));

	}
	
	/**
	 * Test method for {@link com.delcyon.capo.resourcemanager.ResourceURI#removeURN(java.lang.String)}.
	 */
	@Test
	public void testRemoveURN()
	{
		Assert.assertEquals("//username:password@example.com:8042/over/there/index.dtb?type=animal&name=narwhal#nose",ResourceURI.removeURN(testURL));
		Assert.assertEquals("testdb/testdb?user=user\\?&password&\\&\\!systems=\\=!systems?id=:242",ResourceURI.removeURN(testURI));
		Assert.assertEquals("/schemespecific",ResourceURI.removeURN(opaqueTestURI));
	}

}
