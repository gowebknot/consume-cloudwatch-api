package com.demo.monitor_rds.service;


import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.cloudwatch.model.*;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CloudWatchMetricsService {
    private final CloudWatchClient cloudWatchClient;

    public CloudWatchMetricsService(CloudWatchClient cloudWatchClient) {
        this.cloudWatchClient = cloudWatchClient;
    }

    public Map<String,Double> getAllMetrics(){
        Map<String,Double> metricsValues = new HashMap<>();

        String dbInstanceIdentifier = "database-2";
        metricsValues.put("ReadIOPS", getMetricValue("ReadIOPS", dbInstanceIdentifier));
        metricsValues.put("WriteIOPS", getMetricValue("WriteIOPS", dbInstanceIdentifier));
        metricsValues.put("DatabaseConnections", getMetricValue("DatabaseConnections", dbInstanceIdentifier));
        metricsValues.put("NetworkReceiveThroughput", getMetricValue("NetworkReceiveThroughput", dbInstanceIdentifier));
        metricsValues.put("NetworkTransmitThroughput", getMetricValue("NetworkTransmitThroughput", dbInstanceIdentifier));
        metricsValues.put("ReplicaLog", getMetricValue("ReplicaLog", dbInstanceIdentifier));
        metricsValues.put("FreeableMemory", getMetricValue("FreeableMemory", dbInstanceIdentifier));
        metricsValues.put("CPUCreditUsage", getMetricValue("CPUCreditUsage", dbInstanceIdentifier));
        metricsValues.put("CPUCreditBalance", getMetricValue("CPUCreditBalance", dbInstanceIdentifier));
        return metricsValues;
    }

    private Double getMetricValue(String metricName, String dbInstanceIdentifier) {
        try {
            Instant endTime = Instant.now();  // Use current time as end time
            Instant startTime = endTime.minus(Duration.ofMinutes(5));  // Example: 5 minutes ago

            GetMetricDataRequest request = GetMetricDataRequest.builder()
                    .startTime(startTime)
                    .endTime(endTime)
                    .metricDataQueries(
                            MetricDataQuery.builder()
                                    .id("m1")  // Unique identifier for this query
                                    .metricStat(
                                            MetricStat.builder()
                                                    .metric(
                                                            Metric.builder()
                                                                    .namespace("AWS/RDS")  // Replace with your metric's namespace
                                                                    .metricName(metricName)
                                                                    .dimensions(
                                                                            Dimension.builder()
                                                                                    .name("DBInstanceIdentifier")
                                                                                    .value(dbInstanceIdentifier)
                                                                                    .build()
                                                                    )
                                                                    .build()
                                                    )
                                                    .period(300)
                                                    .stat("Average")
                                                    .build()
                                    )
                                    .build()
                    )
                    .build();

            GetMetricDataResponse response = cloudWatchClient.getMetricData(request);

            List<Double> values = response.metricDataResults().get(0).values();
            return values.isEmpty() ? null : values.get(0);
        } catch (Exception ex) {
            ex.printStackTrace();
            return -1d;  // Return a default value indicating failure
        }
    }


}
