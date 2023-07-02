
package com.atakmap.android.forcedelete.plugin;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import com.atak.plugins.impl.PluginContextProvider;
import com.atakmap.android.maps.MapComponent;
import com.atakmap.android.maps.MapView;
import com.atakmap.android.forcedelete.ForceDeleteMapComponent;

import com.atak.plugins.impl.AbstractPlugin;
import gov.tak.api.plugin.IPlugin;
import gov.tak.api.plugin.IServiceController;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import com.atakmap.coremap.log.Log;

public class ForceDeleteLifecycle extends AbstractPlugin implements IPlugin {

    private MapView mapView;

    private final static String TAG = "ForceDeleteLifecycle";

    public ForceDeleteLifecycle(IServiceController isc) {
        super(isc, new ForceDeleteTool(((PluginContextProvider) isc.getService(PluginContextProvider.class)).getPluginContext()),
                (MapComponent) new ForceDeleteMapComponent());
    }
}
