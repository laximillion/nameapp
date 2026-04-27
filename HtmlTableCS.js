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
      row.classList.remove('status-not-taken');
      row.classList.add('status-taken');
      button.style.borderColor = "#034876";
      button.style.color = "#034876";
    } else {
      button.textContent = "Not Taken";
      row.classList.remove('status-taken');
      row.classList.add('status-not-taken');
      button.style.borderColor = "#e2e8f0";
      button.style.color = "#475569";
    }
    updateProgress();
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

  function updateProgress() {
    const total = document.querySelectorAll('button').length;
    const taken = Array.from(document.querySelectorAll('button')).filter(btn => btn.textContent.trim() === "Taken").length;
    const percentage = Math.round((taken / total) * 100);
    
    const bar = document.getElementById('myProgressBar');
    bar.style.width = percentage + "%";
    bar.textContent = percentage + "%";

    
    if (percentage === 100) {
        confetti({
            particleCount: 150,
            spread: 70,
            origin: { y: 0.6 }
        });
    }
}