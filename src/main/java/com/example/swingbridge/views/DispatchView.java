package com.example.swingbridge.views;

import com.example.swingbridge.ui.MainLayout;
import com.example.swingbridge.ui.SwingEditorView;
import com.jdimension.jlawyer.client.JKanzleiGUIBridge;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@PageTitle("Mail / Letter & Fax")
@Route(value = "mail/dispatch", layout = MainLayout.class)
public class DispatchView extends SwingEditorView {
    @Override
    protected void navigateSwing(JKanzleiGUIBridge gui) {
        gui.showDispatch();
    }
}
