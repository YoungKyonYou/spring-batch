package com.example.springbatchtest.batch;
import org.springframework.batch.core.Job;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class BatchController {

    private final JobLauncher jobLauncher;

    private final Job txDailyMonthlyJob;

    @PostMapping("/run-batch")
    public String runBatch() throws Exception {
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("from", "2025-08-01")
                .addString("to", "2025-09-30")
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();

        jobLauncher.run(txDailyMonthlyJob, jobParameters);
        return "Batch job started";
    }
}
