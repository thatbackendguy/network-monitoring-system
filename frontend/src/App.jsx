import React, { useState, useEffect } from "react";
import axios from "axios";
import { Select } from "antd";
import "./App.css";
import { ChartComponent } from "./components/ChartComponent.jsx";

function App() {
  const [data, setData] = useState();
  const [ipAddress, setIpAddress] = useState();
  const [selectedIpAddress, setSelectedIpAddress] = useState();

  const handleChange = (value) => {
    setSelectedIpAddress(value);
  };

  const selectOptions = ipAddress?.map((item) => {
    return { label: item, value: item };
  });

  const fetchData = async () => {
    try {
      const response = await axios.get(
        `http://localhost:8080/get-data/${selectedIpAddress}`
      );
      setData(response.data);
    } catch (error) {
      console.log(error);
    }
  };

  const fetchIpAddress = async () => {
    try {
      const response = await axios.get(`http://localhost:8080/get-ip-address`);
      setIpAddress(response.data.ip_address);
    } catch (error) {
      console.log(error);
    }
  };

  useEffect(() => {
    fetchData();
  }, [selectedIpAddress]);

  useEffect(() => {
    fetchIpAddress();
  }, []);

  const cpuStatsChartData = {
    options: {
      chart: {
        id: "cpu-stats-chart",
      },
      xaxis: {
        categories: data?.poll_timestamp,
      },
    },
    series: [
      {
        name: "System CPU Percentage",
        data: data?.system_cpu_percentage?.map(Number),
      },
      {
        name: "User CPU Percentage",
        data: data?.user_cpu_percentage?.map(Number),
      },
      {
        name: "Idle CPU Percentage",
        data: data?.idle_cpu_percentage?.map(Number),
      },
    ],
  };

  const memoryStatsChartData = {
    options: {
      chart: {
        id: "memory-stats-chart",
      },
      xaxis: {
        categories: data?.poll_timestamp,
      },
    },
    series: [
      {
        name: "Total Memory",
        data: data?.total_memory?.map(Number),
      },
      {
        name: "Used Memory",
        data: data?.used_memory?.map(Number),
      },
      {
        name: "Free Memory",
        data: data?.free_memory?.map(Number),
      },
    ],
  };

  const swapMemoryStatsChartData = {
    options: {
      chart: {
        id: "swap-memory-stats-chart",
      },
      xaxis: {
        categories: data?.poll_timestamp,
      },
    },
    series: [
      {
        name: "Total Swap Memory",
        data: data?.total_swap_memory?.map(Number),
      },
      {
        name: "Used Swap Memory",
        data: data?.used_swap_memory?.map(Number),
      },
      {
        name: "Free Swap Memory",
        data: data?.free_swap_memory?.map(Number),
      },
    ],
  };

  const contextSwitchesChartData = {
    options: {
      chart: {
        id: "context-switches-chart",
      },
      xaxis: {
        categories: data?.poll_timestamp,
      },
    },
    series: [
      {
        name: "Context Switches",
        data: data?.context_switches?.map(Number),
      },
    ],
  };

  return (
    <div className="app">
      <div className="sm:flex md:flex lg:flex w-full px-12 items-center justify-between py-8 fixed bg-[#1f1d2c] z-[1000]">
      <h1 className="text-white text-lg sm:text-xl md:text-2xl lg:text-3xl text-center">Network Monitoring System</h1>
<div className="mt-4 lg:mt-0 md:mt-0 text-center md:text-center">
  <p className="text-white pb-2 text-sm">Select IP Address</p>
      <Select
        defaultValue="127.0.0.1"
        style={{
          width: 150,
        }}
        onChange={handleChange}
        options={selectOptions}
      />
      </div>
</div>
<br /><br /><br /><br />
      <div className="grid grid-cols-1 gap-12 lg:grid-cols-2 lg:gap-32 p-12 ">
        <ChartComponent title={"CPU Stats"} chartData={cpuStatsChartData} />
        <ChartComponent
          title={"Context Switches Stats"}
          chartData={contextSwitchesChartData}
        />
        <ChartComponent
          title={"Memory Stats"}
          chartData={memoryStatsChartData}
        />

        <ChartComponent
          title={"Swap Memory Stats"}
          chartData={swapMemoryStatsChartData}
        />
      </div>

      <footer className="text-white text-center mt-6 py-4"><a target="_blank" href="https://www.github.com/thatbackendguy">Copyright © {new Date().getFullYear()} Yash Prajapati (@thatbackendguy)</a></footer>
    </div>
  );
}

export default App;
