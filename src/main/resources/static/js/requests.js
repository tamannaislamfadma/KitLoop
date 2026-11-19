document.addEventListener("DOMContentLoaded", function () {

    const screenshotInput =
        document.getElementById(
            "paymentScreenshot"
        );

    const preview =
        document.getElementById(
            "proofPreview"
        );

    const placeholder =
        document.getElementById(
            "proofPlaceholder"
        );

    const senderNumber =
        document.getElementById(
            "senderNumber"
        );

    if (senderNumber) {

        senderNumber.addEventListener(
            "input",
            function () {

                senderNumber.value =
                    senderNumber.value
                        .replace(/\D/g, "")
                        .slice(0, 11);
            }
        );
    }

    if (!screenshotInput) {
        return;
    }

    screenshotInput.addEventListener(
        "change",
        function () {

            const file =
                screenshotInput.files
                    ? screenshotInput.files[0]
                    : null;

            if (!file) {

                if (preview) {
                    preview.hidden = true;
                    preview.src = "";
                }

                if (placeholder) {
                    placeholder.hidden = false;
                }

                return;
            }

            const allowedTypes = [
                "image/jpeg",
                "image/png"
            ];

            if (!allowedTypes.includes(
                file.type
            )) {

                window.alert(
                    "Payment screenshot must be JPG or PNG."
                );

                screenshotInput.value = "";

                return;
            }

            const maximumSize =
                5 * 1024 * 1024;

            if (file.size > maximumSize) {

                window.alert(
                    "Payment screenshot must be 5 MB or smaller."
                );

                screenshotInput.value = "";

                return;
            }

            const reader =
                new FileReader();

            reader.addEventListener(
                "load",
                function () {

                    if (preview) {

                        preview.src =
                            reader.result;

                        preview.hidden =
                            false;
                    }

                    if (placeholder) {
                        placeholder.hidden =
                            true;
                    }
                }
            );

            reader.readAsDataURL(
                file
            );
        }
    );
});