package com.krux.hyperion.objects

import com.krux.hyperion.HyperionContext

/**
 * A spark step that runs on Spark Cluster
 */
case class SparkStep(
    jar: String = "",
    mainClass: String = "",
    args: Seq[String] = List()
  )(
    implicit hc: HyperionContext
  ){

  val scriptRunner = "s3://elasticmapreduce/libs/script-runner/script-runner.jar"
  val jobRunner = s"${hc.scriptUri}run-spark-step.sh"

  def withJar(jar: String) = this.copy(jar = jar)
  def withMainClass(mainClass: String) = this.copy(mainClass = mainClass)
  def withArgs(args: String*) = this.copy(args = args)
  def withArgSeq(args: Seq[String]) = this.copy(args = args)

  def toStepString = (scriptRunner +: jobRunner +: jar +: mainClass +: args).mkString(",")

}