document.addEventListener("DOMContentLoaded", function () {

    const authPage =
        document.querySelector("[data-initial-tab]");

    const tabButtons =
        document.querySelectorAll("[data-auth-target]");

    const panels =
        document.querySelectorAll("[data-auth-panel]");

    function showPanel(panelName) {

        tabButtons.forEach(function (button) {

            const active =
                button.dataset.authTarget === panelName;

            button.classList.toggle(
                "active",
                active
            );

            button.setAttribute(
                "aria-selected",
                String(active)
            );
        });

        panels.forEach(function (panel) {

            const active =
                panel.dataset.authPanel === panelName;

            panel.classList.toggle(
                "active",
                active
            );

            panel.hidden = !active;
        });
    }

    tabButtons.forEach(function (button) {

        button.addEventListener(
            "click",
            function () {

                showPanel(
                    button.dataset.authTarget
                );
            }
        );
    });

    const initialTab =
        authPage
            ? authPage.dataset.initialTab
            : "signin";

    showPanel(
        initialTab === "register"
            ? "register"
            : "signin"
    );

    const passwordButtons =
        document.querySelectorAll(
            "[data-password-toggle]"
        );

    passwordButtons.forEach(function (button) {

        button.addEventListener(
            "click",
            function () {

                const inputId =
                    button.dataset.passwordToggle;

                const input =
                    document.getElementById(inputId);

                if (!input) {
                    return;
                }

                const showing =
                    input.type === "text";

                input.type =
                    showing
                        ? "password"
                        : "text";

                const icon =
                    button.querySelector("i");

                if (icon) {

                    icon.className =
                        showing
                            ? "bi bi-eye"
                            : "bi bi-eye-slash";
                }

                button.setAttribute(
                    "aria-label",
                    showing
                        ? "Show password"
                        : "Hide password"
                );
            }
        );
    });

    const phoneInput =
        document.getElementById(
            "register-phone"
        );

    if (phoneInput) {

        phoneInput.addEventListener(
            "input",
            function () {

                phoneInput.value =
                    phoneInput.value
                        .replace(/\D/g, "")
                        .slice(0, 11);
            }
        );
    }

    const otpInput =
        document.querySelector(
            "[data-otp-input]"
        );

    if (otpInput) {

        otpInput.addEventListener(
            "input",
            function () {

                otpInput.value =
                    otpInput.value
                        .replace(/\D/g, "")
                        .slice(0, 6);
            }
        );
    }
});