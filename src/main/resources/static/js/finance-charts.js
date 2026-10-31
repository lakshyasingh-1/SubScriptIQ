(() => {
    const canvas = document.querySelector('[data-finance-chart]');
    if (!canvas || !window.Chart) return;
    const labels = (canvas.dataset.labels || '').split('|').filter(Boolean);
    const invoiced = (canvas.dataset.invoiced || '').split(',').filter(Boolean).map(Number);
    const collected = (canvas.dataset.collected || '').split(',').filter(Boolean).map(Number);
    new Chart(canvas, {
        type: 'bar',
        data: {
            labels,
            datasets: [
                { label: 'Invoiced', data: invoiced, backgroundColor: '#70a0e8', borderRadius: 5 },
                { label: 'Collected', data: collected, backgroundColor: '#55bd91', borderRadius: 5 }
            ]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: { legend: { position: 'bottom', labels: { usePointStyle: true, boxWidth: 7 } } },
            scales: { y: { beginAtZero: true, grid: { color: '#e9edf1' } }, x: { grid: { display: false } } }
        }
    });
})();
