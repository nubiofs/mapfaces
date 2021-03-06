/*
 *    Mapfaces -
 *    http://www.mapfaces.org
 *
 *    (C) 2007 - 2008, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 3 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.mapfaces.component.treelayout;

import org.mapfaces.component.abstractTree.UITreeLinesBase;

/**
 * @author Kevin Delfour (Geomatys)
 */
public class UITreeLines extends UITreeLinesBase {

    private static final String RENDERER_TYPE = "org.mapfaces.renderkit.treelayout.HTMLTreeLines";
    private static final String FAMILY = "org.mapfaces.treelayout.TreeLines";

    private String varCompId;

    /**
     * {@inheritDoc }
     */
    @Override
    public String getFamily() {
        return FAMILY;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String getRendererType() {
        return RENDERER_TYPE;
    }

    public String getVarCompId() {
        return varCompId;
    }

    public void setVarCompId(final String varCompId) {
        this.varCompId = varCompId;
    }
}
