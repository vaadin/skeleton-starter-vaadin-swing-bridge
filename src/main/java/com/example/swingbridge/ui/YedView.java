package com.example.swingbridge.ui;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.modernization.swing.bridge.component.SwingBridge;

@Route(value = "yed")
public class YedView extends VerticalLayout {
	
    //private static final String MAIN_CLASS = "uk.ac.starlink.topcat.Driver";
	private static final String MAIN_CLASS = "com.yworks.A.yEd";

    public YedView() {
        add(new SwingBridge(MAIN_CLASS));
    }

}
