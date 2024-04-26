import React, { useState } from "react";
import { Modal, Form, Input, Button } from "antd";
import { LuSendHorizonal } from "react-icons/lu";

const AlertModal = ({ visible, setVisible, alerts, clearAlerts }) => {
  const handleResolve = () => {
    clearAlerts();
  };

  const formatDate = (timestamp) => {
    const date = new Date(timestamp);
    const options = {
      year: "numeric",
      month: "short",
      day: "numeric",
      hour: "2-digit",
      minute: "2-digit",
      second: "2-digit",
      hour12: true,
    };
    return date.toLocaleString("en-US", options);
  };

  return (
    <div className="alert">
      <Modal
        title="Alerts"
        visible={visible}
        onCancel={() => setVisible(false)}
        footer={null}
      >
        <div className="alert-modal max-h-[300px] overflow-y-auto">
          {alerts?.length > 0 &&
            alerts?.map((item, index) => (
              <div className="flex justify-between items-center bg-gray-100 my-2 rounded-lg py-2 px-4">
                <p className="text-lg">{item.message}</p>
                <span>{formatDate(item.timestamp)}</span>
              </div>
            ))}
        </div>
        <div className="flex justify-end">
          <button
            className="hover:cursor-pointer text-lg py-2 hover:bg-red-600 w-32 px-4 bg-red-500 text-white rounded-lg mt-2"
            onClick={clearAlerts}
          >
            Clear All
          </button>
        </div>
      </Modal>
    </div>
  );
};

export default AlertModal;
