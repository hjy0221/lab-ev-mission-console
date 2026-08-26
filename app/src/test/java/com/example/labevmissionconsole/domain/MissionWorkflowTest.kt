package com.example.labevmissionconsole.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class MissionWorkflowTest {
  @Test fun `workflow advances through the five MVP stages`() {
    val workflow = MissionWorkflow()
    assertEquals(MissionStage.DRIVE, workflow.moveTo(MissionStage.DRIVE))
    assertEquals(MissionStage.FIELD, workflow.moveTo(MissionStage.FIELD))
    assertEquals(MissionStage.ANALYSIS, workflow.moveTo(MissionStage.ANALYSIS))
    assertEquals(MissionStage.REPORT, workflow.moveTo(MissionStage.REPORT))
  }

  @Test fun `workflow rejects skipped stages`() {
    val workflow = MissionWorkflow()
    assertThrows(IllegalArgumentException::class.java) { workflow.moveTo(MissionStage.FIELD) }
  }
}
