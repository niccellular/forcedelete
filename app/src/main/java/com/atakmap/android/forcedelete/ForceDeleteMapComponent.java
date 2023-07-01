
package com.atakmap.android.forcedelete;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Looper;

import com.atakmap.android.cot.CotMapComponent;
import com.atakmap.android.importexport.CotEventFactory;
import com.atakmap.android.ipc.AtakBroadcast;
import com.atakmap.android.ipc.AtakBroadcast.DocumentedIntentFilter;

import com.atakmap.android.maps.MapEvent;
import com.atakmap.android.maps.MapEventDispatcher;
import com.atakmap.android.maps.MapItem;
import com.atakmap.android.maps.MapItemDeleteReceiver;
import com.atakmap.android.maps.MapView;
import com.atakmap.android.dropdown.DropDownMapComponent;

import com.atakmap.comms.CotDispatcher;
import com.atakmap.coremap.cot.event.CotEvent;
import com.atakmap.coremap.log.Log;
import com.atakmap.android.forcedelete.plugin.R;

import java.util.Collection;
import java.util.Locale;
import java.util.UUID;

public class ForceDeleteMapComponent extends DropDownMapComponent {

    private static final String TAG = "ForceDeleteMapComponent";

    public void onCreate(final Context context, Intent intent, final MapView view) {
        super.onCreate(context, intent, view);

        final MapEventDispatcher eventDispatcher = view.getMapEventDispatcher();
        eventDispatcher.pushListeners();

        // assign a ITEM_REMOVED handler to all existing map items
        for (MapItem mi : view.getRootGroup().getAllItems()) {
            if (mi.toString().contains("[Untitled Item]") || mi.toString().contains("no-defined-type") || mi.getType().equals("self"))
                continue;
            Log.d(TAG, "Adding handler to existing map item: " + mi);
            eventDispatcher.addMapItemEventListener(mi, new MapEventDispatcher.OnMapEventListener() {
                @Override
                public void onMapItemMapEvent(MapItem mapItem, final MapEvent mapEvent) {
                    if (mapItem.getUID() == view.getSelfMarker().getUID())
                        return;
                    if (mapEvent.getType().equals(MapEvent.ITEM_REMOVED)) {
                        if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
                            if (!mapItem.getType().equalsIgnoreCase("u-d-c-c") ||       // circle
                                    !mapItem.getType().equalsIgnoreCase("u-d-r") ||     // rect
                                    !mapItem.getType().equalsIgnoreCase("u-d-f") ||     // trap
                                    !mapItem.getType().equalsIgnoreCase("u-d-f-m"))     // freehand
                            {
                                return;
                            }
                            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(view.getContext());
                            alertBuilder.setTitle("Remote Force Delete");
                            alertBuilder.setMessage("Do you want to remotely force delete this marker?");
                            final AlertDialog alertDialog = alertBuilder.create();
                            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    if (i == -1) {
                                        String uid = mapEvent.getItem().getUID();
                                        final String CoT = String.format(Locale.US, "<?xml version=\"1.0\" standalone=\"yes\"?> <event start=\"2012-01-01T00:00:00Z\" time=\"2012-01-01T00:00:00Z\" stale=\"2020-01-01T00:00:00Z\" how=\"m-g\" type=\"t-x-d-d\" uid=\"%s\" version=\"2.0\"> <detail> <link uid=\"%s\" relation=\"none\" type=\"none\"/> <__forcedelete/> </detail> <point ce=\"0\" le=\"0\" lat=\"36.789783\" lon=\"0\" hae=\"0\"/> </event>", UUID.randomUUID().toString(), uid);
                                        Log.d(TAG, "Sending remote __forcedelete for uid: " + uid);
                                        CotMapComponent.getExternalDispatcher().dispatch(CotEvent.parse(CoT));
                                        CotMapComponent.getExternalDispatcher().dispatch(CotEvent.parse(CoT));
                                    }
                                }
                            });
                            alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    return;
                                }
                            });
                            alertDialog.setCancelable(true);
                            alertDialog.show();
                        }
                    }
                }
            });
        }

        // for all new map items, capture them here and setup the handler
        eventDispatcher.addMapEventListener(MapEvent.ITEM_ADDED, new MapEventDispatcher.MapEventDispatchListener() {
            @Override
            public void onMapEvent(final MapEvent mapEvent) {
                Log.d(TAG, "ITEM_ADDED: " + mapEvent.getItem());
                eventDispatcher.addMapItemEventListener(mapEvent.getItem(), new MapEventDispatcher.OnMapEventListener() {
                    @Override
                    public void onMapItemMapEvent(MapItem mapItem, final MapEvent mapEvent) {
                        if (mapItem.toString().contains("[Untitled Item]") || mapItem.toString().contains("no-defined-type") || mapItem.getUID() == view.getSelfMarker().getUID())
                            return;
                        if (!mapItem.getType().startsWith("a-") &&                  // marker
                                !mapItem.getType().startsWith("b-") &&              // spot
                                !mapItem.getType().startsWith("u-d-v") &&           // vehicle
                                !mapItem.getType().equalsIgnoreCase("u-d-c-c") &&   // circle
                                !mapItem.getType().equalsIgnoreCase("u-d-r") &&     // rect
                                !mapItem.getType().equalsIgnoreCase("u-d-f") &&     // trap
                                !mapItem.getType().equalsIgnoreCase("u-d-f-m"))     // freehand
                        {
                            return;
                        }
                        if (mapEvent.getType().equals(MapEvent.ITEM_REMOVED)) {
                            if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
                                Log.d(TAG, "MapItem removed: " + mapItem);
                                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(view.getContext());
                                alertBuilder.setTitle("Remote Force Delete");
                                alertBuilder.setMessage("Do you want to remotely force delete this marker?");
                                final AlertDialog alertDialog = alertBuilder.create();
                                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        if (i == -1) {
                                            String uid = mapEvent.getItem().getUID();
                                            final String CoT = String.format(Locale.US, "<?xml version=\"1.0\" standalone=\"yes\"?> <event start=\"2012-01-01T00:00:00Z\" time=\"2012-01-01T00:00:00Z\" stale=\"2020-01-01T00:00:00Z\" how=\"m-g\" type=\"t-x-d-d\" uid=\"%s\" version=\"2.0\"> <detail> <link uid=\"%s\" relation=\"none\" type=\"none\"/> <__forcedelete/> </detail> <point ce=\"0\" le=\"0\" lat=\"36.789783\" lon=\"0\" hae=\"0\"/> </event>", UUID.randomUUID().toString(), uid);
                                            Log.d(TAG, "Sending remote __forcedelete for uid: " + uid);
                                            CotMapComponent.getExternalDispatcher().dispatch(CotEvent.parse(CoT));
                                            CotMapComponent.getExternalDispatcher().dispatch(CotEvent.parse(CoT));
                                        }
                                    }
                                });
                                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        return;
                                    }
                                });
                                alertDialog.setCancelable(true);
                                alertDialog.show();
                            }
                        }
                    }
                });
            }
        });
    }

    @Override
    protected void onDestroyImpl(Context context, MapView view) {
        super.onDestroyImpl(context, view);
        view.getMapEventDispatcher().popListeners();
    }
}
