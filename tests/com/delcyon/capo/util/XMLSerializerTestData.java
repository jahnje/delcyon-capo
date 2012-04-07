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
package com.delcyon.capo.util;

import java.util.HashMap;
import java.util.Vector;

/**
 * @author jeremiah
 *
 */
@SuppressWarnings("unused")
public class XMLSerializerTestData implements TestInterface
{
	private int primInt = 0;
	private Integer objInt = null;
	private Integer[] objIntArray = null;
	
	private float primFloat = 0;
	private Float objFloat = null;
	private Float[] objFloatArray = null;
	
	private double primDouble = 0;
	private Double objDouble = null;
	private Double[] objDoubleArray = null;
	
	private long primLong = 0;
	private Long objLong = null;
	private Long[] objLongArray = null;
	
	private short primShort = 0;
	private Short objShort = null;
	private Short[] objShortArray = null;
	
	private byte primByte = 0;
	private Byte objByte = null;
	private Byte[] objByteArray = null;
	
	private boolean primBoolean = true;
	private Boolean objBoolean = null;	
	private Boolean[] objBooleanArray = null;
	
	
	
	private String objString = null;	
	private String[] objStringArray = null;
	
	private XMLSerializerTestData xmlSerializerTestData = null;
	
	private Vector<TestInterface> xmlSerializerTestDataVector = null;
	
	private TestInterface[] xmlSerializerTestDatasArray = null;
	
	private HashMap<String, TestInterface> xmlSerializerTestDataHashMap = null;
	
	private XMLSerializerTestData()
	{
		
	}
	
	
	
	
	public HashMap<String, TestInterface> getXmlSerializerTestDataHashMap()
	{
		return xmlSerializerTestDataHashMap;
	}




	public void setXmlSerializerTestDataHashMap(HashMap<String, TestInterface> xmlSerializerTestDataHashMap)
	{
		this.xmlSerializerTestDataHashMap = xmlSerializerTestDataHashMap;
	}




	public XMLSerializerTestData(XMLSerializerTestData xmlSerializerTestData)
	{
		this.xmlSerializerTestData = xmlSerializerTestData;
	}

	public void setXmlSerializerTestData(XMLSerializerTestData xmlSerializerTestData)
	{
		this.xmlSerializerTestData = xmlSerializerTestData;
	}
		
	
	public TestInterface[] getXmlSerializerTestDatasArray()
	{
		return xmlSerializerTestDatasArray;
	}

	public void setXmlSerializerTestDatasArray(TestInterface[] xmlSerializerTestDatasArray)
	{
		this.xmlSerializerTestDatasArray = xmlSerializerTestDatasArray;
	}

	public Vector<TestInterface> getXmlSerializerTestDataVector()
	{
		return xmlSerializerTestDataVector;
	}

	public void setXmlSerializerTestDataVector(Vector<TestInterface> xmlSerializerTestDatas)
	{
		this.xmlSerializerTestDataVector = xmlSerializerTestDatas;
	}

	public int getPrimInt()
	{
		return primInt;
	}

	public void setPrimInt(int primInt)
	{
		this.primInt = primInt;
	}

	public Integer getObjInt()
	{
		return objInt;
	}

	public void setObjInt(Integer objInt)
	{
		this.objInt = objInt;
	}

	public Integer[] getObjIntArray()
	{
		return objIntArray;
	}

	public void setObjIntArray(Integer[] objIntArray)
	{
		this.objIntArray = objIntArray;
	}

	public float getPrimFloat()
	{
		return primFloat;
	}

	public void setPrimFloat(float primFloat)
	{
		this.primFloat = primFloat;
	}

	public Float getObjFloat()
	{
		return objFloat;
	}

	public void setObjFloat(Float objFloat)
	{
		this.objFloat = objFloat;
	}

	public Float[] getObjFloatArray()
	{
		return objFloatArray;
	}

	public void setObjFloatArray(Float[] objFloatArray)
	{
		this.objFloatArray = objFloatArray;
	}

	public double getPrimDouble()
	{
		return primDouble;
	}

	public void setPrimDouble(double primDouble)
	{
		this.primDouble = primDouble;
	}

	public Double getObjDouble()
	{
		return objDouble;
	}

	public void setObjDouble(Double objDouble)
	{
		this.objDouble = objDouble;
	}

	public Double[] getObjDoubleArray()
	{
		return objDoubleArray;
	}

	public void setObjDoubleArray(Double[] objDoubleArray)
	{
		this.objDoubleArray = objDoubleArray;
	}

	public long getPrimLong()
	{
		return primLong;
	}

	public void setPrimLong(long primLong)
	{
		this.primLong = primLong;
	}

	public Long getObjLong()
	{
		return objLong;
	}

	public void setObjLong(Long objLong)
	{
		this.objLong = objLong;
	}

	public Long[] getObjLongArray()
	{
		return objLongArray;
	}

	public void setObjLongArray(Long[] objLongArray)
	{
		this.objLongArray = objLongArray;
	}

	public short getPrimShort()
	{
		return primShort;
	}

	public void setPrimShort(short primShort)
	{
		this.primShort = primShort;
	}

	public Short getObjShort()
	{
		return objShort;
	}

	public void setObjShort(Short objShort)
	{
		this.objShort = objShort;
	}

	public Short[] getObjShortArray()
	{
		return objShortArray;
	}

	public void setObjShortArray(Short[] objShortArray)
	{
		this.objShortArray = objShortArray;
	}

	public byte getPrimByte()
	{
		return primByte;
	}

	public void setPrimByte(byte primByte)
	{
		this.primByte = primByte;
	}

	public Byte getObjByte()
	{
		return objByte;
	}

	public void setObjByte(Byte objByte)
	{
		this.objByte = objByte;
	}

	public Byte[] getObjByteArray()
	{
		return objByteArray;
	}

	public void setObjByteArray(Byte[] objByteArray)
	{
		this.objByteArray = objByteArray;
	}

	public boolean isPrimBoolean()
	{
		return primBoolean;
	}

	public void setPrimBoolean(boolean primBoolean)
	{
		this.primBoolean = primBoolean;
	}

	public Boolean getObjBoolean()
	{
		return objBoolean;
	}

	public void setObjBoolean(Boolean objBoolean)
	{
		this.objBoolean = objBoolean;
	}

	public Boolean[] getObjBooleanArray()
	{
		return objBooleanArray;
	}

	public void setObjBooleanArray(Boolean[] objBooleanArray)
	{
		this.objBooleanArray = objBooleanArray;
	}

	public String getObjString()
	{
		return objString;
	}

	public void setObjString(String objString)
	{
		this.objString = objString;
	}

	public String[] getObjStringArray()
	{
		return objStringArray;
	}

	public void setObjStringArray(String[] objStringArray)
	{
		this.objStringArray = objStringArray;
	}

	public XMLSerializerTestData getXmlSerializerTestData()
	{
		return xmlSerializerTestData;
	}

	@Override
	public String toString()
	{		
		return ReflectionUtility.processToString(this,"\n");
	}
}
