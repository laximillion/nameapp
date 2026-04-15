/**
 * 
 * @param {HTMLElement} button 
 */

function switchStatus(button) {
    /* find the closest table row that contains button */
    const row = button.closest("tr");

    /* switch the button based on current status */
    if (button.textContent === "Not Taken") {
      button.textContent = "Taken";
      row.classList.remove("not-taken");
      row.classList.add("taken");
    } else {
      button.textContent = "Not Taken";
      row.classList.remove("taken");
      row.classList.add("not-taken");
    }
  }

  function switchStatusCheck(checkbox) {
    /* find the closest table row that contains button */
    const row = checkbox.closest("tr");

    /* switch the button based on current status */
    if (checkbox.checked == true) {
      //button.textContent = "Taken";
      row.classList.remove("not-taken");
      row.classList.add("taken");
      //row.style.backgroundColor = "#DAFFD5";
    } else {
      //button.textContent = "Not Taken";
      row.classList.remove("taken");
      row.classList.add("not-taken");
      //row.style.backgroundColor = "#FFD1DC";
    }
  }