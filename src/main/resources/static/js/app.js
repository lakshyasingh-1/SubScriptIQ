(() => {
    let pendingConfirmation = null;
    const token = document.querySelector('meta[name="csrf-token"]')?.content;
    const header = document.querySelector('meta[name="csrf-header"]')?.content;
    if (token && header && window.htmx) {
        document.body.setAttribute('hx-headers', JSON.stringify({ [header]: token }));
    }
    if (document.querySelector('[data-demo-error]')) {
        document.querySelectorAll('form .form-control, form .form-select').forEach((field) => {
            field.classList.add('is-invalid');
        });
    }

    document.addEventListener('click', (event) => {
        const trigger = event.target.closest('[data-confirm]');
        if (trigger && trigger.dataset.confirmed !== 'true') {
            event.preventDefault();
            pendingConfirmation = trigger;
            document.querySelector('#confirmModalMessage').textContent = trigger.dataset.confirm;
            bootstrap.Modal.getOrCreateInstance(document.querySelector('#confirmModal')).show();
        }
        const addButton = event.target.closest('[data-add-row]');
        if (addButton) {
            const body = document.querySelector(addButton.dataset.addRow);
            const row = document.createElement('tr');
            row.innerHTML = '<td><input class="form-control" name="variantAttributes[]" aria-label="Variant attribute" required></td>' +
                '<td><input class="form-control" name="variantValues[]" aria-label="Variant value" required></td>' +
                '<td><input class="form-control" name="variantExtraPrices[]" type="number" step="0.01" value="0" aria-label="Variant additional price"></td>' +
                '<td><button class="btn btn-sm btn-outline-danger" type="button" data-remove-row>Remove</button></td>';
            body?.append(row);
        }
        const removeButton = event.target.closest('[data-remove-row]');
        if (removeButton) removeButton.closest('tr')?.remove();
    });

    document.querySelector('#confirmModalContinue')?.addEventListener('click', () => {
        if (!pendingConfirmation) return;
        const trigger = pendingConfirmation;
        pendingConfirmation = null;
        trigger.dataset.confirmed = 'true';
        bootstrap.Modal.getOrCreateInstance(document.querySelector('#confirmModal')).hide();
        if (trigger.form) trigger.form.requestSubmit(trigger);
        else if (trigger.href) window.location.assign(trigger.href);
    });

    document.querySelector('#confirmModal')?.addEventListener('hidden.bs.modal', () => {
        pendingConfirmation = null;
    });

    document.addEventListener('change', (event) => {
        if (event.target.matches('[data-discount-scope]')) {
            const products = document.querySelector('[data-product-scope]');
            if (products) products.hidden = event.target.value !== 'PRODUCT';
        }
    });

    document.addEventListener('input', (event) => {
        if (event.target.matches('[data-payment-amount]')) {
            document.querySelectorAll('[data-outstanding-row]').forEach((row) => {
                const normalize = (value) => value.trim().replace(/(\.\d*?[1-9])0+$|\.0+$/, '$1');
                const matches = normalize(row.dataset.outstanding) === normalize(event.target.value);
                row.classList.toggle('border-primary', matches);
                row.classList.toggle('bg-primary-subtle', matches);
            });
        }
    });

    document.addEventListener('htmx:responseError', (event) => {
        const target = event.detail.target;
        if (target) {
            target.innerHTML = '<div class="alert alert-danger mb-0" role="alert">The request failed. Please try again.</div>';
        }
    });
})();
