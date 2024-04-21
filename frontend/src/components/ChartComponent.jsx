import Chart from "react-apexcharts";

export const ChartComponent = ({chartData,title}) => {
  return (
    <div className="chart-container p-4 rounded-2xl bg-[#272637]">
    <h2 className="text-white px-4 py-8 text-xl md:text-2xl lg:text-3xl font-bold">{title}</h2>
    <Chart
      options={{
        ...chartData.options,
        chart: {
          ...chartData.options.chart,
          background: '#272637', // Set background color of chart
        },
        xaxis: {
          ...chartData.options.xaxis,
          labels: {
            ...chartData.options.xaxis.labels,
            style: {
              colors: '#ffffff', // Set x-axis label text color to white
            },
          },
        },
        yaxis: {
          ...chartData.options?.yaxis,
          labels: {
            ...chartData.options?.yaxis?.labels,
            style: {
              colors: '#ffffff', // Set y-axis label text color to white
            },
          },
        },
        legend: {
          ...chartData.options?.legend,
          labels: {
            colors: '#ffffff', // Set legend text color to white
          },
        },
      }}
      series={chartData.series}
      type="line"
    />
  </div>
  )
}
