package com.example.swingbridge.views;

import com.example.swingbridge.ui.MainLayout;
import com.example.swingbridge.ui.SwingEditorView;
import com.jdimension.jlawyer.client.JKanzleiGUIBridge;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@PageTitle("Desktop")
@Route(value = "desktop", layout = MainLayout.class)
public class DesktopView extends SwingEditorView {
    @Override
    protected void onSwingReady(JKanzleiGUIBridge gui) {
        gui.showDesktop();
    }
}
