package com.delcyon.capo.xml.cdom;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import org.junit.Assert;
import org.junit.Test;

import com.google.gson.JsonObject;

/**
 * FB24788: JSON through CDocument and back keeps a string that looks like a number with leading zeros (AOIC's county FIPS
 * "001") a string - JSON can't write a number that way - while real numbers stay numbers.
 */
public class CDocumentJSONTest
{
	@Test
	public void leadingZeroStringsStayStrings() throws Exception
	{
		String json = "{\"supervisingcountyfips\":{\"type\":[\"string\",\"null\"],\"enum\":[\"001\",\"033\",\"101\",null],\"maxLength\":3},"
			+ "\"amounts\":{\"enum\":[0,7,1.5,-2]},\"code\":\"007\",\"count\":12,\"rate\":0.25,\"flag\":true}";
		CDocument document = CDocumentJSON.convert(new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)), "root");
		JsonObject back = CDocumentJSON.convert(document);

		Assert.assertEquals("[\"001\",\"033\",\"101\",\"null\"]", back.getAsJsonObject("supervisingcountyfips").get("enum").toString());
		Assert.assertEquals("\"007\"", back.get("code").toString());
		Assert.assertEquals("3", back.getAsJsonObject("supervisingcountyfips").get("maxLength").toString());
		Assert.assertEquals("[0,7,1.5,-2]", back.getAsJsonObject("amounts").get("enum").toString());
		Assert.assertEquals("12", back.get("count").toString());
		Assert.assertEquals("0.25", back.get("rate").toString());
		Assert.assertEquals("true", back.get("flag").toString());
	}
}
