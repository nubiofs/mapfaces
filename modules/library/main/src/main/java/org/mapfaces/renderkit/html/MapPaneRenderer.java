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
package org.mapfaces.renderkit.html;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import org.ajax4jsf.framework.renderer.RendererUtils.HTML;
import org.geotoolkit.map.MapContext;
import org.mapfaces.component.UILayer;
import org.mapfaces.component.UIMapPane;
import org.mapfaces.component.UIWidgetBase;
import org.mapfaces.component.layer.UIFeatureLayer;
import org.mapfaces.component.layer.UIMapContextLayer;
import org.mapfaces.component.layer.UIWmsLayer;
import org.mapfaces.component.models.UIContext;
import org.mapfaces.models.AbstractModelBase;
import org.mapfaces.models.Context;
import org.mapfaces.models.Layer;
import org.mapfaces.models.layer.DefaultMapContextLayer;
import org.mapfaces.models.layer.FeatureLayer;
import org.mapfaces.models.layer.MapContextLayer;
import org.mapfaces.models.layer.WmsLayer;
import org.mapfaces.util.ContextFactory;
import org.mapfaces.util.DefaultContextFactory;
import org.mapfaces.util.FacesUtils;
import org.mapfaces.util.MapUtils;

/**
 * @author Olivier Terral (Geomatys).
 * @author Mehdi Sidhoum (Geomatys).
 */
public class MapPaneRenderer extends WidgetBaseRenderer {

    private static final Logger LOGGER = Logger.getLogger(WidgetBaseRenderer.class.getName());

