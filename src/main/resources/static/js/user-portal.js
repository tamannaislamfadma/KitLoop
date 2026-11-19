document.addEventListener("DOMContentLoaded", function () {

    const body = document.body;

    const openButton =
        document.getElementById(
            "sidebarToggleButton"
        );

    const closeButton =
        document.getElementById(
            "sidebarCloseButton"
        );

    const backdrop =
        document.getElementById(
            "sidebarBackdrop"
        );

    const sidebar =
        document.getElementById(
            "userSidebar"
        );

    function openSidebar() {
        body.classList.add(
            "sidebar-open"
        );
    }

    function closeSidebar() {
        body.classList.remove(
            "sidebar-open"
        );
    }

    if (openButton) {

        openButton.addEventListener(
            "click",
            openSidebar
        );
    }

    if (closeButton) {

        closeButton.addEventListener(
            "click",
            closeSidebar
        );
    }

    if (backdrop) {

        backdrop.addEventListener(
            "click",
            closeSidebar
        );
    }

    if (sidebar) {

        const navigationLinks =
            sidebar.querySelectorAll(
                ".sidebar-link"
            );

        navigationLinks.forEach(
            function (link) {

                link.addEventListener(
                    "click",
                    function () {

                        if (
                            window.innerWidth <= 900
                        ) {
                            closeSidebar();
                        }
                    }
                );
            }
        );
    }

    document.addEventListener(
        "keydown",
        function (event) {

            if (
                event.key === "Escape"
            ) {
                closeSidebar();
            }
        }
    );

    window.addEventListener(
        "resize",
        function () {

            if (
                window.innerWidth > 900
            ) {
                closeSidebar();
            }
        }
    );
});