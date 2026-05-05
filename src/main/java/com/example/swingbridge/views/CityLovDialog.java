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
        setResizable(true);
        setWidth("32rem");
        setHeight("28rem");
        setCloseOnOutsideClick(false);
        // Drop the Dialog's default body padding so the tinted content
        // layout extends to all edges (left/right/bottom).
        getElement().setAttribute("theme", "no-padding");

        getElement()
                .executeJs("this.$.overlay.$.overlay.style.lineHeight = '0'");
        getElement().executeJs(
                "this.$.overlay.$.overlay.querySelector('header').style.padding = '0 4px 0 0'");
        getElement().executeJs(
                "this.$.overlay.$.overlay.querySelector('div[part~=\"title\"]').style.fontSize = 'smaller'");

        // Custom drag handler bound to the LOV's overlay header.
        //
        // We can't use Vaadin Dialog's built-in setDraggable(true) here
        // because Vaadin auto-attaches new dialogs into the active
        // overlay's content slot. When chooseCity opens this LOV from
        // inside an active SwingBridgeDialog chain, the LOV's
        // vaadin-dialog host ends up nested inside the parent dialog's
        // overlay structure. With both dialogs marked draggable, a
        // pointerdown on the LOV header bubbles up and triggers the
        // parent's drag handler too — both end up applying the same
        // top/left absolute position to their own [part="overlay"]
        // inner elements, so the parent dialog jumps to align with the
        // LOV's drop point.
        //
        // The custom handler below is bound to OUR overlay's header
        // and updates only OUR [part="overlay"] inner element's inline
        // top/left. Because we never enable Vaadin's drag mode, the
        // parent's drag handler isn't woken up by our pointer events.
        getElement().executeJs(
                "const inner = this.$.overlay.$.overlay;"
                + "const header = inner.querySelector('header');"
                + "header.style.cursor = 'move';"
                + "header.addEventListener('pointerdown', e => {"
                + "  if (e.target.closest('vaadin-button, button, [tabindex]')) return;"
                + "  e.preventDefault();"
                + "  const rect = inner.getBoundingClientRect();"
                + "  const startX = e.clientX, startY = e.clientY;"
                + "  const startLeft = rect.left, startTop = rect.top;"
                + "  const onMove = ev => {"
                + "    inner.style.position = 'absolute';"
                + "    inner.style.left = (startLeft + ev.clientX - startX) + 'px';"
                + "    inner.style.top = (startTop + ev.clientY - startY) + 'px';"
                + "  };"
                + "  const onUp = () => {"
                + "    document.removeEventListener('pointermove', onMove);"
                + "    document.removeEventListener('pointerup', onUp);"
                + "  };"
                + "  document.addEventListener('pointermove', onMove);"
                + "  document.addEventListener('pointerup', onUp);"
                + "});");

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
        queryRow.setPadding(false);
        queryRow.setAlignItems(FlexComponent.Alignment.END);
        queryRow.expand(queryField);

        grid.addColumn(CityDataBean::getZipCode).setHeader("PLZ").setAutoWidth(true);
        grid.addColumn(CityDataBean::getCity).setHeader("Ort").setFlexGrow(1);
        grid.setSizeFull();
        grid.addThemeVariants(GridVariant.LUMO_COMPACT, GridVariant.LUMO_NO_BORDER);
        grid.addItemDoubleClickListener(e -> useSelection(e.getItem()));

        VerticalLayout content = new VerticalLayout(queryRow, grid);
        content.getStyle().set("background", "var(--lumo-contrast-5pct)");
        content.setSizeFull();
        content.setSpacing(true);
        content.expand(grid);
        add(content);

        this.getElement().getStyle().set("background", "var(--lumo-contrast-5pct)");

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
