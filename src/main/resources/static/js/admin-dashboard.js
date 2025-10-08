document.addEventListener('DOMContentLoaded', function () {
    const tableBody = document.getElementById('paymentsTableBody');
    const filterForm = document.getElementById('filterForm');

    // Helper function to fetch and display payments (using the existing implementation)
    const fetchAndDisplayPayments = (url) => {
        // --- THIS IS THE CORRECTED URL ---
        fetch(url)
            .then(response => {
                if (!response.ok) {
                    throw new Error('Network response was not ok');
                }
                return response.json();
            })
            .then(data => {
                tableBody.innerHTML = ''; // Clear existing data
                data.forEach(payment => {
                    // Helper function for status badges with colors and icons (reconstructed from template)
                    const getStatusBadge = (status) => {
                        switch(status) {
                            case 'PAID': return '<span class="badge bg-success"><i class="bi bi-check-circle-fill me-1"></i>PAID</span>';
                            case 'PENDING': return '<span class="badge bg-warning text-dark"><i class="bi bi-clock-fill me-1"></i>PENDING</span>';
                            case 'FAILED': return '<span class="badge bg-danger"><i class="bi bi-x-circle-fill me-1"></i>FAILED</span>';
                            default: return `<span class="badge bg-secondary">${status}</span>`;
                        }
                    };
                    
                    // Helper function for method badges (reconstructed from template)
                    const getMethodBadge = (method) => {
                        let badgeClass = 'bg-secondary';
                        if (method) {
                            const lowerMethod = method.toLowerCase();
                            if (lowerMethod.includes('card')) badgeClass = 'bg-info';
                            else if (lowerMethod.includes('gcash')) badgeClass = 'bg-primary';
                            else if (lowerMethod.includes('bdo')) badgeClass = 'bg-dark';
                            else if (lowerMethod.includes('paymaya')) badgeClass = 'bg-light text-dark border'; 
                        }
                        return `<span class="badge ${badgeClass}">${method}</span>`;
                    };

                    const amountDisplay = payment.amount ? '₱' + parseFloat(payment.amount).toFixed(2) : 'N/A';
                    
                    const row = `
                        <tr>
                            <td>${payment.id}</td>
                            <td>${payment.name}</td>
                            <td>${payment.email}</td>
                            <td><span class="fw-bold">${amountDisplay}</span></td>
                            <td>${getMethodBadge(payment.method)}</td>
                            <td>${getStatusBadge(payment.status)}</td>
                            <td>
                                <a href="/admin/payment/edit/${payment.id}" class="btn btn-sm btn-warning me-1" title="Edit Payment">
                                    <i class="bi bi-pencil-square"></i>
                                </a>
                                <form action="/admin/payment/delete/${payment.id}" method="post" style="display:inline;" onsubmit="return confirm('Are you sure you want to delete the payment for ${payment.name}?');">
                                    <button type="submit" class="btn btn-sm btn-danger" title="Delete Payment">
                                        <i class="bi bi-trash"></i>
                                    </button>
                                </form>
                            </td>
                        </tr>
                    `;
                    tableBody.innerHTML += row;
                });
            })
            .catch(error => {
                console.error('Error fetching payments:', error);
                tableBody.innerHTML = '<tr><td colspan="7" class="text-center p-4 text-danger">Error loading data. Check console for details.</td></tr>';
            });
    };


    // Initial data load
    fetchAndDisplayPayments('/admin/api/payments');

    // Filter logic
    filterForm.addEventListener('submit', function (e) {
        e.preventDefault();
        const keyword = document.getElementById('keyword').value.trim();
        const method = document.getElementById('method').value;
        const minAmount = document.getElementById('minAmount').value;
        const maxAmount = document.getElementById('maxAmount').value;

        let url = '/admin/api/payments?';
        
        // Build the URL with all available filters
        if (keyword) url += `keyword=${keyword}&`;
        if (method) url += `method=${method}&`;
        if (minAmount) url += `minAmount=${minAmount}&`;
        if (maxAmount) url += `maxAmount=${maxAmount}&`;
        
        // Clean up trailing & or ?
        if (url.endsWith('?') || url.endsWith('&')) {
            url = url.slice(0, -1);
        }

        fetchAndDisplayPayments(url);
    });
});