document.addEventListener('DOMContentLoaded', function () {
    const logsTableBody = document.getElementById('logsTableBody');

    // --- THIS IS THE CORRECTED URL ---
    fetch('/admin/api/logs')
        .then(response => {
            if (!response.ok) {
                // If the server response is not OK, throw an error to be caught by .catch()
                throw new Error('Network response was not ok. Status: ' + response.status);
            }
            return response.json();
        })
        .then(data => {
            logsTableBody.innerHTML = ''; // Clear any existing rows
            data.forEach(log => {
                // Format the timestamp to be more readable
                const formattedTimestamp = new Date(log.timestamp).toLocaleString('en-US', {
                    year: 'numeric',
                    month: 'short',
                    day: 'numeric',
                    hour: '2-digit',
                    minute: '2-digit',
                    second: '2-digit'
                });

                const row = `
                    <tr>
                        <td>${log.id}</td>
                        <td>${formattedTimestamp}</td>
                        <td>${log.action}</td>
                        <td>${log.details}</td>
                        <td>${log.user}</td>
                        <td>${log.ipAddress}</td>
                    </tr>
                `;
                logsTableBody.innerHTML += row;
            });
        })
        .catch(error => {
            console.error('Error fetching audit logs:', error);
            // Display a user-friendly error message in the table
            logsTableBody.innerHTML = '<tr><td colspan="6" class="text-center text-danger">Failed to load audit logs. Please check the console for more details.</td></tr>';
        });
});