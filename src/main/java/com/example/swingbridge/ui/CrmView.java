package com.example.swingbridge.ui;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.modernization.swing.bridge.component.SwingBridge;

@Route("")
public class CrmView extends VerticalLayout {
    private static final String MAIN_CLASS = "com.swingcrm.SwingCrmApplication";

    public CrmView() {
        add(new SwingBridge(MAIN_CLASS));
    }
}
