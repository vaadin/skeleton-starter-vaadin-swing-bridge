package com.example.swingbridge.views;

import java.util.Arrays;
import java.util.function.Consumer;

import com.jdimension.jlawyer.client.JKanzleiGUIBridge;
import com.jdimension.jlawyer.persistence.CityDataBean;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.icon.VaadinIcon;
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
        setWidth("32rem");
        setHeight("28rem");

        queryField.setPlaceholder("Query");
        queryField.setValue(initialQuery == null ? "" : initialQuery);
        queryField.setWidthFull();

        Button searchButton = new Button(VaadinIcon.SEARCH.create(), e -> search());
        searchButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        queryField.setSuffixComponent(searchButton);
        queryField.addKeyPressListener(Key.ENTER, e -> search());

        grid.addColumn(CityDataBean::getZipCode).setHeader("PLZ").setAutoWidth(true);
        grid.addColumn(CityDataBean::getCity).setHeader("Ort").setFlexGrow(1);
        grid.setSizeFull();
        grid.addThemeVariants(GridVariant.LUMO_COMPACT, GridVariant.LUMO_NO_BORDER);
        grid.addItemDoubleClickListener(e -> useSelection(e.getItem()));

        VerticalLayout content = new VerticalLayout(queryField, grid);
        content.setSizeFull();
        content.setPadding(false);
        content.setSpacing(true);
        content.expand(grid);
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
        if (query == null || query.isBlank()) {
            grid.setItems();
            return;
        }
        SwingBridge.interop().of(JKanzleiGUIBridge.class)
                .requestAsync(b -> b.searchCityData(query))
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
