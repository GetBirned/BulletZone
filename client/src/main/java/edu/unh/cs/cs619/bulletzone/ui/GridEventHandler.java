package edu.unh.cs.cs619.bulletzone.ui;

import com.squareup.otto.Subscribe;

import edu.unh.cs.cs619.bulletzone.ClientActivity;
import edu.unh.cs.cs619.bulletzone.rest.GridUpdateEvent;
import edu.unh.cs.cs619.bulletzone.events.BusProvider;

public class GridEventHandler {

    private static final String TAG = GridEventHandler.class.getSimpleName();
    private ClientActivity clientActivity;
    private BusProvider busProvider;

    public GridEventHandler(ClientActivity clientActivity, BusProvider busProvider) {
        this.clientActivity = clientActivity;
        this.busProvider = busProvider;
        busProvider.getEventBus().register(this);
    }

    @Subscribe
    public void onUpdateGrid(GridUpdateEvent event) {
        if (event.gw != null) {
            clientActivity.updateGrid(event.gw);
        }
    }

    // Add other grid-related event handling methods as needed

    public void unregister() {
        busProvider.getEventBus().unregister(this);
    }
}
