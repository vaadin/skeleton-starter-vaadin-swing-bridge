package com.example.swingbridge.views;

import com.example.swingbridge.ui.MainLayout;
import com.example.swingbridge.ui.SwingEditorView;
import com.jdimension.jlawyer.client.JKanzleiGUIBridge;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@PageTitle("Calendar / By Date")
@Route(value = "calendar/by-date", layout = MainLayout.class)
public class CalendarByDateView extends SwingEditorView {
    @Override
    protected void navigateSwing(JKanzleiGUIBridge gui) {
        gui.showCalendarByDate();
    }
}
