document.addEventListener("DOMContentLoaded", function () {

    const imageInput =
        document.getElementById(
            "equipmentImage"
        );

    const preview =
        document.getElementById(
            "equipmentPreview"
        );

    const placeholder =
        document.getElementById(
            "imagePlaceholder"
        );

    const changeImageButton =
        document.getElementById(
            "changeImageButton"
        );

    const typeInputs =
        document.querySelectorAll(
            "[data-listing-type]"
        );

    const freeShareGroup =
        document.getElementById(
            "freeSharePriceGroup"
        );

    const rentGroup =
        document.getElementById(
            "rentPriceGroup"
        );

    const saleGroup =
        document.getElementById(
            "salePriceGroup"
        );

    const description =
        document.getElementById(
            "description"
        );

    const descriptionCounter =
        document.getElementById(
            "descriptionCounter"
        );

    function updateImagePreview() {

        if (!imageInput
            || !preview
            || !placeholder) {
            return;
        }

        const file =
            imageInput.files
                ? imageInput.files[0]
                : null;

        if (!file) {

            preview.hidden = true;
            preview.removeAttribute("src");

            placeholder.hidden = false;

            if (changeImageButton) {
                changeImageButton.hidden = true;
            }

            return;
        }

        const allowedTypes = [
            "image/jpeg",
            "image/png"
        ];

        if (!allowedTypes.includes(file.type)) {

            window.alert(
                "Please select a JPG or PNG image."
            );

            imageInput.value = "";

            updateImagePreview();

            return;
        }

        const maxBytes =
            5 * 1024 * 1024;

        if (file.size > maxBytes) {

            window.alert(
                "Equipment image must be 5 MB or smaller."
            );

            imageInput.value = "";

            updateImagePreview();

            return;
        }

        const reader =
            new FileReader();

        reader.addEventListener(
            "load",
            function () {

                preview.src =
                    reader.result;

                preview.hidden = false;

                placeholder.hidden = true;

                if (changeImageButton) {
                    changeImageButton.hidden = false;
                }
            }
        );

        reader.readAsDataURL(file);
    }

    function selectedListingType() {

        const selected =
            document.querySelector(
                "[data-listing-type]:checked"
            );

        return selected
            ? selected.value
            : null;
    }

    function updateConditionalFields() {

        const selectedType =
            selectedListingType();

        if (freeShareGroup) {
            freeShareGroup.hidden =
                selectedType !== "FREE_SHARE";
        }

        if (rentGroup) {
            rentGroup.hidden =
                selectedType !== "RENT";
        }

        if (saleGroup) {
            saleGroup.hidden =
                selectedType !== "SALE";
        }

        updateTypeCardSelection();
    }

    function updateTypeCardSelection() {

        const options =
            document.querySelectorAll(
                ".listing-type-option"
            );

        options.forEach(function (option) {

            const input =
                option.querySelector(
                    "input[type='radio']"
                );

            option.classList.toggle(
                "selected",
                Boolean(
                    input
                    && input.checked
                )
            );
        });
    }

    function updateDescriptionCounter() {

        if (!description
            || !descriptionCounter) {
            return;
        }

        descriptionCounter.textContent =
            description.value.length
            + " / 2000";
    }

    if (imageInput) {

        imageInput.addEventListener(
            "change",
            updateImagePreview
        );
    }

    if (changeImageButton
        && imageInput) {

        changeImageButton.addEventListener(
            "click",
            function () {

                imageInput.click();
            }
        );
    }

    typeInputs.forEach(function (input) {

        input.addEventListener(
            "change",
            updateConditionalFields
        );
    });

    if (description) {

        description.addEventListener(
            "input",
            updateDescriptionCounter
        );
    }

    updateConditionalFields();
    updateDescriptionCounter();
});