package de.rki.coronawarnapp.contactdiary.retention

import de.rki.coronawarnapp.bugreporting.reportProblem
import de.rki.coronawarnapp.task.Task
import de.rki.coronawarnapp.task.TaskFactory
import de.rki.coronawarnapp.task.common.DefaultProgress
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import org.joda.time.Duration
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Provider

class ContactDiaryCleanTask @Inject constructor(
    private val retentionCalculation: ContactDiaryDataRetentionCalculation
) : Task<DefaultProgress, Task.Result> {

    private val internalProgress = ConflatedBroadcastChannel<DefaultProgress>()
    override val progress: Flow<DefaultProgress> = internalProgress.asFlow()

    private var isCanceled = false

    override suspend fun run(arguments: Task.Arguments) = try {
        Timber.d("Running with arguments=%s", arguments)

        retentionCalculation.clearObsoleteContactDiaryLocationVisits()
        Timber.tag(TAG).d("Obsolete contact diary location visits cleaned up")

        retentionCalculation.clearObsoleteContactDiaryPersonEncounters()
        Timber.tag(TAG).d("Obsolete contact diary person encounters cleaned up")

        object : Task.Result {}
    } catch (error: Exception) {
        Timber.tag(TAG).e(error)
        // We do not need to show any error pop-up message to the user, so if any error would appear - log and forget
        error.reportProblem(TAG, error.message)
        object : Task.Result {}
    } finally {
        Timber.i("Finished (isCanceled=$isCanceled).")
        internalProgress.close()
    }

    override suspend fun cancel() {
        Timber.w("cancel() called.")
        isCanceled = true
    }

    data class Result(val deletedEntries: Int) : Task.Result

    class Config : TaskFactory.Config {
        override val executionTimeout: Duration = Duration.standardMinutes(9)
        override val collisionBehavior: TaskFactory.Config.CollisionBehavior =
            TaskFactory.Config.CollisionBehavior.SKIP_IF_SIBLING_RUNNING
    }

    class Factory @Inject constructor(
        private val taskByDagger: Provider<ContactDiaryCleanTask>
    ) : TaskFactory<DefaultProgress, Task.Result> {

        override suspend fun createConfig(): TaskFactory.Config = Config()

        override val taskProvider: () -> Task<DefaultProgress, Task.Result> = {
            taskByDagger.get()
        }
    }

    companion object {
        private val TAG: String? = ContactDiaryCleanTask::class.simpleName
    }
}
