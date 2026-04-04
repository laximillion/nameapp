document.addEventListener('DOMContentLoaded', () => {
    const selects = document.querySelectorAll(".optional-field select");

    selects.forEach(select => {
        select.addEventListener('change', () => {
            const label = select.parentElement.querySelector('label');
            const symbol = label.querySelector(".optional-symbol");

            if (select.value) {
                label.style.color = '#000';
                if (symbol) symbol.style.color = '#000';
            } else {
                label.style.color = '#888';
                if (symbol) symbol.style.color = '#888';
            }
        });
    });
});