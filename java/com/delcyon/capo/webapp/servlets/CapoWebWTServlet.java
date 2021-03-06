/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package com.delcyon.capo.webapp.servlets;

import eu.webtoolkit.jwt.WApplication;
import eu.webtoolkit.jwt.WEnvironment;
import eu.webtoolkit.jwt.WtServlet;

public class CapoWebWTServlet extends WtServlet {
    private static final long serialVersionUID = 1L;

    @Override
    public WApplication createApplication(WEnvironment env) {
        return new CapoWebApplication(env, false);
    }
}
