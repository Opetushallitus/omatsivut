package fi.vm.sade.omatsivut.security

import java.time.Instant

import com.github.kagkarlsson.scheduler.task.{DeadExecutionHandler, Execution, ExecutionComplete, ExecutionContext, ExecutionOperations, TaskInstance, VoidExecutionHandler}
import com.github.kagkarlsson.scheduler.task.helper.{RecurringTask, Tasks}
import com.github.kagkarlsson.scheduler.task.schedule.{CronSchedule, Schedule}
import fi.vm.sade.utils.slf4j.Logging

object SessionCleaner extends Logging {

  def createTaskForScheduler(sessionService: SessionService, cronString: String) = {

    val executionHandler: VoidExecutionHandler[Void] = new VoidExecutionHandler[Void] {
      logger.info("Scheduled task execution handler setup")

      override def execute(taskInstance: TaskInstance[Void], executionContext: ExecutionContext): Unit = {
        logger.info("Scheduled session cleanup starting")
        sessionService.deleteAllExpired()
        logger.info("Scheduled session cleanup finished")
      }
    }

    class DeadExecutionRescheduler(schedule: Schedule) extends DeadExecutionHandler[Void] {
      logger.info(s"Dead execution handler setup for schedule $schedule")
      override def deadExecution(execution: Execution, executionOperations: ExecutionOperations[Void]): Unit = {
        val now = Instant.now
        val complete = ExecutionComplete.failure(execution, now, now, null)
        val next: Instant = schedule.getNextExecutionTime(complete)
        logger.warn("Rescheduling dead execution: " + execution + " to " + next)
        executionOperations.reschedule(complete, next)
      }
    }

    val cronSchedule: Schedule = new CronSchedule(cronString)
    val deadExecutionRescheduler = new DeadExecutionRescheduler(cronSchedule)

    logger.info(s"Session clean up, scheduled task created (cron=$cronString)")

    Tasks.recurring(s"cron-session-cleaner-task", cronSchedule)
      .onDeadExecution(deadExecutionRescheduler)
      .execute(executionHandler)
  }

}
