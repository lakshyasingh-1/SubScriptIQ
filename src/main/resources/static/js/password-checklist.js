(() => {
    const input = document.querySelector('[data-password-input]');
    const submit = document.querySelector('[data-password-submit]');
    if (!input || !submit) return;

    const rules = [
        ['length', (value) => value.length >= 9],
        ['uppercase', (value) => /[A-Z]/.test(value)],
        ['lowercase', (value) => /[a-z]/.test(value)],
        ['special', (value) => /[^A-Za-z0-9]/.test(value)]
    ];
    const update = () => {
        const value = input.value;
        const valid = rules.map(([name, check]) => {
            const passed = check(value);
            document.querySelector(`[data-rule="${name}"]`)?.classList.toggle('pass', passed);
            return passed;
        }).every(Boolean);
        submit.disabled = !valid;
    };
    input.addEventListener('input', update);
    update();
})();
