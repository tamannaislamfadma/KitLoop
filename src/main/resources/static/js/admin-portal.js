document.addEventListener("DOMContentLoaded", function () {

    const openButton =
        document.getElementById(
            "adminSidebarOpen"
        );

    const closeButton =
        document.getElementById(
            "adminSidebarClose"
        );

    function openMenu() {
        document.body.classList.add(
            "admin-menu-open"
        );
    }

    function closeMenu() {
        document.body.classList.remove(
            "admin-menu-open"
        );
    }

    if (openButton) {
        openButton.addEventListener(
            "click",
            openMenu
        );
    }

    if (closeButton) {
        closeButton.addEventListener(
            "click",
            closeMenu
        );
    }

    document.addEventListener(
        "keydown",
        function (event) {

            if (event.key === "Escape") {
                closeMenu();
            }
        }
    );

    window.addEventListener(
        "resize",
        function () {

            if (window.innerWidth > 850) {
                closeMenu();
            }
        }
    );
});