    /**
     * {@inheritDoc }
     */
    @Override
    public void encodeBegin(final FacesContext context, final UIComponent component) throws IOException {
        super.encodeBegin(context, component);
        final UIMapPane comp = (UIMapPane) component;
        if (this.debug) {
            LOGGER.log(Level.INFO, "[DEBUG] MapPaneRenderer ENCOD BEGIN");
        }
        final String clientId = comp.getClientId(context);
        final Context model;
        if (comp.getModel() instanceof Context) {
            model = (Context) comp.getModel();
        } else {
            //The model context is null or not a Context instance
            throw new UnsupportedOperationException("The model context is null or not supported yet !");
        }

        String style = getStyle();
        final String styleClass = getStyleClass();
        final ResponseWriter writer = context.getResponseWriter();

        final String height = model.getWindowHeight();
        final String width = model.getWindowWidth();

        if (comp.getMaxExtent() == null) {
            comp.setMaxExtent(model.getMinx() + "," + model.getMiny() + "," + model.getMaxx() + "," + model.getMaxy());
        }

        final String id = (String) comp.getAttributes().get(HTML.id_ATTRIBUTE);
        if (style == null) {
            style = "width:" + width + "px;height:" + height + "px;z-index:0;";
        } else {
            style = "width:" + width + "px;height:" + height + "px;z-index:0;" + style;
        }

        if (this.debug) {
            LOGGER.log(Level.INFO, "\t the style property of the MapPane is " + style);
        }
        writer.startElement(HTML.DIV_ELEM, comp);
        writer.writeAttribute(HTML.id_ATTRIBUTE, clientId, HTML.id_ATTRIBUTE);
        if (styleClass == null) {
            writer.writeAttribute(HTML.class_ATTRIBUTE, "mfMapPane", "styleClass");
        } else {
            writer.writeAttribute(HTML.class_ATTRIBUTE, styleClass, "styleClass");
        }
        if (style != null) {
            writer.writeAttribute(HTML.style_ATTRIBUTE, style, HTML.style_ATTRIBUTE);
        /*MapPane ViewPort div*/
        }
        writer.startElement(HTML.DIV_ELEM, comp);

        if (id != null) {
            writer.writeAttribute(HTML.id_ATTRIBUTE, id + "_MapFaces_Viewport", HTML.id_ATTRIBUTE);
        } else {
            writer.writeAttribute(HTML.id_ATTRIBUTE, clientId + "_MapFaces_Viewport", HTML.id_ATTRIBUTE);
        }
        writer.writeAttribute(HTML.style_ATTRIBUTE, "overflow: hidden;position:relative;width:100%;height:100%;", HTML.style_ATTRIBUTE);
        writer.writeAttribute(HTML.class_ATTRIBUTE, "mfMapViewport", "styleClass");

        /*Layers Container div*/
        writer.startElement(HTML.DIV_ELEM, comp);

        if (id != null) {
            writer.writeAttribute(HTML.id_ATTRIBUTE, id + "_MapFaces_Container", HTML.id_ATTRIBUTE);
        } else {
            writer.writeAttribute(HTML.id_ATTRIBUTE, clientId + "_MapFaces_Container", HTML.id_ATTRIBUTE);
        }
        writer.writeAttribute(HTML.style_ATTRIBUTE, "top:0px;left:0px;position:absolute;z-index: 0;", HTML.style_ATTRIBUTE);

        final MapContext mapcontext = (MapContext) comp.getValue();
        if (mapcontext != null) {
            //adding all the MapContext layers  into an allInOne layer.
            final ContextFactory contextFactory = new DefaultContextFactory();
            model.clearMapContextLayers();
            final DefaultMapContextLayer mcLayer = (DefaultMapContextLayer) contextFactory.createDefaultMapContextLayer(FacesUtils.getNewIndex(model));
            mcLayer.setMapContext(mapcontext);
            model.addLayer((MapContextLayer) mcLayer);
        }

        final List<Layer> layers = model.getLayers();

        comp.setAjaxCompId(FacesUtils.getParentUIModelBase(context, component).getAjaxCompId());

        removeChildren(context, component);

        if (this.debug) {
            LOGGER.log(Level.INFO, "[DEBUG] The context of the Mappane contains " + layers.size() + " layers.");
        }

        for (final Layer temp : layers) {
            if (this.debug) {
                LOGGER.log(Level.INFO, "[DEBUG] The current layer is a :  " + temp.getType() + " layer.");
            }
            if (temp != null && temp.getType() != null) {
                switch (temp.getType()) {
                    case WMS:
                        final UIWmsLayer layer = new UIWmsLayer();
                        layer.setModel((AbstractModelBase) model);
                        if (temp.getId() != null) {
                            layer.getAttributes().put(HTML.id_ATTRIBUTE, FacesUtils.getParentUIModelBase(context, component).getId() + "_" + comp.getId() + "_" + temp.getId());
                        } else {
                            temp.setId(layer.getId());
                        }
                        comp.getChildren().add(layer);
                        temp.setCompId(layer.getClientId(context));
                        layer.setLayer((WmsLayer) temp);
                        break;
                    case MAPCONTEXT:
                        final MapContextLayer tmpMContext = (MapContextLayer) temp;
                        final UIMapContextLayer uiMCLayer = new UIMapContextLayer();
                        uiMCLayer.setModel((AbstractModelBase) model);
                        uiMCLayer.getAttributes().put(HTML.id_ATTRIBUTE, FacesUtils.getParentUIModelBase(context, component).getId() + "_" + comp.getId() + "_" + temp.getId());
                        tmpMContext.setCompId(uiMCLayer.getClientId(context));
                        uiMCLayer.setLayer(tmpMContext);
                        if (tmpMContext.getMapContext() != null) {
                            uiMCLayer.setValue(tmpMContext.getMapContext());
                        }
                        comp.getChildren().add(uiMCLayer);
                        break;
                    case FEATURE:
                        final FeatureLayer tmpfeature = (FeatureLayer) temp;
                        final UIFeatureLayer uiFLayer = new UIFeatureLayer();
                        uiFLayer.setModel((AbstractModelBase) model);
                        uiFLayer.setImage(tmpfeature.getImage());
                        uiFLayer.setFeatures(tmpfeature.getFeatures());
                        uiFLayer.setRotation(tmpfeature.getRotation());
                        uiFLayer.setSize(tmpfeature.getSize());
                        uiFLayer.setBindingIndex(tmpfeature.getGroupId());

                        if (temp.getId() != null) {
                            uiFLayer.getAttributes().put(HTML.id_ATTRIBUTE, FacesUtils.getParentUIModelBase(context, component).getId() + "_" + comp.getId() + "_" + tmpfeature.getId());
                        } else {
                            tmpfeature.setId(uiFLayer.getId());
                        }
                        comp.getChildren().add(uiFLayer);
                        tmpfeature.setCompId(uiFLayer.getClientId(context));
                        uiFLayer.setLayer(tmpfeature);
                        break;

                    case DEFAULT:
                    case WFS:
                    case WCS:
                    case SLD:
                    case FES:
                    case GML:
                    case KML:
                    default:
                        break;
                }
            }
        }

        writer.flush();
        if (this.debug) {
            LOGGER.log(Level.INFO, "[DEBUG]  mappane has " + comp.getChildren().size() + " childrens");
        }
        //Setting the model to all children of the MapPane component
        for (final UIComponent tmp : comp.getChildren()) {
            if (tmp instanceof UIWidgetBase) {
                ((UIWidgetBase) tmp).setModel((AbstractModelBase) model);
                ((UIWidgetBase) tmp).setDebug(this.debug);
            }
        }
    }

