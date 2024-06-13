$(document).ready(function() {
  const ctx = document.getElementById('chart').getContext('2d');
  const chart = new Chart(ctx, {
    type: 'bar',
    data: {
      labels: ['Dysk (256 GB)', 'RAM (8 GB)'],
      datasets: [{
        label: 'Uzycie',
        data: [],
        backgroundColor: [
          'rgba(255, 99, 132, 0.2)',
          'rgba(54, 162, 235, 0.2)'
        ],
        borderColor: [
          'rgba(255, 99, 132, 1)',
          'rgba(54, 162, 235, 1)'
        ],
        borderWidth: 1
      }]
    },
    options: {
      scales: {
        y: {
          beginAtZero: true,
          max: 100, // Set the maximum value of the Y-axis to 100
          title: {
            display: true,
            text: 'Użycie (%)' // Display a title for the Y-axis
          }
        }
      }
    }
  });

  setInterval(async function() {
    const response = await fetch('/api/data');
    const data = await response.json();
    const diskUsagePercentage = (data.diskUsage / 256) * 100; // Convert disk usage to percentage
    const memoryUsagePercentage = (data.memoryUsage / 8192) * 100; // Convert memory usage to percentage
    chart.data.datasets[0].data = [diskUsagePercentage, memoryUsagePercentage];
    chart.update();
  
    // Update the text content of the paragraph elements
    document.getElementById('disk-usage').textContent = `Użycie dysku: ${data.diskUsage.toFixed(2)} GB`;
    document.getElementById('ram-usage').textContent = `Użycie RAM: ${data.memoryUsage.toFixed(2)} MB`;
  }, 5000);
});