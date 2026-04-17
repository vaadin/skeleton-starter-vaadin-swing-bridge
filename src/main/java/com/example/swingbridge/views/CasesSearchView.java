package com.example.swingbridge.views;

import com.example.swingbridge.ui.MainLayout;
import com.example.swingbridge.ui.SwingEditorView;
import com.jdimension.jlawyer.bridge.JLawyerNavigation;
import com.vaadin.flow.router.Route;

@Route(value = "cases/search", layout = MainLayout.class)
public class CasesSearchView extends SwingEditorView {
    @Override
    protected void onSwingReady(JLawyerNavigation nav) {
        nav.showCasesSearch();
    }
}
