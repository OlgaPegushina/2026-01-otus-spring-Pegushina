package ru.otus.hw;

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.*;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import static org.assertj.core.api.Assertions.*;

class MigrateJobRestartIT {

    @Autowired private JobLauncher jobLauncher;

    @Autowired
    @Qualifier("migrateJob")
    private Job migrateJob;

    @Autowired private JobExplorer jobExplorer;

    @Test
    void shouldNotCreateNewInstance_whenRunWithSameParams() throws Exception {
        String jobName = migrateJob.getName();

        JobParameters params = new JobParametersBuilder()
                .addLong("dataVersion", 1L) // identifying параметр
                .toJobParameters();

        long instancesBefore = jobExplorer.getJobInstanceCount(jobName);

        JobExecution first = jobLauncher.run(migrateJob, params);
        assertThat(first.getStatus()).isEqualTo(BatchStatus.COMPLETED);

        long instancesAfterFirst = jobExplorer.getJobInstanceCount(jobName);
        assertThat(instancesAfterFirst).isEqualTo(instancesBefore + 1);

        assertThatThrownBy(() -> jobLauncher.run(migrateJob, params))
                .isInstanceOf(JobInstanceAlreadyCompleteException.class);

        long instancesAfterSecondTry = jobExplorer.getJobInstanceCount(jobName);
        assertThat(instancesAfterSecondTry).isEqualTo(instancesAfterFirst);
    }
}
