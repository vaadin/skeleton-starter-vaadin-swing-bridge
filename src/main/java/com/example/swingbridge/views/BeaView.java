package com.example.swingbridge.views;

import com.example.swingbridge.ui.MainLayout;
import com.example.swingbridge.ui.SwingEditorView;
import com.jdimension.jlawyer.client.JKanzleiGUIBridge;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@PageTitle("Mail / beA")
@Route(value = "mail/bea", layout = MainLayout.class)
public class BeaView extends SwingEditorView {
    @Override
    protected void navigateSwing(JKanzleiGUIBridge gui) {
        gui.showBea();
    }
}
