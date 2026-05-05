package com.example.swingbridge.views;

import com.example.swingbridge.ui.MainLayout;
import com.example.swingbridge.ui.SwingEditorView;
import com.jdimension.jlawyer.client.JKanzleiGUIBridge;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@PageTitle("Addresses / Search")
@Route(value = "addresses/search", layout = MainLayout.class)
public class AddressesSearchView extends SwingEditorView {
    @Override
    protected void onSwingReady(JKanzleiGUIBridge gui) {
        gui.showAddressesSearch();
    }
}
