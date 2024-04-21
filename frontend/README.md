# Network Monitoring System

The Network Monitoring System is a robust application designed to monitor and visualize real-time system metrics for network devices. Utilizing a modern tech stack, it offers efficient backend processing, dynamic frontend interaction, and seamless database management.

## Technologies Used
### Backend
* **Java**: Powering the backend logic for robust functionality.
* **Vert.x Core & Vert.x Web**: Enabling reactive, high-performance web handling.
* **Logger**: Ensuring comprehensive event logging for system monitoring.

### Frontend
* **React.js**: Driving the frontend with dynamic UI components.
* **Tailwind CSS**: Styling the interface for a sleek, responsive design.
* **ApexCharts**: Visualizing data with interactive and engaging chart displays.

## Database
* **MySQL**: Managing and storing system metrics data efficiently and securely.

## Features
* Supports monitoring upto **10,000** devices.
* Real-time monitoring of CPU, memory, and other vital system metrics.
* Interactive charting for analyzing historical data trends.
* User-friendly interface, optimized for usability.
* Scalable backend architecture ensures seamless performance under varying loads.

## Screenshots
![NMS 1](/frontend/public/nms1.png)
![NMS 2](/frontend//public/nms2.png)

## Backend APIs
| Sr. No.|Description| Request Type  | Endpoint  |
|---|---|---|---|
|1|Register Device| POST  | `/register-device`  |
|2|Start Polling| GET  |  `/start-polling` |
|3|Get System Metrics| GET  | `/get-data/:ip_address` |
|4|Get List of IP Address| GET  | `/get-ip-address` |

* **Register Device**: Add body parameter as follows
    ```
    {
        "username": "yash",
        "password": 1234,
        "ip_address": "127.0.0.1",
        "device_type": "linux"
    }
    ```
> Change values according your system configuration. To check IP address, run this following command:
```
ifconfig | grep broadcast | awk {'print $2'}
```

## GitHub Repo
<a href="https://github.com/thatbackendguy/network-monitoring-system"><img src="https://opengraph.githubassets.com/42bc0c1d6fa18b25576ead8f49432f0ca77199d85e517dd6b2366d9d4e4ab955/thatbackendguy/network-monitoring-system" width="50%"/></a>