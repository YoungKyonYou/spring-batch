package com.example.springbatchtest.batch;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class BatchController {

    private final JobLauncher jobLauncher;

    private final Job txDailyMonthlyJob;
    private final Job fileToDbJob;


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

    @PostMapping("/run-file-to-db")
    public String runFileToDbBatch(@RequestParam(defaultValue = "data/file.txt") String inputFile) throws Exception {
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("inputFile", inputFile)
                .addLong("timestamp", System.currentTimeMillis())  // 매번 다른 파라미터로 실행 가능
                .toJobParameters();

        jobLauncher.run(fileToDbJob, jobParameters);
        return "File to DB Batch job started with file: " + inputFile;
    }
}
