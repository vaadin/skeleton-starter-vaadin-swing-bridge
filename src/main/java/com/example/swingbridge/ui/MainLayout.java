package com.example.swingbridge.ui;

import com.example.swingbridge.views.*;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.Layout;

@Layout
public class MainLayout extends AppLayout {

    public MainLayout() {
        DrawerToggle toggle = new DrawerToggle();

        H1 title = new H1(getTranslation("app.title"));
        title.getStyle()
                .set("font-size", "1.125rem")
                .set("margin", "0");

        addToNavbar(toggle, title);

        SideNav nav = createSideNav();
        nav.getStyle().set("margin", "var(--vaadin-gap-s)");

        Scroller scroller = new Scroller(nav);
        addToDrawer(scroller);

        setPrimarySection(Section.DRAWER);
    }

    private SideNavItem group(String translationKey, VaadinIcon icon) {
        SideNavItem item = new SideNavItem(getTranslation(translationKey));
        item.setPrefixComponent(icon.create());
        return item;
    }

    private SideNav createSideNav() {
        SideNav nav = new SideNav();

        // Desktop
        nav.addItem(new SideNavItem(
                getTranslation("nav.desktop"),
                DesktopView.class,
                VaadinIcon.DESKTOP.create()));

        // Cases (Akten)
        SideNavItem cases = group("nav.cases", VaadinIcon.FOLDER);
        cases.addItem(new SideNavItem(
                getTranslation("nav.cases.new"),
                CasesNewView.class,
                VaadinIcon.PLUS.create()));
        cases.addItem(new SideNavItem(
                getTranslation("nav.cases.search"),
                CasesSearchView.class,
                VaadinIcon.SEARCH.create()));
        nav.addItem(cases);

        // Addresses (Adressen)
        SideNavItem addresses = group("nav.addresses", VaadinIcon.GROUP);
        addresses.addItem(new SideNavItem(
                getTranslation("nav.addresses.new"),
                AddressesNewView.class,
                VaadinIcon.PLUS.create()));
        addresses.addItem(new SideNavItem(
                getTranslation("nav.addresses.search"),
                AddressesSearchView.class,
                VaadinIcon.SEARCH.create()));
        nav.addItem(addresses);

        // Calendar (Kalender)
        SideNavItem calendar = group("nav.calendar", VaadinIcon.CALENDAR);
        calendar.addItem(new SideNavItem(
                getTranslation("nav.calendar.bydate"),
                CalendarByDateView.class,
                VaadinIcon.CALENDAR_CLOCK.create()));
        calendar.addItem(new SideNavItem(
                getTranslation("nav.calendar.search"),
                CalendarSearchView.class,
                VaadinIcon.SEARCH.create()));
        calendar.addItem(new SideNavItem(
                getTranslation("nav.calendar.missing"),
                CalendarMissingView.class,
                VaadinIcon.EXCLAMATION_CIRCLE.create()));
        nav.addItem(calendar);

        // Mail (Post)
        SideNavItem mail = group("nav.mail", VaadinIcon.ENVELOPE);
        mail.addItem(new SideNavItem(
                getTranslation("nav.mail.messaging"),
                MessagingView.class,
                VaadinIcon.CHAT.create()));
        mail.addItem(new SideNavItem(
                getTranslation("nav.mail.email"),
                EmailView.class,
                VaadinIcon.MAILBOX.create()));
        mail.addItem(new SideNavItem(
                getTranslation("nav.mail.bea"),
                BeaView.class,
                VaadinIcon.SHIELD.create()));
        mail.addItem(new SideNavItem(
                getTranslation("nav.mail.dispatch"),
                DispatchView.class,
                VaadinIcon.PAPERPLANE.create()));
        mail.addItem(new SideNavItem(
                getTranslation("nav.mail.scans"),
                ScansView.class,
                VaadinIcon.BARCODE.create()));
        nav.addItem(mail);

        // Research (Recherche)
        SideNavItem research = group("nav.research", VaadinIcon.BOOK);
        research.addItem(new SideNavItem(
                getTranslation("nav.research.fulltext"),
                FullTextSearchView.class,
                VaadinIcon.FILE_SEARCH.create()));
        research.addItem(new SideNavItem(
                getTranslation("nav.research.reports"),
                ReportsView.class,
                VaadinIcon.CHART.create()));
        nav.addItem(research);

        return nav;
    }
}
