import React, { useState, useEffect } from "react";
import { FaPlay } from "react-icons/fa6";
import { TbDeviceImacPlus } from "react-icons/tb";
import axios from "axios";
import "./App.css";
import { toast, Toaster } from "react-hot-toast";
import { Select, Modal, Input, Tooltip, Badge } from "antd";
import { ChartComponent } from "./components/ChartComponent.jsx";
import { FaBell } from "react-icons/fa";
// import AlertModal from "./components/AlertModal.jsx";

function App() {
  const [data, setData] = useState();
  const [ipAddress, setIpAddress] = useState();
  const [selectedIpAddress, setSelectedIpAddress] = useState();
  const [modalVisible, setModalVisible] = useState(false);
  const [formData, setFormData] = useState({
    username: "",
    ipAddress: "",
    password: "",
    deviceType: "",
  });
  // const [alertModal, setAlertModal] = useState()
  // const [alerts, setAlerts] = useState()

  const getAlerts = async () => {
    try {
      const response = await axios.get(
        `http://localhost:8080/get-alerts/${selectedIpAddress}`
      );

      console.log(response.data);

      // if (response.data["status"] === "success") {
      //   setAlerts(response.data);
      // } else {
      //   setComments([]);
      // }
    } catch (error) {
      console.log(error);
      // setComments([]);
    }

  };

  const clearAlerts = async () => {
    try {
      const response = await axios.delete(
         `http://localhost:8080/clear-alerts/${selectedIpAddress}`
      );

      if (response.data.status === "success") {
        // console.log(response.data)
        toast.success(response.data.message);
        getAlerts();
      } else {
        toast.error(response.data.message);
      }
    } catch (error) {
      console.log(error);
    }
  };

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
      setIpAddress(response.data["ip.address"]);
    } catch (error) {
      console.log(error);
    }
  };

  const startPolling = async () => {
    try {
      const res = await axios.get(`http://localhost:8080/start-polling`);

      if (res?.data?.status === "success") {
        toast.success(res?.data?.message);
      } else {
        toast.error(res?.data?.message);
      }
    } catch (error) {
      toast.error("Server is down!");
      console.log(error);
    }
  };

  const openModal = () => {
    setModalVisible(true);
  };

  const closeModal = () => {
    setModalVisible(false);
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData({ ...formData, [name]: value });
  };

  const handleSubmit = async () => {
    try {
      const res = await axios.post(`http://localhost:8080/register-device`, {
        "username": formData.username.trim(),
        "password": formData.password.trim(),
        "ip.address": formData.ipAddress.trim(),
        "device.type": formData.deviceType.trim(),
      });
      if (res?.data?.status === "success") {
        toast.success(res?.data?.message);

        setFormData({
          username: "",
          ipAddress: "",
          password: "",
          deviceType: "",
        });
      } else {
        toast.error(res?.data?.message);
      }
      closeModal();
    } catch (error) {
      toast.error("Server is down!");
      console.log(error);
    }
  };

  useEffect(() => {
    fetchData();

    const fetchDataInterval = setInterval(() => {
      fetchData();
    }, 10000);

    return () => {
      clearInterval(fetchDataInterval);
    };
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
        categories: data && data['poll.timestamp'],
      },
    },
    series: [
      {
        name: "System CPU Percentage",
        data: data && data["system.cpu.percentage"].map(Number) ,
      },
      {
        name: "User CPU Percentage",
        data: data && data["user.cpu.percentage"].map(Number) ,
      },
      {
        name: "Idle CPU Percentage",
        data: data && data["idle.cpu.percentage"].map(Number) ,
      },
    ],
  };

  const memoryStatsChartData = {
    options: {
      chart: {
        id: "memory-stats-chart",
      },
      xaxis: {
        categories: data && data['poll.timestamp'],
      },
    },
    series: [
      {
        name: "Total Memory",
        data: data && data["total.memory"].map(Number) ,
      },
      {
        name: "Used Memory",
        data: data && data["used.memory"].map(Number) ,
      },
      {
        name: "Free Memory",
        data: data && data["free.memory"].map(Number) ,
      },
    ],
  };

  const swapMemoryStatsChartData = {
    options: {
      chart: {
        id: "swap-memory-stats-chart",
      },
      xaxis: {
        categories: data && data['poll.timestamp'],
      },
    },
    series: [
      {
        name: "Total Swap Memory",
        data: data && data["total.swap.memory"].map(Number) ,
      },
      {
        name: "Used Swap Memory",
        data: data && data["used.swap.memory"].map(Number) ,
      },
      {
        name: "Free Swap Memory",
        data: data &&  data["free.swap.memory"].map(Number) ,
      },
    ],
  };

  const contextSwitchesChartData = {
    options: {
      chart: {
        id: "context-switches-chart",
      },
      xaxis: {
        categories: data && data['poll.timestamp'],
      },
    },
    series: [
      {
        name: "Context Switches",
        data: data && data["context.switches"].map(Number) ,
      },
    ],
  };


  return (
    <div className="app">
      <Toaster />

      <div className="z-10 fixed bottom-6 right-6">
        <div>
          <Badge count={7} className="mb-[-40px]" />
        </div>

        <div className="py-4 hover:cursor-pointer px-4 rounded-full bg-white text-red-500 hover:text-red-600">
          <FaBell className="text-2xl" />
        </div>
      </div>

      <div className="sm:flex md:flex lg:flex w-full px-12 items-center justify-between py-8 fixed bg-[#1f1d2c] z-[1000]">
        <div className="flex items-center gap-4">
          <img src="/src/assets/monitoring.png" alt="Logo" width={64} />
          <h1 className="text-white text-lg sm:text-xl md:text-2xl lg:text-3xl text-center font-bold">
            Network Monitoring System
          </h1>
        </div>
        <div className="flex text-center md:text-center gap-4">
          <div className="flex items-center gap-4">
            <Badge
              className="mt-6 hover:cursor-not-allowed"
              count={ipAddress ? ipAddress.length : 0}
              style={{ backgroundColor: "#52c41a" }}
            />
            <div>
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

          <div className="gap-4 flex">
            <Tooltip
              title="Agent will start poling data from regsitered devices [*/10 secs]"
              placement="bottom"
            >
              <button
                className="flex bg-green-500 hover:bg-green-700 text-center text-white font-semibold py-2 px-6 rounded-full hover:cursor-pointer items-center gap-3"
                onClick={startPolling}
              >
                <FaPlay className="text-xl" />
                <div> Start Polling</div>
              </button>
            </Tooltip>
            <button
              className="flex bg-blue-500 hover:bg-blue-700 text-center text-white font-semibold py-2 px-6 rounded-full hover:cursor-pointer items-center gap-3"
              onClick={openModal}
            >
              <TbDeviceImacPlus className="text-xl" />
              <div>Register Device</div>
            </button>
          </div>
        </div>
      </div>
      <br />
      <br />
      <br />
      <br />
      <div className="grid grid-cols-1 gap-12 lg:grid-cols-2 lg:gap-32 p-12 ">
        <ChartComponent title={"CPU Stats"} chartData={cpuStatsChartData} />

        <ChartComponent
          title={"Memory Stats"}
          chartData={memoryStatsChartData}
        />

        <ChartComponent
          title={"Context Switches Stats"}
          chartData={contextSwitchesChartData}
        />

        <ChartComponent
          title={"Swap Memory Stats"}
          chartData={swapMemoryStatsChartData}
        />
      </div>

      {/* Modal */}
      <Modal
        title="Register Device"
        visible={modalVisible}
        onCancel={closeModal}
        footer={[
          <button
            key="submit"
            onClick={handleSubmit}
            className="bg-blue-500 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded mr-4"
          >
            Submit
          </button>,
          <button
            key="cancel"
            onClick={closeModal}
            className="border border-gray-300 hover:border-gray-400 text-gray-700 font-bold py-2 px-4 rounded"
          >
            Cancel
          </button>,
        ]}
      >
        <div className="mb-4">
          <label className="block text-gray-700 text-sm font-bold mb-2">
            Username:
          </label>
          <Input
            type="text"
            name="username"
            value={formData.username}
            onChange={handleInputChange}
          />
        </div>
        <div className="mb-4">
          <label className="block text-gray-700 text-sm font-bold mb-2">
            IP Address:
          </label>
          <Input
            type="text"
            name="ipAddress"
            value={formData.ipAddress}
            onChange={handleInputChange}
          />
        </div>
        <div className="mb-4">
          <label className="block text-gray-700 text-sm font-bold mb-2">
            Password:
          </label>
          <Input
            type="password"
            name="password"
            value={formData.password}
            onChange={handleInputChange}
          />
        </div>
        <div className="mb-4">
          <label className="block text-gray-700 text-sm font-bold mb-2">
            Device Type:
          </label>
          <Input
            type="text"
            name="deviceType"
            value={formData.deviceType}
            onChange={handleInputChange}
          />
        </div>
      </Modal>

      {/* <AlertModal
      visible={alertModal}
      setVisible={setAlertModal}
      alerts={alerts}
      clearAlerts={clearAlerts}
      /> */}

      <footer className="text-white text-center mt-6 py-4">
        <a target="_blank" href="https://www.github.com/thatbackendguy">
          Copyright © {new Date().getFullYear()} Yash Prajapati
          (@thatbackendguy)
        </a>
      </footer>
    </div>
  );
}

export default App;
