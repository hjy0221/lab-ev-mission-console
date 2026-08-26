package com.example.labevmissionconsole.domain

class MissionWorkflow(initial: MissionStage = MissionStage.MISSION) {
  var stage: MissionStage = initial
    private set

  fun moveTo(next: MissionStage): MissionStage {
    require(next.ordinal == stage.ordinal + 1) { "Invalid transition: $stage -> $next" }
    stage = next
    return stage
  }
}
