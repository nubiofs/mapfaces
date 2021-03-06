/**
 * This function add an OpenLayers.layer.Vector  to an  OpenLayers.Map.
 * Features of this layer are generated by an feature collection in OpenLayers.GeoJSON format
 *
 * @param  json    String       The GeoJson feature collection to parse
 * @param  data    String       object always to null (we need it because of Mootools ajax request and IE)
 *
 *
 */
window.loadGeoJSON = function (json, data) {
    var map = null;
    var layer = null;
    var featureCollection = null;

    if ( window.maps && json != null) {

        //case if the json is in fact an XmlHttpRequest response object
        if (json.responseText) {
            json  = json.responseText
        }
        
        if (json != null && json != "") {
            //json contains targetMapId (String: jsVariable of the MapPane),
            //targetLayerId(String: jsVariabele of the layer)
            //and featureCollection (GeoJSON) variables
            eval(json);
            map = window.maps[targetMapId];
            layer = window[targetLayerId];
        }
    }
    
    if (map != null) {

        if (layer != null && layer.div != null) {
                map.removeLayer(layer);
                layer.destroy();
                window[targetLayerId].destroy();
        }

        if (featureCollection != null) {

            var geojson_format = new OpenLayers.Format.GeoJSON();
            var myStyles = new OpenLayers.StyleMap({
                    "default": new OpenLayers.Style({
                        fillOpacity: "0.5",
                        fillColor: "#ff0000",
                        strokeColor: "#ff9933",
                        strokeWidth: 5,
                        pointRadius: "10"
                    })
                });
            layer = new OpenLayers.Layer.Vector("GeoJSON", {styleMap: myStyles});
            map.addLayer(layer);
            layer.addFeatures(geojson_format.read(featureCollection));
            map.zoomToExtent(layer.getDataExtent());
            window[targetLayerId] = layer;
        }
    }
}

window.loadMap = function(id) {
    if (id.indexOf(":") != -1) {
        id = id.replace(":","");
    }
    eval("if(window.loadMap"+id+")window.loadMap"+id+"();"); 
}

window.loadMapAndZoom = function(id) {
    window.loadMap(id);
    window.addControlsToMap(id);
    window.maps[id].zoomToExtent(window.maps[id].currentExtent);
}

window.addControlsToMap = function(id) {
    var controls = eval("if(window.controlToAdd"+id+")window.controlToAdd"+id);    
    if (controls) {
        for (var i = 0; i < controls.length; i++) {
            controls[i]();
        }
    }
}

window.reloadAllMaps = function() {
    if (window.maps) {
        for (var i in window.maps) {
            var map = window.maps[i];
            if (map && map.div && map.div.style.display != 'none') {
                if (map.currentExtent)
                  map.zoomToExtent(map.currentExtent);
                else
                  map.zoomToMaxExtent();
            } else {
                 window.loadMapAndZoom(i);
            }
        }
    }
}

////Add onload function to window to zoom the map to the maxExtent
OpenLayers.Event.observe(window, 'load', window.reloadAllMaps);