    @Override
    public void encodeChildren(final FacesContext context, final UIComponent component) throws IOException {
        final UIMapPane uiMapPane = (UIMapPane) component;
        final String jsMapVariable = FacesUtils.getJsVariableFromClientId(uiMapPane.getClientId(context));
        uiMapPane.setAddLayersScript("window.layerToAdd" + jsMapVariable + "=[];");
        final List<UIComponent> childrens = component.getChildren();
        if (this.debug) {
            LOGGER.log(Level.INFO, "[DEBUG] Le composant " + component.getFamily() + " has " + childrens.size() + " children :");
        }
        for (final UIComponent tmp : childrens) {
            if (this.debug) {
                LOGGER.log(Level.INFO, "[DEBUG]  \tChild family's " + tmp.getFamily());
            }
            FacesUtils.encodeRecursive(context, tmp);
            if (tmp instanceof UILayer) {
                final UILayer uiLayer = (UILayer) tmp;
                final Layer layer = uiLayer.getLayer();
                if (!layer.isDisable()) {
                    final String clientId = uiLayer.getClientId(context);
                    final String jsLayerVariable = FacesUtils.getJsVariableFromClientId(uiLayer.getClientId(context));

                    final StringBuilder stringBuilder = new StringBuilder(uiMapPane.getAddLayersScript());
                    //Create an array whjo contains all the MapFaces layers to add for a specific MapPane
                    stringBuilder.

                            append("window.layerToAdd").append(jsMapVariable).append(".push(function() {").
                            //If OpenLayers classes are correctly loaded
                            append("if (window.OpenLayers &&  window.OpenLayers.Layer && window.OpenLayers.Layer.MapFaces) {").
                            //Create a MapFaces layer
                            append(jsLayerVariable).append("= new OpenLayers.Layer.MapFaces('").append(clientId).append("', {").
                                append("id:").append("'").append(jsLayerVariable).append("'").append(",").
                                append("visibility:").append(!layer.isHidden()).append(",").
                                append("maxScale:").append(layer.getMinScaleDenominator()).append(",").
                                append("minScale:").append(layer.getMaxScaleDenominator()).append("").
                            append("});").
                            //append(jsMapVariable).append(".removeLayer(").append(jsLayerVariable).append(");").
                            append(jsMapVariable).append(".addLayer(").append(jsLayerVariable).append(");").
                            append("}});");
                    uiMapPane.setAddLayersScript(stringBuilder.toString());
                }
            }
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void encodeEnd(final FacesContext context, final UIComponent component) throws IOException {
        super.encodeEnd(context, component);
        final UIMapPane comp = (UIMapPane) component;
        if (this.debug) {
            LOGGER.log(Level.INFO, "[DEBUG] MapPaneRenderer ENCOD END");
        }
        final Context model;
        if (comp.getModel() instanceof Context) {
            model = (Context) comp.getModel();
        } else {
            //The model context is null or not an Context instance
            throw new UnsupportedOperationException("The model context is null or not supported yet !");
        }

        final ResponseWriter writer = context.getResponseWriter();
        // ... ending the started elements
        writer.endElement(HTML.DIV_ELEM);
        writer.endElement(HTML.DIV_ELEM);
        /* Construct OpenLayers Map Object */
        writer.startElement(HTML.SCRIPT_ELEM, comp);
        writer.writeAttribute(HTML.TYPE_ATTR, "text/javascript", HTML.TYPE_ATTR);

        //suppression des ":" pour nommer l'objet javascript correspondant correctement
//        String jsObject = FacesUtils.getParentUIModelBase(context, component).getClientId(context);
        final String jsObject = FacesUtils.getJsVariableFromClientId(comp.getClientId(context));
        final String srs = model.getSrs().toUpperCase(Locale.getDefault());

        final StringBuilder stringBuilder = new StringBuilder("")
                /**
                 * If window.maps (list of the maps)  doesn't exist , we create it;
                 */
                .append("if(!window.maps)window.maps = {};")

                /**
                 * Add a null object to the window.maps list
                 */
                .append("window.maps.").append(jsObject).append(" = null;")

                /**
                 * Create an empty Array wich contains all controls to add to the current map
                 */
                .append("window.controlToAdd" + jsObject + " = []; ")

                /**
                 * Define a function  who will load the map;
                 */
                .append("window.loadMap" + jsObject + " = function() {")

                /**
                 * Test if map options object doesn't exist and all needed OpenLayers class has been loaded correctly
                 */
                .append("if (typeof ").append(jsObject).append("_mapOptions == 'undefined' ").
                append("&& window.OpenLayers && window.OpenLayers.Projection ").
                append("&& window.OpenLayers.Size && window.OpenLayers.Bounds) { ")

                /**
                 * Create the map options object, it contains all options needed to render a map;
                 */
                .append("var ").append(jsObject).append("_mapOptions = {");


        /**
         * OpenLayers map options
         */
        /**
         * Id of the javascript Map object
         */
        stringBuilder.append("id: '" + jsObject + "',");

        /**
         * List of Control objects to add to the map by default
         */
        stringBuilder.append("controls: [],");

        /**
         * Projection
         */
        stringBuilder.append("projection: new OpenLayers.Projection('").append(srs).append("'),");

        /**
         * Units
         */
        stringBuilder.append("units: '").append(MapUtils.getUnits(srs)).append("',");

        /**
         * Size
         */
        stringBuilder.append("size: new OpenLayers.Size('").append(model.getWindowWidth()).append("','").append(model.getWindowHeight()).append("'),");


        //@Todo Define clearly which extent is used to restrict the zoom
        /**
         * MaxExtent
         */
        stringBuilder.append("maxExtent: new OpenLayers.Bounds(").append(comp.getMaxExtent()).append("),");

        /**
         * CurrentExtent , it'as a MapFaces option not an OpenLayers one
         */
        stringBuilder.append("currentExtent: new OpenLayers.Bounds(").append(model.getMinx()).append(",").append(model.getMiny()).append(",").append(model.getMaxx()).append(",").append(model.getMaxy()).append("),");

        /**
         * RestrictedExtent
         */
        stringBuilder.append("restrictedExtent: new OpenLayers.Bounds(").append(comp.getMaxExtent()).append("),");

        /**
         * MaxResolution
         */
        stringBuilder.append("maxResolution: 'auto',");

        /**
         * NumZoomLevels
         */
        stringBuilder.append("numZoomLevels: ").append(comp.getNumZoomLevels()).append(",");

        /**
         * Theme
         */
        stringBuilder.append("theme:  null,");

        /**
         * FractionnalZoom
         */
        stringBuilder.append("fractionalZoom:  true,");


        /**
         * MapFaces map options
         */
        /**
         * moveend event
         */
        stringBuilder.append("moveend:  [],");
        /**
         * zoomend event
         */
        stringBuilder.append("zoomend:  [],");
        /**
         * LayersName
         */
        stringBuilder.append("layersName:  '").append(model.getLayersCompId()).append("',");

        /**
         * mfAjaxCompId : Id of the a4j component to call for refresh  the map
         */
        stringBuilder.append("mfAjaxCompId: '").append(FacesUtils.getParentUIModelBase(context, component).getAjaxCompId()).append("',");

        /**
         * mfFormId : Id of the parent UIForm  of the UIMapPane;
         */
        stringBuilder.append("mfFormId: '").append(FacesUtils.getFormId(context, component)).append("',");

        /**
         * mfRequestId : Id of the request, a totally arbitrary attribute
         */
        stringBuilder.append("mfRequestId: 'updateBboxOrWindow'");

        /**
         * Close  the map options creation
         */
        stringBuilder.append("};");

        /**
         * Else If map options object already exist and all needed OpenLayers class has been loaded correctly
         */
        stringBuilder.append("} else if (window.OpenLayers && window.OpenLayers.Bounds) {").

                /**
                 * Overwrite the current layersName
                 */
                append(jsObject + "_mapOptions.layersName = '").append(model.getLayersCompId()).append("' ;").

                /**
                 * Overwrite the current extent
                 */
                append(jsObject).append("_mapOptions.currentExtent = new OpenLayers.Bounds(").

                append(model.getMinx()).append(",").append(model.getMiny()).append(",").

                append(model.getMaxx()).append(",").append(model.getMaxy()).append(");");

        /**
         * Close the Else If
         */
        stringBuilder.append("}").
                /**
                 * If OpenLayers class are correctly loaded we create the Map object and push it into the window.maps list
                 */
                append("if (window.OpenLayers && window.OpenLayers.Map) {").
                /**
                 * Create the JS  Map object
                 */
               // append("if(window.").append(jsObject).append(")window.").append(jsObject).append(".destroy();").
                append("window.").append(jsObject).append("     = new OpenLayers.Map('").append(comp.getClientId(context)).append("'," + jsObject + "_mapOptions);").
                /**
                 * Add the MapFaces layers
                 */
                append(comp.getAddLayersScript()).
                /**
                 * Attach the Map object to the window.maps list
                 */
                append("window.maps.").append(jsObject).append("     = window.").append(jsObject).append(";").
                /**
                 * Attach OpenLayers.MapFaces.Layer(s) to the map
                 */
                append("if( window.layerToAdd" + jsObject + ")").
                append("for (var i = 0 ; i <  window.layerToAdd" + jsObject + ".length; i++) {").
                append("window.layerToAdd" + jsObject + "[i]();").
                append("}").
                /**
                 * Close the If
                 */
                append("}").
                /**
                 * Close the loadMap function declaration
                 */
                append("};").
                /**
                 * Run the loadMap function
                 */
                append("window.loadMap" + jsObject + "();");


        writer.write(stringBuilder.toString());
        writer.endElement(HTML.SCRIPT_ELEM);
        writer.endElement(HTML.DIV_ELEM);
        writer.flush();
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void decode(final FacesContext context, final UIComponent component) {
        super.decode(context, component);
        final UIMapPane comp = (UIMapPane) component;
        if (this.debug) {
            LOGGER.log(Level.INFO, "[DEBUG] MapPaneRenderer DECODE");
        }
        final UIContext contextComp = (UIContext) FacesUtils.getParentUIContext(context, comp);
        final Context tmp = (Context) contextComp.getModel();

        if (context.getExternalContext().getRequestParameterMap() != null) {
            final Map params = context.getExternalContext().getRequestParameterMap();
            final String render = (String) params.get("render");
            if (render == null || render.equals("true")) {
                comp.setInitDisplay(true);
            }
        }

        contextComp.setModel((AbstractModelBase) tmp);
        comp.setModel((AbstractModelBase) tmp);
    }
}
