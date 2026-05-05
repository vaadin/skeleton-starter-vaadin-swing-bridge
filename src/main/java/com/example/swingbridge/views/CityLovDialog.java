package com.example.swingbridge.views;

import java.util.Arrays;
import java.util.function.Consumer;

import com.jdimension.jlawyer.client.JKanzleiGUIBridge;
import com.jdimension.jlawyer.persistence.CityDataBean;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.modernization.swing.bridge.component.SwingBridge;

/**
 * Vaadin replacement for j-lawyer's Swing {@code CitySearchDialog}.
 * Renders the ZIP/City picker as a Vaadin {@code Dialog} that sources its
 * rows from the Swing-side {@code searchCityData} method via
 * {@link com.vaadin.modernization.swing.bridge.interop.BridgeHandle#requestAsync}.
 * <p>
 * The dialog completes the supplied {@code Consumer<CityDataBean>} exactly
 * once with the picked bean (or {@code null} on Cancel / dialog close).
 */
public class CityLovDialog extends Dialog {

    private final TextField queryField = new TextField();
    private final Grid<CityDataBean> grid = new Grid<>(CityDataBean.class, false);
    private final Consumer<CityDataBean> onPick;
    private boolean completed = false;

    public CityLovDialog(String initialQuery, Consumer<CityDataBean> onPick) {
        this.onPick = onPick;
        setHeaderTitle("PLZ / Ort");
        setDraggable(true);
        setWidth("32rem");
        setHeight("28rem");

        // Close button on the header — same shape as SwingBridgeDialog uses.
        Button closeButton = new Button(VaadinIcon.CLOSE_SMALL.create(),
                e -> closeWith(null));
        closeButton.setMaxWidth(25, Unit.PIXELS);
        closeButton.setMaxHeight(25, Unit.PIXELS);
        closeButton.getStyle().set("margin-left", "auto");
        closeButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY,
                ButtonVariant.LUMO_ERROR);
        closeButton.getElement().setAttribute("tabindex", "-1");
        getHeader().add(closeButton);

        queryField.setPlaceholder("Query");
        queryField.setValue(initialQuery == null ? "" : initialQuery);
        queryField.addKeyPressListener(Key.ENTER, e -> search());

        Icon searchIcon = VaadinIcon.SEARCH.create();
        searchIcon.setColor("var(--lumo-primary-contrast-color)");
        Button searchButton = new Button(searchIcon, e -> search());
        searchButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        HorizontalLayout queryRow = new HorizontalLayout(queryField, searchButton);
        queryRow.setWidthFull();
        queryRow.setAlignItems(FlexComponent.Alignment.END);
        queryRow.expand(queryField);

        grid.addColumn(CityDataBean::getZipCode).setHeader("PLZ").setAutoWidth(true);
        grid.addColumn(CityDataBean::getCity).setHeader("Ort").setFlexGrow(1);
        grid.setSizeFull();
        grid.addThemeVariants(GridVariant.LUMO_COMPACT, GridVariant.LUMO_NO_BORDER);
        grid.addItemDoubleClickListener(e -> useSelection(e.getItem()));

        VerticalLayout content = new VerticalLayout(queryRow, grid);
        content.setSizeFull();
        content.setPadding(true);
        content.setSpacing(true);
        content.expand(grid);
        // Subtle tint to visually separate the body from the white header.
        content.getStyle().set("background", "var(--lumo-contrast-5pct)");
        add(content);

        Button cancel = new Button("Cancel", e -> closeWith(null));
        Button ok = new Button("OK", e -> {
            CityDataBean picked = grid.asSingleSelect().getValue();
            if (picked != null) closeWith(picked);
        });
        ok.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        getFooter().add(new HorizontalLayout(cancel, ok));

        addOpenedChangeListener(e -> {
            if (!e.isOpened() && !completed) {
                deliver(null);
            }
        });

        if (!queryField.isEmpty()) {
            search();
        }
    }

    private void search() {
        String query = queryField.getValue();
        SwingBridge.interop().of(JKanzleiGUIBridge.class)
                .requestAsync(b -> b.searchCityData(query == null ? "" : query))
                .whenComplete((rows, err) -> getUI().ifPresent(ui -> ui.access(() -> {
                    if (err != null) {
                        grid.setItems();
                        return;
                    }
                    grid.setItems(rows == null ? java.util.List.of()
                            : Arrays.asList(rows));
                })));
    }

    private void useSelection(CityDataBean bean) {
        if (bean != null) closeWith(bean);
    }

    private void closeWith(CityDataBean bean) {
        deliver(bean);
        close();
    }

    private void deliver(CityDataBean bean) {
        if (completed) return;
        completed = true;
        onPick.accept(bean);
    }
}
