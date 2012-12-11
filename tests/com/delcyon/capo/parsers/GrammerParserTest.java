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
package com.delcyon.capo.parsers;

import java.io.FileInputStream;
import java.util.Vector;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author jeremiah
 *
 */
public class GrammerParserTest
{

    @Test
    public void test() throws Exception
    {
        GrammerParser grammerParser = new GrammerParser();
        grammerParser.loadNotationGrammer(new FileInputStream("test-data/parser_test_data/SIMPLE.notation"));       
        grammerParser.loadGrammer(new FileInputStream("test-data/parser_test_data/SIMPLE.grammer"));
        grammerParser.parse(new FileInputStream("test-data/parser_test_data/SIMPLE.input"));
    }

}
