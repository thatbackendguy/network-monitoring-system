import Chart from "react-apexcharts";

export const ChartComponent = ({chartData,title}) => {
  return (
    <div className="chart-container p-4 rounded-[2rem] bg-[#272637]">
    <h2 className="text-white px-4 py-8 text-xl md:text-2xl lg:text-3xl font-semibold">{title}</h2>
    <Chart
      options={{
        dataLabels: {
          enabled: false
        },
        stroke: {
          curve: 'smooth'
        },
        ...chartData.options,
        chart: {
          ...chartData.options.chart,
          background: '#272637',
        },
        xaxis: {
          // type: 'datetime',
          ...chartData.options.xaxis,
          labels: {
            ...chartData.options.xaxis.labels,
            style: {
              colors: '#ffffff',
            },
          },
        },
        yaxis: {
          ...chartData.options?.yaxis,
          labels: {
            ...chartData.options?.yaxis?.labels,
            style: {
              colors: '#ffffff',
            },
          },
        },
        legend: {
          ...chartData.options?.legend,
          labels: {
            colors: '#ffffff', // Set legend text color to white
            style: {
              fontSize:'1rem'
            }
          },
        },
      }}
      series={chartData.series}
      type="area"
    />
  </div>
  )
}
