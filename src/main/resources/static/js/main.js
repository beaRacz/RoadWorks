/**
 * RoadWorks Manager – main.js
 * Diverse utilitare front-end
 */

document.addEventListener('DOMContentLoaded', function () {

    // 1. Auto-dismiss flash alerts after 5 seconds
    document.querySelectorAll('.alert.alert-success, .alert.alert-info').forEach(function (alert) {
        setTimeout(function () {
            const bsAlert = bootstrap.Alert.getOrCreateInstance(alert);
            if (bsAlert) bsAlert.close();
        }, 5000);
    });

    // 2. Highlight overdue/expiring table rows with pulse on badge
    document.querySelectorAll('.badge.bg-danger').forEach(function (badge) {
        // Only pulse badges that contain "NOT REMOVED" or "EXPIRED"
        if (badge.textContent.includes('NOT REMOVED') || badge.textContent.includes('EXPIRED')) {
            badge.classList.add('pulse');
        }
    });

    // 3. Confirm delete forms – extra safety
    document.querySelectorAll('form[data-confirm]').forEach(function (form) {
        form.addEventListener('submit', function (e) {
            const msg = form.dataset.confirm || 'Are you sure?';
            if (!confirm(msg)) {
                e.preventDefault();
            }
        });
    });

    // 4. Date validation: repairEndDate must be >= repairStartDate
    const startInput = document.querySelector('[name="repairStartDate"]');
    const endInput   = document.querySelector('[name="repairEndDate"]');
    if (startInput && endInput) {
        function updateMinEndDate() {
            if (startInput.value) {
                endInput.min = startInput.value;
            }
        }
        startInput.addEventListener('change', updateMinEndDate);
        updateMinEndDate();
    }

    // 5. Signs placed/removed toggle logic
    const signsPlacedCheckbox  = document.getElementById('signsPlaced');
    const signsRemovedCheckbox = document.getElementById('signsRemoved');
    if (signsPlacedCheckbox && signsRemovedCheckbox) {
        signsPlacedCheckbox.addEventListener('change', function () {
            if (!this.checked) {
                signsRemovedCheckbox.checked = false;
                signsRemovedCheckbox.disabled = true;
            } else {
                signsRemovedCheckbox.disabled = false;
            }
        });
        // Initial state
        if (!signsPlacedCheckbox.checked) {
            signsRemovedCheckbox.disabled = true;
        }
    }

    // 6. Table row click -> navigate to detail page (if data-href set)
    document.querySelectorAll('tr[data-href]').forEach(function (row) {
        row.addEventListener('click', function () {
            window.location.href = row.dataset.href;
        });
    });

    // 7. Tooltip initialization (Bootstrap 5)
    document.querySelectorAll('[data-bs-toggle="tooltip"]').forEach(function (el) {
        new bootstrap.Tooltip(el);
    });
});
