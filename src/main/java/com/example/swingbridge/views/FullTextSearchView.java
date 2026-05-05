package com.example.swingbridge.views;

import com.example.swingbridge.ui.MainLayout;
import com.example.swingbridge.ui.SwingEditorView;
import com.jdimension.jlawyer.client.JKanzleiGUIBridge;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@PageTitle("Research / Full Text")
@Route(value = "research/fulltext", layout = MainLayout.class)
public class FullTextSearchView extends SwingEditorView {
    @Override
    protected void navigateSwing(JKanzleiGUIBridge gui) {
        gui.showFullTextSearch();
    }
}
