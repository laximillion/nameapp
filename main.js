document.addEventListener('DOMContentLoaded', () => {
<<<<<<< Updated upstream
    //find the all the <select> labels under optiona-field
const selects = document.querySelectorAll(".optional-field select");
// for each select in selects
selects.forEach(select => {
    // set an event listener that trigger when user makes change to the select
    select.addEventListener('change', () => {
        // find the label in the same parent div of this <select>
        const label = select.parentElement.querySelector('label');
        // find the '+' symbol inside the label
        const symbol = label.querySelector(".optional-symbol");
        // if the value of select is valid
        if (select.value) {
            label.style.color = '#000';
            if (symbol) {
                symbol.style.color = '#000';
            }
        } else {
            label.style.color = '#888';
            if (symbol) {
                symbol.style.color = '#888';
            }
        }
    });
});

form.addEventListener('submit', (event) => {
    const m1 = major1Select.value;
    const m2 = major2Select.value;
    const mi = minorSelect.value;

    if (m2 && m1) {
        event.preventDefault();
        alert("You can only choose a second major OR a minor");
    }

    if (m2 && m1 == m2) {
        event.preventDefault();
        alert("Your Second Major must be different from your first Major.");
        return;
    }

    if (mi && m1 == mi) {
        event.preventDefault();
        alert("Your Minor must be different from your first Major.");
        return;
    }
    
});

});
